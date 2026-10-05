package com.chaos.gravestone.block;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.ChaosGravestone;
import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.config.GravestoneConfig;
import com.chaos.gravestone.storage.ProviderRegistry;
import com.chaos.gravestone.storage.SlotProvider;
import com.chaos.gravestone.storage.StoredEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GravestoneBlockEntity extends BlockEntity {

	private UUID graveId = UUID.randomUUID();
	private UUID owner;
	private String ownerName = "";
	/** Tiempo de juego al morir (para el desbloqueo). */
	private long deathTime;
	/** Fecha real al morir, en milisegundos (para el grabado). */
	private long deathMillis;
	/** Día del mundo en el que murió. */
	private long deathDay;
	@Nullable
	private Component deathCause;
	/** Número de objetos; en el cliente sustituye a {@link #entries}, que no se sincroniza. */
	private int itemCount;
	private final List<StoredEntry> entries = new ArrayList<>();

	public GravestoneBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.GRAVESTONE_ENTITY, pos, state);
	}

	public void init(ServerPlayer player, List<StoredEntry> contents, Component cause) {
		this.owner = player.getUUID();
		this.ownerName = player.getGameProfile().getName();
		this.deathTime = player.level().getGameTime();
		this.deathMillis = System.currentTimeMillis();
		this.deathDay = player.level().getDayTime() / 24000L + 1;
		this.deathCause = cause;
		this.entries.clear();
		this.entries.addAll(contents);
		this.itemCount = contents.size();
		setChanged();
		if (level != null) {
			level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
		}
	}

	public UUID getGraveId() {
		return graveId;
	}

	/** Colocada a mano (sin muerte detrás). Usa el nombre porque es lo que también llega al cliente. */
	public boolean isDecorative() {
		return ownerName.isEmpty();
	}

	/** El dueño de la tumba; el UUID también se sincroniza para que el cliente prediga el picado. */
	public boolean isOwner(Player player) {
		return owner != null && owner.equals(player.getUUID());
	}

	public String getOwnerName() {
		return ownerName;
	}

	public long getDeathMillis() {
		return deathMillis;
	}

	public long getDeathDay() {
		return deathDay;
	}

	@Nullable
	public Component getDeathCause() {
		return deathCause;
	}

	public int getItemCount() {
		return itemCount;
	}

	public boolean canOpen(Player player) {
		GravestoneConfig cfg = GravestoneConfig.get();
		if (!cfg.protectedByOwner || owner == null || owner.equals(player.getUUID())) {
			return true;
		}
		if (cfg.opsCanOpen && player.hasPermissions(2)) {
			return true;
		}
		return cfg.unlockAfterMinutes > 0
				&& level != null
				&& level.getGameTime() - deathTime >= cfg.unlockAfterMinutes * 60L * 20L;
	}

	/** Devuelve cada objeto a su ranura; lo que no cabe va al inventario y, si no, al suelo. */
	public void restoreTo(ServerPlayer player) {
		for (StoredEntry entry : entries) {
			SlotProvider provider = ProviderRegistry.get(entry.providerId());
			boolean placed = false;
			if (provider != null) {
				try {
					placed = provider.restore(player, entry);
				} catch (Throwable t) {
					ChaosGravestone.LOGGER.error("Fallo al restaurar con {}", entry.providerId(), t);
				}
			}
			if (!placed) {
				giveOrDrop(player, entry.stack().copy());
			}
		}
		entries.clear();
		itemCount = 0;
		GravestoneCompass.removeFrom(player, graveId);
		setChanged();
	}

	/** Suelta todo el contenido en el mundo (lápida rota o provider ausente). */
	public void dropAll() {
		if (level == null || level.isClientSide) {
			return;
		}
		for (StoredEntry entry : entries) {
			Containers.dropItemStack(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
					worldPosition.getZ() + 0.5, entry.stack().copy());
		}
		entries.clear();
		itemCount = 0;
	}

	private static void giveOrDrop(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack) && !stack.isEmpty()) {
			player.drop(stack, false);
		}
	}

	/** Datos que se graban en la lápida; los comparten el guardado y la sincronización. */
	private void saveDisplay(CompoundTag tag, HolderLookup.Provider registries) {
		if (owner != null) {
			tag.putUUID("owner", owner);
		}
		tag.putString("ownerName", ownerName);
		tag.putLong("deathMillis", deathMillis);
		tag.putLong("deathDay", deathDay);
		tag.putInt("itemCount", itemCount);
		if (deathCause != null) {
			tag.putString("deathCause", Component.Serializer.toJson(deathCause, registries));
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.saveAdditional(tag, registries);
		saveDisplay(tag, registries);
		tag.putUUID("graveId", graveId);
		tag.putLong("deathTime", deathTime);
		ListTag list = new ListTag();
		for (StoredEntry entry : entries) {
			list.add(entry.save(registries));
		}
		tag.put("entries", list);
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
		super.loadAdditional(tag, registries);
		ownerName = tag.getString("ownerName");
		deathMillis = tag.getLong("deathMillis");
		deathDay = tag.getLong("deathDay");
		deathCause = tag.contains("deathCause")
				? Component.Serializer.fromJson(tag.getString("deathCause"), registries)
				: null;
		if (tag.hasUUID("graveId")) {
			graveId = tag.getUUID("graveId");
		}
		owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
		deathTime = tag.getLong("deathTime");
		entries.clear();
		for (Tag t : tag.getList("entries", Tag.TAG_COMPOUND)) {
			StoredEntry entry = StoredEntry.load((CompoundTag) t, registries);
			if (!entry.stack().isEmpty()) {
				entries.add(entry);
			}
		}
		itemCount = tag.contains("itemCount") ? tag.getInt("itemCount") : entries.size();
	}

	/** Al cliente solo le llegan los datos del grabado, nunca el inventario. */
	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		saveDisplay(tag, registries);
		return tag;
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
}

package com.chaos.gravestone;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.network.TotemSkullPayload;

import com.chaos.gravestone.platform.Services;
import com.chaos.gravestone.storage.PendingCompassHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.phys.Vec3;

/** Brújula que apunta a una lápida; guía con partículas cuando se lleva en la mano. */
public final class GravestoneCompass {

	private static final String TAG = "chaosgravestone_grave";
	/** Hasta dónde llega el rastro de calaveras desde el jugador, en bloques. */
	private static final double TRAIL_LENGTH = 8.0;

	public record GraveRef(UUID id, GlobalPos pos) {}

	private GravestoneCompass() {}

	/**
	 * Al reaparecer: entrega las brújulas pendientes. Lo llama el evento de cada cargador.
	 * Las pendientes se guardan en los datos del jugador (ver ServerPlayerMixin), así que
	 * sobreviven a salir del mundo o reiniciar el servidor desde la pantalla de muerte.
	 */
	public static void onRespawn(ServerPlayer player) {
		List<ItemStack> pending = pending(player);
		if (pending.isEmpty()) {
			return;
		}
		List<ItemStack> stacks = new ArrayList<>(pending);
		pending.clear();
		stacks.forEach(stack -> give(player, stack));
	}

	public static ItemStack create(GraveRef ref) {
		ItemStack stack = new ItemStack(ModBlocks.GRAVE_COMPASS);
		stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(ref.pos()), false));
		CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putUUID(TAG, ref.id()));
		return stack;
	}

	/**
	 * Al inventario; si está lleno, al suelo a sus pies. Se crea la entidad a mano porque
	 * {@code player.drop} haría desaparecer la brújula (ver {@link #vanishOnToss}).
	 */
	public static void give(ServerPlayer player, ItemStack stack) {
		if (!player.getInventory().add(stack) && !stack.isEmpty()) {
			ItemEntity item = new ItemEntity(player.level(), player.getX(), player.getY(), player.getZ(), stack);
			item.setNoPickUpDelay();
			player.level().addFreshEntity(item);
		}
	}

	/** Brújula nueva para una lápida recién creada. */
	public static void addPending(ServerPlayer player, GraveRef ref) {
		addPending(player, create(ref));
	}

	/** Brújula que llevaba el jugador al morir; se le devuelve igual al reaparecer. */
	public static void addPending(ServerPlayer player, ItemStack stack) {
		pending(player).add(stack);
	}

	private static List<ItemStack> pending(ServerPlayer player) {
		return ((PendingCompassHolder) player).chaosgravestone$pendingCompasses();
	}

	/**
	 * Al tirar una brújula vinculada (Q, arrastrarla fuera del inventario...) se desvanece en
	 * vez de caer: solo existe para su dueño. {@code true} si se ha hecho desaparecer.
	 */
	public static boolean vanishOnToss(ServerPlayer player, ItemStack stack) {
		if (read(stack).isEmpty()) {
			return false;
		}
		ServerLevel level = player.serverLevel();
		level.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getEyeY() - 0.3, player.getZ(),
				8, 0.15, 0.15, 0.15, 0.01);
		level.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
		player.displayClientMessage(Component.translatable("message.chaosgravestone.compass_vanished"), true);
		return true;
	}

	/** Lee la lápida a la que apunta esta brújula (normal o de ender), si lo es. */
	public static Optional<GraveRef> read(ItemStack stack) {
		// Items.COMPASS: brújulas de versiones anteriores del mod, para que también se limpien.
		if (!stack.is(ModBlocks.GRAVE_COMPASS) && !stack.is(ModBlocks.ENDER_GRAVE_COMPASS)
				&& !stack.is(Items.COMPASS)) {
			return Optional.empty();
		}
		CustomData data = stack.get(DataComponents.CUSTOM_DATA);
		LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
		if (data == null || tracker == null || tracker.target().isEmpty()) {
			return Optional.empty();
		}
		CompoundTag tag = data.copyTag();
		if (!tag.hasUUID(TAG)) {
			return Optional.empty();
		}
		return Optional.of(new GraveRef(tag.getUUID(TAG), tracker.target().get()));
	}

	/** Quita del inventario todas las brújulas de esa lápida. */
	public static void removeFrom(ServerPlayer player, UUID graveId) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			if (read(inv.getItem(i)).filter(r -> r.id().equals(graveId)).isPresent()) {
				inv.setItem(i, ItemStack.EMPTY);
			}
		}
	}

	/**
	 * Lleva al jugador delante de la lápida, mirándola (también entre dimensiones), con la
	 * animación del tótem con calavera morada. {@code false} si la lápida ya no existe.
	 */
	public static boolean teleportToGrave(ServerPlayer player, GraveRef ref) {
		ServerLevel level = player.server.getLevel(ref.pos().dimension());
		BlockPos pos = ref.pos().pos();
		if (level == null || !(level.getBlockState(pos).getBlock() instanceof GravestoneBlock)) {
			player.displayClientMessage(Component.translatable("message.chaosgravestone.grave_gone"), true);
			removeFrom(player, ref.id());
			return false;
		}
		Direction facing = level.getBlockState(pos).getValue(GravestoneBlock.FACING);
		BlockPos dest = findSafeSpot(level, pos, facing);
		// Mirando hacia la lápida.
		double dx = pos.getX() - dest.getX();
		double dz = pos.getZ() - dest.getZ();
		float yaw = dx == 0 && dz == 0 ? facing.getOpposite().toYRot()
				: (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);

		ServerLevel from = player.serverLevel();
		from.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(),
				40, 0.4, 0.8, 0.4, 0.05);
		from.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

		player.teleportTo(level, dest.getX() + 0.5, dest.getY(), dest.getZ() + 0.5, yaw, 10.0F);
		player.resetFallDistance();

		level.sendParticles(ParticleTypes.REVERSE_PORTAL, dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5,
				60, 0.5, 1.0, 0.5, 0.08);
		level.sendParticles(ParticleTypes.WITCH, dest.getX() + 0.5, dest.getY() + 1, dest.getZ() + 0.5,
				30, 0.6, 0.8, 0.6, 0.1);
		level.playSound(null, dest, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8F, 1.2F);
		Services.PLATFORM.sendToPlayer(player, new TotemSkullPayload());
		return true;
	}

	/**
	 * Dónde dejar al jugador: delante de la lápida si es seguro; si no, el sitio seguro más cercano
	 * (radio 2); y si no hay ninguno, de pie encima de la lápida.
	 */
	private static BlockPos findSafeSpot(ServerLevel level, BlockPos grave, Direction facing) {
		BlockPos front = grave.relative(facing);
		if (isSafe(level, front)) {
			return front;
		}
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (BlockPos p : BlockPos.betweenClosed(grave.offset(-2, -1, -2), grave.offset(2, 1, 2))) {
			if (!p.equals(grave) && isSafe(level, p)) {
				double d = p.distSqr(front);
				if (d < bestDist) {
					bestDist = d;
					best = p.immutable();
				}
			}
		}
		return best != null ? best : grave.above();
	}

	/** Pies y cabeza libres y sin fluidos; suelo firme debajo que no queme (lava, fuego, magma). */
	private static boolean isSafe(ServerLevel level, BlockPos feet) {
		BlockPos head = feet.above();
		BlockPos below = feet.below();
		BlockState floor = level.getBlockState(below);
		return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
				&& level.getBlockState(head).getCollisionShape(level, head).isEmpty()
				&& level.getFluidState(feet).isEmpty()
				&& level.getFluidState(head).isEmpty()
				&& !floor.getCollisionShape(level, below).isEmpty()
				&& level.getFluidState(below).isEmpty()
				&& !floor.is(BlockTags.FIRE)
				&& !floor.is(BlockTags.CAMPFIRES)
				&& !floor.is(Blocks.MAGMA_BLOCK)
				&& !hasLavaAround(level, feet);
	}

	private static boolean hasLavaAround(ServerLevel level, BlockPos feet) {
		for (Direction d : Direction.Plane.HORIZONTAL) {
			if (level.getFluidState(feet.relative(d)).is(FluidTags.LAVA)
					|| level.getFluidState(feet.below().relative(d)).is(FluidTags.LAVA)) {
				return true;
			}
		}
		return false;
	}

	/** Al final de cada tick del servidor (evento de cada cargador): limpieza y partículas guía. */
	public static void tick(MinecraftServer server) {
		int time = server.getTickCount();
		if (time % 4 != 0) {
			return;
		}
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			if (time % 20 == 0) {
				cleanStale(server, player);
			}
			guide(player, time);
		}
	}

	/** Elimina brújulas cuya lápida ya no existe (rota, abierta por otro, etc.). */
	private static void cleanStale(MinecraftServer server, ServerPlayer player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			Optional<GraveRef> ref = read(inv.getItem(i));
			if (ref.isEmpty()) {
				continue;
			}
			ServerLevel level = server.getLevel(ref.get().pos().dimension());
			BlockPos pos = ref.get().pos().pos();
			if (level != null && level.isLoaded(pos) && !(level.getBlockState(pos).getBlock() instanceof GravestoneBlock)) {
				inv.setItem(i, ItemStack.EMPTY);
			}
		}
	}

	private static void guide(ServerPlayer player, int time) {
		Optional<GraveRef> ref = read(player.getMainHandItem());
		if (ref.isEmpty()) {
			ref = read(player.getOffhandItem());
		}
		if (ref.isEmpty() || !ref.get().pos().dimension().equals(player.level().dimension())) {
			return;
		}
		ServerLevel level = player.serverLevel();
		Vec3 target = Vec3.atCenterOf(ref.get().pos().pos());
		Vec3 from = player.position().add(0, 1.0, 0);
		Vec3 delta = target.subtract(from);
		double dist = delta.length();
		if (dist < 1.5) {
			return;
		}
		Vec3 dir = delta.normalize();

		// Rastro de calaveras hacia la lápida, hasta 8 bloques de ti y espaciadas: una tanda
		// cada 12 ticks (0,6 s), una calavera cada 2,5 bloques. Cada una sube sola (SkullParticle).
		if (time % 12 == 0) {
			double shift = (time % 36) / 36.0 * 2.5;
			for (double d = 1.5 + shift; d <= Math.min(dist, TRAIL_LENGTH); d += 2.5) {
				Vec3 p = from.add(dir.scale(d));
				level.sendParticles(player, ModParticles.SKULL, false, p.x, p.y, p.z, 1, 0.08, 0.08, 0.08, 0);
			}
		}
		// Cerca de la lápida, de vez en cuando brota encima una calavera dorada (sube como mucho
		// hasta 4 bloques sobre la base, ver SkullParticle).
		if (dist < 48 && time % 16 == 0) {
			BlockPos grave = ref.get().pos().pos();
			level.sendParticles(player, ModParticles.GOLD_SKULL, false, grave.getX() + 0.5, grave.getY() + 1.05,
					grave.getZ() + 0.5, 1, 0.15, 0.0, 0.15, 0);
		}
	}
}

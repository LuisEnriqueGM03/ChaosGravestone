package com.chaos.gravestone.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.ChaosGravestone;
import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.chaos.gravestone.config.GravestoneConfig;
import com.chaos.gravestone.platform.Services;
import com.chaos.gravestone.storage.PendingCompassHolder;
import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * Autoprueba de lógica (parte de SelfTest): muertes y reapariciones reales en el servidor integrado.
 * Cada comprobación sale en el log como "[logictest] ... -> OK/FALLO".
 */
public final class LogicTest {

	/** Ranuras de un mod de accesorios (Trinkets/Accessories); las añade el cargador si están. */
	public interface CompatHook {
		String name();

		/** Pone el objeto en una ranura de accesorio y devuelve su clave, o null si no hay ranuras. */
		@Nullable
		String equip(ServerPlayer player, ItemStack stack);

		ItemStack get(ServerPlayer player, String slot);

		/** Vacía todas las ranuras de accesorio. */
		void clear(ServerPlayer player);
	}

	public static final List<CompatHook> compat = new ArrayList<>();

	/** El listener de muerte de cada cargador cancela la siguiente muerte (simula un mod de revivir). */
	public static boolean cancelNextDeath;

	private static int passed;
	private static int failed;

	private LogicTest() {}

	public static void run(ServerPlayer player, BlockPos origin, UUID farGraveId, BlockPos farPos) {
		passed = 0;
		failed = 0;
		GravestoneConfig cfg = GravestoneConfig.get();
		boolean oldStore = cfg.storeExperience;
		int oldPercent = cfg.experiencePercent;
		try {
			cfg.storeExperience = true;
			cfg.experiencePercent = 75;
			runAll(player, origin, farGraveId, farPos);
		} catch (Throwable t) {
			failed++;
			ChaosGravestone.LOGGER.error("[logictest] excepción -> FALLO", t);
		} finally {
			cfg.storeExperience = oldStore;
			cfg.experiencePercent = oldPercent;
			cancelNextDeath = false;
		}
		ChaosGravestone.LOGGER.info("[logictest] resumen ({}{}): {} OK, {} FALLO", Services.PLATFORM.name(),
				compat.stream().map(h -> ", " + h.name()).reduce("", String::concat), passed, failed);
	}

	private static void runAll(ServerPlayer p, BlockPos origin, UUID farGraveId, BlockPos farPos) {
		ServerLevel level = p.serverLevel();
		BlockPos spot = origin.west(7);
		// Suelo despejado para que la lápida caiga donde se muere.
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				level.setBlockAndUpdate(spot.offset(dx, -1, dz), Blocks.GRASS_BLOCK.defaultBlockState());
				for (int dy = 0; dy <= 3; dy++) {
					level.setBlockAndUpdate(spot.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
				}
			}
		}

		// --- 1. Muerte con objetos, XP y brújula de ender encima ---
		prepare(p, spot);
		p.getInventory().setItem(0, new ItemStack(Items.DIAMOND_SWORD));
		p.getInventory().setItem(1, new ItemStack(Items.BREAD, 16));
		p.getInventory().setItem(39, new ItemStack(Items.IRON_HELMET));
		// Igual que la receta: la brújula vinculada pasa a ser de ender conservando sus datos.
		ItemStack ender = GravestoneCompass.create(
				new GravestoneCompass.GraveRef(farGraveId, GlobalPos.of(level.dimension(), farPos)))
				.transmuteCopy(ModBlocks.ENDER_GRAVE_COMPASS, 1);
		p.getInventory().setItem(5, ender);
		// Un objeto distinto en cada mod de accesorios.
		Item[] accessoryItems = {Items.DIAMOND, Items.EMERALD};
		List<String> compatSlots = new ArrayList<>();
		for (int i = 0; i < compat.size(); i++) {
			String slot = compat.get(i).equip(p, new ItemStack(accessoryItems[i]));
			check("compat: hay ranura de " + compat.get(i).name(), slot != null);
			compatSlots.add(slot);
		}
		p.giveExperienceLevels(30);
		check("xp inicial nivel 30", p.experienceLevel == 30);
		kill(p);

		check("muerte 1: no caen orbes", orbXp(level, spot) == 0);
		check("muerte 1: no caen objetos", items(level, spot) == 0);
		List<ItemStack> pending = pending(p);
		check("muerte 1: 2 brújulas pendientes (ender + nueva) [" + pending.size() + "]", pending.size() == 2);
		check("muerte 1: la de ender está pendiente",
				pending.stream().anyMatch(s -> s.is(ModBlocks.ENDER_GRAVE_COMPASS)));
		Optional<GravestoneCompass.GraveRef> newRef = pending.stream().filter(s -> s.is(ModBlocks.GRAVE_COMPASS))
				.findFirst().flatMap(GravestoneCompass::read);
		check("muerte 1: brújula nueva vinculada", newRef.isPresent());
		GravestoneBlockEntity grave = newRef.map(r -> level.getBlockEntity(r.pos().pos()))
				.filter(GravestoneBlockEntity.class::isInstance).map(GravestoneBlockEntity.class::cast).orElse(null);
		check("muerte 1: lápida creada", grave != null);
		if (grave == null) {
			return;
		}
		BlockPos gravePos = grave.getBlockPos();
		int expectedItems = 3 + (int) compatSlots.stream().filter(java.util.Objects::nonNull).count();
		check("muerte 1: objetos en la lápida = " + expectedItems + " [" + grave.getItemCount() + "]",
				grave.getItemCount() == expectedItems);
		// Nivel 30 = 1395 puntos; 75 % = 1046.
		check("muerte 1: XP guardada 1046 [" + grave.getStoredXp() + "]", grave.getStoredXp() == 1046);
		for (int i = 0; i < compat.size(); i++) {
			if (compatSlots.get(i) != null) {
				check("muerte 1: ranura de " + compat.get(i).name() + " vaciada",
						compat.get(i).get(p, compatSlots.get(i)).isEmpty());
			}
		}

		// --- 2. Persistencia: el NBT del jugador muerto (salir del mundo / reiniciar) conserva las brújulas ---
		CompoundTag saved = p.saveWithoutId(new CompoundTag());
		ServerPlayer reloaded = new ServerPlayer(level.getServer(), level,
				new GameProfile(UUID.randomUUID(), "LogicTestReload"), ClientInformation.createDefault());
		reloaded.load(saved);
		List<ItemStack> reloadedPending = pending(reloaded);
		check("persistencia: 2 brújulas tras recargar NBT [" + reloadedPending.size() + "]",
				reloadedPending.size() == 2
						&& reloadedPending.stream().anyMatch(s -> s.is(ModBlocks.ENDER_GRAVE_COMPASS)
								&& GravestoneCompass.read(s).filter(r -> r.id().equals(farGraveId)).isPresent()));

		// --- 3. Reaparecer: llegan las dos brújulas ---
		p = respawn(p);
		check("reaparecer: brújula de ender devuelta (vinculada a la lápida lejana)",
				findCompass(p, farGraveId) >= 0 && p.getInventory().getItem(findCompass(p, farGraveId))
						.is(ModBlocks.ENDER_GRAVE_COMPASS));
		check("reaparecer: brújula de la nueva lápida", findCompass(p, grave.getGraveId()) >= 0);
		check("reaparecer: no quedan pendientes", pending(p).isEmpty());

		// --- 4. Abrir la lápida: cada cosa a su ranura y la XP ---
		grave.restoreTo(p);
		level.removeBlock(gravePos, false);
		check("abrir: espada en ranura 0", p.getInventory().getItem(0).is(Items.DIAMOND_SWORD));
		check("abrir: pan en ranura 1 (la brújula de ender que ocupaba la ranura se aparta)",
				p.getInventory().getItem(1).is(Items.BREAD));
		check("abrir: casco en ranura 39", p.getInventory().getItem(39).is(Items.IRON_HELMET));
		check("abrir: XP devuelta 1046 [" + p.totalExperience + ", nivel " + p.experienceLevel + "]",
				p.totalExperience == 1046);
		check("abrir: su brújula desaparece", findCompass(p, grave.getGraveId()) < 0);
		check("abrir: la de ender (otra lápida) se queda", findCompass(p, farGraveId) >= 0);
		for (int i = 0; i < compat.size(); i++) {
			String slot = compatSlots.get(i);
			if (slot != null) {
				ItemStack back = compat.get(i).get(p, slot);
				check("abrir: accesorio de " + compat.get(i).name() + " vuelve a su ranura " + slot + " [" + back
						+ ", en inventario=" + p.getInventory().countItem(accessoryItems[i]) + "]",
						back.is(accessoryItems[i]) && back.getCount() == 1);
			}
		}

		// --- 5. Tirar la brújula (Q): desaparece ---
		int enderSlot = findCompass(p, farGraveId);
		if (enderSlot >= 0 && enderSlot < 9) {
			p.getInventory().selected = enderSlot;
			p.drop(true);
		} else {
			ItemStack stack = p.getInventory().removeItemNoUpdate(enderSlot);
			p.drop(stack, false, true);
		}
		check("tirar: la brújula ya no está en el inventario", findCompass(p, farGraveId) < 0);
		check("tirar: no aparece en el suelo", level.getEntitiesOfClass(ItemEntity.class,
				new AABB(p.blockPosition()).inflate(4), e -> GravestoneCompass.read(e.getItem()).isPresent()).isEmpty());

		// --- 6. storeExperience=false: XP vanilla al suelo ---
		GravestoneConfig.get().storeExperience = false;
		prepare(p, spot);
		p.getInventory().setItem(0, new ItemStack(Items.STONE_SWORD));
		p.giveExperienceLevels(10);
		kill(p);
		int vanillaOrbs = orbXp(level, spot);
		GravestoneBlockEntity grave2 = pendingGrave(level, p);
		check("config off: orbes vanilla 70 [" + vanillaOrbs + "]", vanillaOrbs == 70);
		check("config off: lápida sin XP", grave2 != null && grave2.getStoredXp() == 0 && grave2.getItemCount() == 1);
		p = respawn(p);
		if (grave2 != null) {
			grave2.restoreTo(p);
			level.removeBlock(grave2.getBlockPos(), false);
		}
		GravestoneConfig.get().storeExperience = true;

		// --- 7. Solo XP, sin objetos: también hay lápida; al romperla caen los orbes ---
		prepare(p, spot);
		p.giveExperienceLevels(5);
		kill(p);
		GravestoneBlockEntity grave3 = pendingGrave(level, p);
		// Nivel 5 = 55 puntos; 75 % = 41.
		check("solo XP: lápida con 0 objetos y 41 XP",
				grave3 != null && grave3.getItemCount() == 0 && grave3.getStoredXp() == 41);
		p = respawn(p);
		if (grave3 != null) {
			BlockPos pos3 = grave3.getBlockPos();
			clearDrops(level, pos3);
			level.destroyBlock(pos3, false);
			int orbs = orbXp(level, pos3);
			check("romper: caen 41 de XP en orbes [" + orbs + "]", orbs == 41);
		}

		// --- 8. Otro mod cancela la muerte: el inventario sigue intacto y no hay lápida ---
		prepare(p, spot);
		p.getInventory().setItem(0, new ItemStack(Items.GOLDEN_SWORD));
		p.giveExperienceLevels(3);
		cancelNextDeath = true;
		kill(p);
		cancelNextDeath = false;
		check("muerte cancelada: sigue vivo", p.isAlive() && p.getHealth() > 0);
		check("muerte cancelada: conserva la espada", p.getInventory().getItem(0).is(Items.GOLDEN_SWORD));
		check("muerte cancelada: conserva la XP", p.experienceLevel == 3);
		check("muerte cancelada: sin brújula pendiente", pending(p).isEmpty());

		// --- 9. Wither y Ender Dragon ---
		BlockState graveState = ModBlocks.get(com.chaos.gravestone.GravestoneVariant.CLASSIC).defaultBlockState();
		check("wither: no puede romper la lápida", !WitherBoss.canDestroy(graveState));
		check("dragón: lápida en dragon_immune", graveState.is(BlockTags.DRAGON_IMMUNE));
		boolean allVariants = true;
		for (com.chaos.gravestone.GravestoneVariant v : com.chaos.gravestone.GravestoneVariant.values()) {
			BlockState s = ModBlocks.get(v).defaultBlockState();
			allVariants &= s.is(BlockTags.WITHER_IMMUNE) && s.is(BlockTags.DRAGON_IMMUNE);
		}
		check("todas las variantes inmunes al Wither y al Dragón", allVariants);
	}

	private static void prepare(ServerPlayer p, BlockPos spot) {
		p.setGameMode(GameType.SURVIVAL);
		p.getInventory().clearContent();
		compat.forEach(h -> h.clear(p));
		p.setExperienceLevels(0);
		p.setExperiencePoints(0);
		p.totalExperience = 0;
		p.teleportTo(p.serverLevel(), spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0F, 0.0F);
		clearDrops(p.serverLevel(), spot);
	}

	private static void kill(ServerPlayer p) {
		p.invulnerableTime = 0;
		p.hurt(p.damageSources().genericKill(), Float.MAX_VALUE);
	}

	/** Como el paquete de reaparecer: la conexión pasa a apuntar al jugador nuevo. */
	static ServerPlayer respawn(ServerPlayer p) {
		ServerPlayer fresh = p.server.getPlayerList().respawn(p, false, Entity.RemovalReason.KILLED);
		fresh.connection.player = fresh;
		return fresh;
	}

	private static List<ItemStack> pending(ServerPlayer p) {
		return new ArrayList<>(((PendingCompassHolder) p).chaosgravestone$pendingCompasses());
	}

	@Nullable
	private static GravestoneBlockEntity pendingGrave(ServerLevel level, ServerPlayer p) {
		return pending(p).stream().filter(s -> s.is(ModBlocks.GRAVE_COMPASS)).findFirst()
				.flatMap(GravestoneCompass::read).map(r -> level.getBlockEntity(r.pos().pos()))
				.filter(GravestoneBlockEntity.class::isInstance).map(GravestoneBlockEntity.class::cast).orElse(null);
	}

	private static int findCompass(ServerPlayer p, UUID graveId) {
		for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
			if (GravestoneCompass.read(p.getInventory().getItem(i)).filter(r -> r.id().equals(graveId)).isPresent()) {
				return i;
			}
		}
		return -1;
	}

	private static void clearDrops(ServerLevel level, BlockPos pos) {
		AABB box = new AABB(pos).inflate(6);
		level.getEntitiesOfClass(ExperienceOrb.class, box).forEach(Entity::discard);
		level.getEntitiesOfClass(ItemEntity.class, box).forEach(Entity::discard);
	}

	private static int orbXp(ServerLevel level, BlockPos pos) {
		return level.getEntitiesOfClass(ExperienceOrb.class, new AABB(pos).inflate(6)).stream()
				.mapToInt(ExperienceOrb::getValue).sum();
	}

	private static int items(ServerLevel level, BlockPos pos) {
		return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(6)).size();
	}

	private static void check(String name, boolean ok) {
		if (ok) {
			passed++;
		} else {
			failed++;
		}
		ChaosGravestone.LOGGER.info("[logictest] {} -> {}", name, ok ? "OK" : "FALLO");
	}
}

package com.chaos.gravestone.client;

import java.util.List;
import java.util.UUID;

import com.chaos.gravestone.ChaosGravestone;
import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.GravestoneVariant;
import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.chaos.gravestone.storage.StoredEntry;

import java.util.function.Consumer;

import com.chaos.gravestone.platform.Services;
import com.mojang.authlib.GameProfile;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Autoprueba visual, solo en desarrollo: ./gradlew :fabric:runSelftest o :neoforge:runSelftest.
 * Da la brújula, gira la cámara 360° en pasos de 45° con capturas, fotografía las lápidas de día y de noche,
 * y comprueba romper la tumba, la receta de ender y el teletransporte.
 * Las capturas quedan en run/screenshots/selftest_*.png (de cada cargador).
 */
public final class SelfTest {

	private static final int START = 80;
	private static final int STEP = 30;
	private static final int STEPS = 8;

	/** Hueco de inventario (fuera de la barra) para la brújula de la fila; no choca con la mano. */
	private static final int SPARE_SLOT = 20;

	private static int ticks = -1;
	private static BlockPos breakPos;
	private static BlockPos farPos;
	private static UUID farGraveId;
	private static UUID breakGraveId;

	private SelfTest() {}

	public static boolean enabled() {
		return System.getProperty("chaosgravestone.selftest") != null;
	}

	/**
	 * Selecciona la pestaña del mod en la pantalla creativa. Cada cargador pagina las pestañas a su
	 * manera; si no lo rellena, se omiten las capturas de la pestaña.
	 */
	public static Consumer<CreativeModeInventoryScreen> tabSelector;

	/** Al final de cada tick del cliente (evento de cada cargador). */
	public static void tick(Minecraft mc) {
		if (mc.player == null || mc.level == null || mc.getSingleplayerServer() == null) {
			return;
		}
		mc.options.pauseOnLostFocus = false;
		ticks++;

		if (ticks == START) {
			setup(mc.getSingleplayerServer(), mc.player.getUUID());
		}

		// 8 orientaciones: la brújula apunta a una lápida 30 bloques al norte.
		int rotStart = START + 40;
		for (int i = 0; i < STEPS; i++) {
			float yaw = i * 45.0F;
			// Se fija cada tick: el ratón real movería la cámara entre medias.
			if (ticks >= rotStart + i * STEP && ticks < rotStart + (i + 1) * STEP) {
				look(mc, yaw, 0);
			}
			if (ticks == rotStart + i * STEP + STEP - 2) {
				shot(mc, "compass_yaw" + (int) yaw);
			}
		}

		// Fila de variantes (4 al sur): de día y de noche.
		int graveStart = rotStart + STEPS * STEP;
		// Antes, a la mano la brújula de la lápida clásica de la fila, para ver sus calaveras doradas.
		if (ticks == graveStart - 40) {
			MinecraftServer server = mc.getSingleplayerServer();
			UUID id = mc.player.getUUID();
			server.execute(() -> {
				ServerPlayer p = server.getPlayerList().getPlayer(id);
				var inv = p.getInventory();
				ItemStack held = inv.getItem(inv.selected);
				inv.setItem(inv.selected, inv.getItem(SPARE_SLOT));
				inv.setItem(SPARE_SLOT, held);
			});
		}
		if (ticks >= graveStart && ticks < graveStart + 64) {
			look(mc, 0, 26);
		}
		if (ticks == graveStart + 20) {
			shot(mc, "variants_day");
			// Descripción de la brújula que se lleva en la mano (coordenadas, dimensión y texto).
			ItemStack held = mc.player.getMainHandItem();
			for (Component line : held.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player,
					TooltipFlag.Default.NORMAL)) {
				ChaosGravestone.LOGGER.info("[selftest] tooltip: {}", line.getString());
			}
			mc.getSingleplayerServer().execute(() -> mc.getSingleplayerServer().overworld().setDayTime(18000));
		}
		if (ticks == graveStart + 50) {
			shot(mc, "variants_night");
		}
		// El dueño rompe su tumba: todo al suelo, nada al inventario, y pierde la brújula.
		if (ticks == graveStart + 60) {
			MinecraftServer server = mc.getSingleplayerServer();
			UUID id = mc.player.getUUID();
			server.execute(() -> breakTest(server.getPlayerList().getPlayer(id)));
		}

		// Receta de ender + carga completada: teletransporte a la lápida lejana con el tótem morado.
		if (ticks == graveStart + 65) {
			MinecraftServer server = mc.getSingleplayerServer();
			UUID id = mc.player.getUUID();
			server.execute(() -> enderTest(server.getPlayerList().getPlayer(id)));
		}
		if (ticks == graveStart + 73) {
			shot(mc, "teleport_totem");
		}
		if (ticks == graveStart + 110) {
			shot(mc, "teleport_arrival");
		}

		// Pestaña creativa: dos capturas en segundos distintos para ver rotar el icono.
		if (ticks == graveStart + 115) {
			MinecraftServer server = mc.getSingleplayerServer();
			UUID id = mc.player.getUUID();
			server.execute(() -> server.getPlayerList().getPlayer(id).setGameMode(GameType.CREATIVE));
		}
		if (ticks == graveStart + 125) {
			CreativeModeInventoryScreen screen = new CreativeModeInventoryScreen(mc.player,
					mc.player.connection.enabledFeatures(), mc.options.operatorItemsTab().get());
			mc.setScreen(screen);
			if (tabSelector != null) {
				tabSelector.accept(screen);
			} else {
				ChaosGravestone.LOGGER.info("[selftest] pestaña creativa: sin selector en {}, se omite",
						Services.PLATFORM.name());
			}
		}
		if (ticks == graveStart + 140) {
			shot(mc, "creative_tab_a");
		}
		if (ticks == graveStart + 165) {
			shot(mc, "creative_tab_b");
		}
		if (ticks == graveStart + 175) {
			ChaosGravestone.LOGGER.info("[selftest] terminado ({})", Services.PLATFORM.name());
			mc.stop();
		}
	}

	private static void enderTest(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		ItemStack pearl = new ItemStack(Items.ENDER_PEARL);
		ItemStack compass = GravestoneCompass.create(
				new GravestoneCompass.GraveRef(farGraveId, GlobalPos.of(level.dimension(), farPos)));
		CraftingInput good = CraftingInput.of(3, 3, List.of(
				ItemStack.EMPTY, pearl, ItemStack.EMPTY,
				pearl, compass, pearl,
				ItemStack.EMPTY, pearl, ItemStack.EMPTY));
		CraftingInput bad = CraftingInput.of(3, 3, List.of(
				pearl, pearl, ItemStack.EMPTY,
				pearl, compass, ItemStack.EMPTY,
				ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY));
		var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, good, level);
		boolean badRejected = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, bad, level).isEmpty();
		ItemStack result = recipe.map(r -> r.value().assemble(good, level.registryAccess())).orElse(ItemStack.EMPTY);
		boolean linked = result.is(ModBlocks.ENDER_GRAVE_COMPASS) && GravestoneCompass.read(result)
				.filter(r -> r.id().equals(farGraveId)).isPresent();

		// Equivale a mantener clic derecho los 5 s completos.
		player.setItemInHand(InteractionHand.MAIN_HAND, result);
		result.finishUsingItem(level, player);
		double distance = Math.sqrt(player.blockPosition().distSqr(farPos));
		BlockPos feet = player.blockPosition();
		boolean safe = level.getFluidState(feet).isEmpty()
				&& !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty();
		boolean ok = recipe.isPresent() && badRejected && linked && distance <= 3.0 && safe;
		ChaosGravestone.LOGGER.info("[selftest] ender: recetaOk={} patrónMaloRechazado={} vinculada={} "
				+ "distanciaALápida={} sueloSeguro={} -> {}", recipe.isPresent(), badRejected, linked,
				String.format("%.1f", distance), safe, ok ? "OK" : "FALLO");
	}

	private static void breakTest(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		player.setGameMode(GameType.SURVIVAL);
		// Restos de pruebas anteriores (tumbas sustituidas al aplanar) que falsearían el recuento.
		level.getEntitiesOfClass(ItemEntity.class, new AABB(breakPos).inflate(4)).forEach(ItemEntity::discard);
		BlockState state = level.getBlockState(breakPos);
		float ownerProgress = state.getDestroyProgress(player, level, breakPos);
		// Otro jugador cualquiera (no conectado): basta para calcular su progreso de picado.
		ServerPlayer other = new ServerPlayer(level.getServer(), level,
				new GameProfile(UUID.randomUUID(), "SelfTestOther"), ClientInformation.createDefault());
		float otherProgress = state.getDestroyProgress(other, level, breakPos);
		int swordsBefore = player.getInventory().countItem(Items.DIAMOND_SWORD);
		boolean broken = player.gameMode.destroyBlock(breakPos);
		// Suma de cantidades: dropItemStack reparte una pila en varias entidades. Esperado 1+32+1.
		int onGround = level.getEntitiesOfClass(ItemEntity.class, new AABB(breakPos).inflate(2)).stream()
				.mapToInt(e -> e.getItem().getCount()).sum();
		// Que no haya llegado al inventario al romperla (pudo recoger otra antes, de pruebas previas).
		boolean swordInInventory = player.getInventory().countItem(Items.DIAMOND_SWORD) > swordsBefore;
		boolean compassKept = false;
		for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
			compassKept |= GravestoneCompass.read(player.getInventory().getItem(i))
					.filter(r -> r.id().equals(breakGraveId)).isPresent();
		}
		boolean ok = ownerProgress > 0 && otherProgress == 0 && broken && onGround == 34 && !swordInInventory
				&& !compassKept;
		ChaosGravestone.LOGGER.info("[selftest] romper: progresoDueño={} progresoOtro={} rota={} itemsEnSuelo={} "
				+ "espadaEnInventario={} brújulaSigue={} -> {}", ownerProgress, otherProgress, broken, onGround,
				swordInInventory, compassKept, ok ? "OK" : "FALLO");
	}

	private static void setup(MinecraftServer server, UUID playerId) {
		server.execute(() -> {
			ServerPlayer player = server.getPlayerList().getPlayer(playerId);
			if (player == null) {
				return;
			}
			ServerLevel level = player.serverLevel();
			level.setDayTime(6000);
			level.setWeatherParameters(6000, 0, false, false);

			BlockPos feet = player.blockPosition();
			BlockPos far = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, feet.north(30));
			UUID farId = placeGrave(level, player, far, Direction.SOUTH, GravestoneVariant.CLASSIC);
			farGraveId = farId;
			farPos = far;
			// Explanada plana para la fila de variantes.
			for (int dx = -9; dx <= 9; dx++) {
				for (int dz = -1; dz <= 9; dz++) {
					BlockPos column = feet.offset(dx, 0, dz);
					level.setBlockAndUpdate(column.below(), Blocks.GRASS_BLOCK.defaultBlockState());
					for (int dy = 0; dy <= 6; dy++) {
						level.setBlockAndUpdate(column.above(dy), Blocks.AIR.defaultBlockState());
					}
				}
			}
			player.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
			// Las 7 variantes en fila, 4 bloques al sur, mirando al jugador.
			GravestoneVariant[] variants = GravestoneVariant.values();
			// Dos filas de 9 (a 4 y a 7 bloques al sur).
			for (int i = 0; i < variants.length; i++) {
				BlockPos pos = feet.south(i < 9 ? 4 : 7).east(4 - i % 9);
				UUID graveId = placeGrave(level, player, pos, Direction.NORTH, variants[i]);
				if (i == 0) {
					breakPos = pos;
					breakGraveId = graveId;
				}
			}
			player.getInventory().clearContent();
			player.getInventory().setItem(SPARE_SLOT, GravestoneCompass.create(
					new GravestoneCompass.GraveRef(breakGraveId, GlobalPos.of(level.dimension(), breakPos))));

			player.getInventory().setItem(player.getInventory().selected,
					GravestoneCompass.create(new GravestoneCompass.GraveRef(farId, GlobalPos.of(level.dimension(), far))));
			ChaosGravestone.LOGGER.info("[selftest] jugador en {}, lápida lejana en {}", feet, far);
		});
	}

	private static UUID placeGrave(ServerLevel level, ServerPlayer player, BlockPos pos, Direction facing,
			GravestoneVariant variant) {
		level.setBlockAndUpdate(pos, ModBlocks.get(variant).defaultBlockState().setValue(GravestoneBlock.FACING, facing));
		GravestoneBlockEntity grave = (GravestoneBlockEntity) level.getBlockEntity(pos);
		grave.init(player, List.of(
				new StoredEntry("vanilla", "slot#0", new ItemStack(Items.DIAMOND_SWORD)),
				new StoredEntry("vanilla", "slot#1", new ItemStack(Items.TORCH, 32)),
				new StoredEntry("vanilla", "slot#39", new ItemStack(Items.IRON_HELMET))),
				Component.translatable("death.attack.mob", player.getDisplayName(), Component.literal("Zombie")));
		return grave.getGraveId();
	}

	private static void look(Minecraft mc, float yaw, float pitch) {
		mc.player.setYRot(yaw);
		mc.player.yRotO = yaw;
		mc.player.setYHeadRot(yaw);
		mc.player.setXRot(pitch);
		mc.player.xRotO = pitch;
	}

	private static void shot(Minecraft mc, String name) {
		Screenshot.grab(mc.gameDirectory, "selftest_" + name + ".png", mc.getMainRenderTarget(),
				msg -> ChaosGravestone.LOGGER.info("[selftest] {}", msg.getString()));
	}
}

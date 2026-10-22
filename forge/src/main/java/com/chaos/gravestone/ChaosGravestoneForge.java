package com.chaos.gravestone;

import com.chaos.gravestone.client.ChaosGravestoneForgeClient;
import com.chaos.gravestone.compat.CosmeticArmorProvider;
import com.chaos.gravestone.compat.TravelersBackpackProvider;
import com.chaos.gravestone.storage.ProviderRegistry;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/** Entrada de Forge: registra todo en RegisterEvent y conecta los eventos con el código común. */
@Mod(ChaosGravestone.MOD_ID)
public class ChaosGravestoneForge {

	public ChaosGravestoneForge(FMLJavaModLoadingContext context) {
		IEventBus modBus = context.getModEventBus();
		ChaosGravestone.init();
		// Mods de ranuras (opcionales): cada clase solo se carga si su mod está. Curios no existe en Forge 1.21.1.
		if (ModList.get().isLoaded("travelersbackpack")) {
			ProviderRegistry.register(new TravelersBackpackProvider());
		}
		if (ModList.get().isLoaded("cosmeticarmorreworked")) {
			ProviderRegistry.register(new CosmeticArmorProvider());
		}
		ForgeNetwork.register();

		modBus.addListener(ChaosGravestoneForge::register);

		// La lápida se crea aquí, después de que cualquier otro mod haya podido cancelar la muerte.
		GravestoneManager.loaderHandlesDeath = true;
		MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingDeathEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GravestoneManager.onPlayerDeath(player);
			}
		});
		MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GravestoneCompass.onRespawn(player);
			}
		});
		MinecraftForge.EVENT_BUS.addListener((TickEvent.ServerTickEvent.Post event) ->
				GravestoneCompass.tick(event.getServer()));

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ChaosGravestoneForgeClient.init(modBus);
		}
	}

	/** Se dispara una vez por registro; cada llamada solo actúa sobre el suyo. */
	private static void register(RegisterEvent event) {
		event.register(Registries.BLOCK, helper -> ModBlocks.registerBlocks(helper::register));
		event.register(Registries.ITEM, helper -> ModBlocks.registerItems(helper::register));
		event.register(Registries.BLOCK_ENTITY_TYPE, helper -> ModBlocks.registerBlockEntities(helper::register));
		event.register(Registries.RECIPE_SERIALIZER, helper -> ModBlocks.registerRecipeSerializers(helper::register));
		event.register(Registries.CREATIVE_MODE_TAB, helper -> ModBlocks.registerCreativeTabs(helper::register));
		event.register(Registries.PARTICLE_TYPE, helper -> ModParticles.register(helper::register));
	}
}

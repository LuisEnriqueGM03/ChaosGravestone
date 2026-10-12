package com.chaos.gravestone;

import com.chaos.gravestone.client.ChaosGravestoneForgeClient;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegisterEvent;

/** Entrada de Forge: registra todo en RegisterEvent y conecta los eventos con el cÃ³digo comÃºn. */
@Mod(ChaosGravestone.MOD_ID)
public class ChaosGravestoneForge {

	public ChaosGravestoneForge() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
		ChaosGravestone.init();
		ForgeNetwork.register();

		modBus.addListener(EventPriority.NORMAL, false, RegisterEvent.class, ChaosGravestoneForge::register);

		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, PlayerEvent.PlayerRespawnEvent.class, event -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GravestoneCompass.onRespawn(player);
			}
		});
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.ServerTickEvent.class, event -> {
                if (event.phase == TickEvent.Phase.END) GravestoneCompass.tick(event.getServer());
            });

        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false,
            net.minecraftforge.event.level.BlockEvent.BreakEvent.class, event -> {
                if (event.getLevel().getBlockEntity(event.getPos()) instanceof com.chaos.gravestone.block.GravestoneBlockEntity grave
                        && !grave.isDecorative() && !grave.isOwner(event.getPlayer())) event.setCanceled(true);
            });

		if (FMLEnvironment.dist == Dist.CLIENT) {
			ChaosGravestoneForgeClient.init(modBus);
		}
	}

	/** Se dispara una vez por registro; cada llamada solo actÃºa sobre el suyo. */
	private static void register(RegisterEvent event) {
		event.register(Registries.BLOCK, helper -> ModBlocks.registerBlocks(helper::register));
		event.register(Registries.ITEM, helper -> ModBlocks.registerItems(helper::register));
		event.register(Registries.BLOCK_ENTITY_TYPE, helper -> ModBlocks.registerBlockEntities(helper::register));
		event.register(Registries.RECIPE_SERIALIZER, helper -> ModBlocks.registerRecipeSerializers(helper::register));
		event.register(Registries.CREATIVE_MODE_TAB, helper -> ModBlocks.registerCreativeTabs(helper::register));
		event.register(Registries.PARTICLE_TYPE, helper -> ModParticles.register(helper::register));
	}
}

package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/** Parte de cliente de NeoForge: renderer de la lápida, partículas y aguja de las brújulas. */
public final class ChaosGravestoneNeoForgeClient {

	private ChaosGravestoneNeoForgeClient() {}

	public static void init(IEventBus modBus) {
		modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
				event.registerBlockEntityRenderer(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new));
		modBus.addListener((RegisterParticleProvidersEvent event) -> {
			event.registerSpriteSet(ModParticles.SKULL, SkullParticle.Provider::gray);
			event.registerSpriteSet(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
		});
		modBus.addListener((FMLClientSetupEvent event) ->
				event.enqueueWork(ChaosGravestoneClient::registerItemProperties));

		if (SelfTest.enabled()) {
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> SelfTest.tick(Minecraft.getInstance()));
			// Simula un mod de revivir: cancela la muerte antes que nadie y deja al jugador con vida.
			NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDeathEvent.class, event -> {
				if (LogicTest.cancelNextDeath && event.getEntity() instanceof ServerPlayer player) {
					player.setHealth(1.0F);
					event.setCanceled(true);
				}
			});
			if (ModList.get().isLoaded("curios")) {
				LogicTest.compat.add(new SelfTestCompat.Curios());
			}
		}
	}
}

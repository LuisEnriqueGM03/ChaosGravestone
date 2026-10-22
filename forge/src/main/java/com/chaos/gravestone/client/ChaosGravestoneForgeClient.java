package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Parte de cliente de Forge: renderer de la lápida, partículas y aguja de las brújulas. */
public final class ChaosGravestoneForgeClient {

	private ChaosGravestoneForgeClient() {}

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
			MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent.Post event) ->
					SelfTest.tick(Minecraft.getInstance()));
			// Simula un mod de revivir: cancela la muerte antes que nadie y deja al jugador con vida.
			MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDeathEvent.class, event -> {
				if (LogicTest.cancelNextDeath && event.getEntity() instanceof ServerPlayer player) {
					player.setHealth(1.0F);
					event.setCanceled(true);
				}
			});
		}
	}
}

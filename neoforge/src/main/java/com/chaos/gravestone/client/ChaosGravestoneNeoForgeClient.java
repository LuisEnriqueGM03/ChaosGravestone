package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
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

		if (SelfTest.enabled()) {
			NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> SelfTest.tick(Minecraft.getInstance()));
		}
	}
}

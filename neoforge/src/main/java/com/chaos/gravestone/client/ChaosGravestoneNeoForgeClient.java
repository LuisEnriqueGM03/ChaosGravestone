package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;

/** Parte de cliente de Forge: renderer de la lÃƒÂ¡pida, partÃƒÂ­culas y aguja de las brÃƒÂºjulas. */
public final class ChaosGravestoneNeoForgeClient {

	private ChaosGravestoneNeoForgeClient() {}

	public static void init(IEventBus modBus) {
        modBus.addListener(EventPriority.NORMAL, false, net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent.class,
            event -> event.enqueueWork(ChaosGravestoneClient::registerCompassModels));
		modBus.addListener(EventPriority.NORMAL, false, EntityRenderersEvent.RegisterRenderers.class, event ->
				event.registerBlockEntityRenderer(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new));
		modBus.addListener(EventPriority.NORMAL, false, RegisterParticleProvidersEvent.class, event -> {
			event.registerSpriteSet(ModParticles.SKULL, SkullParticle.Provider::gray);
			event.registerSpriteSet(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
		});

		if (SelfTest.enabled()) {
			MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, TickEvent.ClientTickEvent.class, event -> {
                if (event.phase == TickEvent.Phase.END) SelfTest.tick(Minecraft.getInstance());
            });
		}
	}
}

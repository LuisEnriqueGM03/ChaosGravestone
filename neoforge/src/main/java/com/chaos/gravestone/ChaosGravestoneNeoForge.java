package com.chaos.gravestone;

import com.chaos.gravestone.client.ChaosGravestoneClient;
import com.chaos.gravestone.client.ChaosGravestoneNeoForgeClient;
import com.chaos.gravestone.network.TotemSkullPayload;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

/** Entrada de NeoForge: registra todo en RegisterEvent y conecta los eventos con el código común. */
@Mod(ChaosGravestone.MOD_ID)
public class ChaosGravestoneNeoForge {

	public ChaosGravestoneNeoForge(IEventBus modBus, Dist dist) {
		ChaosGravestone.init();

		modBus.addListener(ChaosGravestoneNeoForge::register);
		modBus.addListener(ChaosGravestoneNeoForge::registerPayloads);

		NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerRespawnEvent event) -> {
			if (event.getEntity() instanceof ServerPlayer player) {
				GravestoneCompass.onRespawn(player);
			}
		});
		NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> GravestoneCompass.tick(event.getServer()));

		if (dist.isClient()) {
			ChaosGravestoneNeoForgeClient.init(modBus);
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

	private static void registerPayloads(RegisterPayloadHandlersEvent event) {
		// La referencia a la clase de cliente solo se resuelve al recibir el paquete (nunca en un servidor dedicado).
		event.registrar("1").playToClient(TotemSkullPayload.TYPE, TotemSkullPayload.CODEC,
				(payload, context) -> context.enqueueWork(ChaosGravestoneClient::showTotemSkull));
	}
}

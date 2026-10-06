package com.chaos.gravestone;

import com.chaos.gravestone.network.TotemSkullPayload;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

/** Entrada de Fabric: registra todo directamente y conecta los eventos con el código común. */
public class ChaosGravestoneFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		ModBlocks.registerBlocks((id, block) -> Registry.register(BuiltInRegistries.BLOCK, id, block));
		ModBlocks.registerItems((id, item) -> Registry.register(BuiltInRegistries.ITEM, id, item));
		ModBlocks.registerBlockEntities((id, type) -> Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, type));
		ModBlocks.registerRecipeSerializers((id, s) -> Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, id, s));
		ModBlocks.registerCreativeTabs((id, tab) -> Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id, tab));
		ModParticles.register((id, type) -> Registry.register(BuiltInRegistries.PARTICLE_TYPE, id, type));

		ChaosGravestone.init();

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GravestoneCompass.onRespawn(newPlayer));
		ServerTickEvents.END_SERVER_TICK.register(GravestoneCompass::tick);
		PayloadTypeRegistry.playS2C().register(TotemSkullPayload.TYPE, TotemSkullPayload.CODEC);
	}
}

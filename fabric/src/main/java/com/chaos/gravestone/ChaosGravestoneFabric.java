package com.chaos.gravestone;

import com.chaos.gravestone.compat.AccessoriesProvider;
import com.chaos.gravestone.compat.CosmeticArmorProvider;
import com.chaos.gravestone.compat.TravelersBackpackProvider;
import com.chaos.gravestone.compat.TrinketsProvider;
import com.chaos.gravestone.network.TotemSkullPayload;
import com.chaos.gravestone.storage.ProviderRegistry;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.loader.api.FabricLoader;
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
		// Mods de accesorios (las clases solo se cargan si el mod está). Pueden ir los dos: cada provider vacía
		// la ranura al capturar, así que con la capa de compatibilidad de Accessories para Trinkets el segundo
		// ya la encuentra vacía y nada se guarda dos veces.
		if (FabricLoader.getInstance().isModLoaded("accessories")) {
			ProviderRegistry.register(new AccessoriesProvider());
		}
		if (FabricLoader.getInstance().isModLoaded("trinkets")) {
			ProviderRegistry.register(new TrinketsProvider());
		}
		if (FabricLoader.getInstance().isModLoaded("travelersbackpack")) {
			ProviderRegistry.register(new TravelersBackpackProvider());
		}
		// En Fabric, Cosmetic Armor Reworked es el fork (misma API); "Cosmetic Armor: Revitalized" usa Trinkets.
		if (FabricLoader.getInstance().isModLoaded("cosmeticarmorreworkedforked")
				|| FabricLoader.getInstance().isModLoaded("cosmeticarmorreworked")) {
			ProviderRegistry.register(new CosmeticArmorProvider());
		}

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> GravestoneCompass.onRespawn(newPlayer));
		ServerTickEvents.END_SERVER_TICK.register(GravestoneCompass::tick);
		PayloadTypeRegistry.playS2C().register(TotemSkullPayload.TYPE, TotemSkullPayload.CODEC);
	}
}

package com.chaos.gravestone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chaos.gravestone.compat.VanillaProvider;
import com.chaos.gravestone.config.GravestoneConfig;
import com.chaos.gravestone.storage.ProviderRegistry;

import net.fabricmc.api.ModInitializer;

public class ChaosGravestone implements ModInitializer {

	public static final String MOD_ID = "chaosgravestone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		GravestoneConfig.load();
		ModBlocks.register();
		ModParticles.register();
		ProviderRegistry.register(new VanillaProvider());
		GravestoneCompass.register();
		// Fase 3/4: providers de Accessories, Trinkets, Travelers Backpack y Cosmetic Armor,
		// cada uno registrado solo si FabricLoader.isModLoaded(...).
	}
}

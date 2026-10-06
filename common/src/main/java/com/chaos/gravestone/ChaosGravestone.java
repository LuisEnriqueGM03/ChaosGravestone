package com.chaos.gravestone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chaos.gravestone.compat.VanillaProvider;
import com.chaos.gravestone.config.GravestoneConfig;
import com.chaos.gravestone.storage.ProviderRegistry;

/** Arranque común. El registro de objetos y los eventos los conecta cada cargador. */
public final class ChaosGravestone {

	public static final String MOD_ID = "chaosgravestone";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private ChaosGravestone() {}

	public static void init() {
		GravestoneConfig.load();
		ProviderRegistry.register(new VanillaProvider());
		// Pendiente: providers de Accessories, Trinkets/Curios, Travelers Backpack y Cosmetic Armor,
		// cada uno registrado solo si Services.PLATFORM.isModLoaded(...).
	}
}

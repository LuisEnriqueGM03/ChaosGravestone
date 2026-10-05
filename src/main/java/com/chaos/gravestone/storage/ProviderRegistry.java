package com.chaos.gravestone.storage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.ChaosGravestone;

import net.minecraft.server.level.ServerPlayer;

public final class ProviderRegistry {

	private static final Map<String, SlotProvider> PROVIDERS = new LinkedHashMap<>();

	private ProviderRegistry() {}

	public static void register(SlotProvider provider) {
		PROVIDERS.put(provider.id(), provider);
	}

	@Nullable
	public static SlotProvider get(String id) {
		return PROVIDERS.get(id);
	}

	/** Captura todo, cada provider aislado para que un fallo de un mod no pierda el resto. */
	public static List<StoredEntry> captureAll(ServerPlayer player) {
		List<StoredEntry> all = new ArrayList<>();
		for (SlotProvider provider : PROVIDERS.values()) {
			try {
				all.addAll(provider.capture(player));
			} catch (Throwable t) {
				ChaosGravestone.LOGGER.error("Fallo al capturar con el provider {}", provider.id(), t);
			}
		}
		return all;
	}
}

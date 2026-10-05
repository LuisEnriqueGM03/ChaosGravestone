package com.chaos.gravestone.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import com.chaos.gravestone.ChaosGravestone;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class GravestoneConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static GravestoneConfig instance = new GravestoneConfig();

	/** Solo el dueño puede abrir la lápida. */
	public boolean protectedByOwner = true;
	/** Minutos tras los que cualquiera puede abrirla; 0 = nunca. */
	public int unlockAfterMinutes = 0;
	/** Los OP (nivel 2+) pueden abrir cualquier lápida. */
	public boolean opsCanOpen = true;
	/** Mostrar coordenadas y dimensión al morir. */
	public boolean showCoordinates = true;
	/**
	 * Estilo de lápida: "auto" (según dimensión, profundidad y bioma) o uno fijo:
	 * cualquier id de GravestoneVariant (gravestone, mossy_gravestone, nether_gravestone, gilded_gravestone...).
	 */
	public String variant = "auto";

	public static GravestoneConfig get() {
		return instance;
	}

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve("chaosgravestone.json");
		if (Files.exists(file)) {
			try (Reader reader = Files.newBufferedReader(file)) {
				GravestoneConfig loaded = GSON.fromJson(reader, GravestoneConfig.class);
				if (loaded != null) {
					instance = loaded;
				}
			} catch (IOException | RuntimeException e) {
				ChaosGravestone.LOGGER.error("Config inválida, usando valores por defecto", e);
			}
		}
		try (Writer writer = Files.newBufferedWriter(file)) {
			GSON.toJson(instance, writer);
		} catch (IOException e) {
			ChaosGravestone.LOGGER.error("No se pudo escribir la config", e);
		}
	}
}

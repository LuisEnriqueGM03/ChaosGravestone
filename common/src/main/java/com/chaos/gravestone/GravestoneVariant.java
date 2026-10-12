package com.chaos.gravestone;

import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.config.GravestoneConfig;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.SoundType;

/**
 * Estilos de lápida. Las texturas, modelos y blockstates de cada id los genera tools/TextureGen.java.
 * Al morir se elige según la dimensión, la profundidad y el bioma (o lo que fije la config).
 * Amatista, cobre, cuarzo y dorada no salen solas: son para decorar (pestaña creativa) o forzarlas en la config.
 */
public enum GravestoneVariant {
	CLASSIC("gravestone", SoundType.STONE, 0),
	MOSSY("mossy_gravestone", SoundType.STONE, 0),
	SANDSTONE("sandstone_gravestone", SoundType.STONE, 0),
	FROST("frost_gravestone", SoundType.STONE, 0),
	DEEPSLATE("deepslate_gravestone", SoundType.DEEPSLATE_BRICKS, 0),
	NETHER("nether_gravestone", SoundType.NETHER_BRICKS, 4),
	END("end_gravestone", SoundType.STONE, 3),
	PRISMARINE("prismarine_gravestone", SoundType.STONE, 2),
	CHERRY("cherry_gravestone", SoundType.STONE, 0),
	AMETHYST("amethyst_gravestone", SoundType.AMETHYST, 3),
	COPPER("copper_gravestone", SoundType.COPPER, 0),
	QUARTZ("quartz_gravestone", SoundType.STONE, 0),
	GILDED("gilded_gravestone", SoundType.STONE, 0),
	SCULK("sculk_gravestone", SoundType.SCULK_CATALYST, 3),
	CRIMSON("crimson_gravestone", SoundType.NETHER_BRICKS, 2),
	WARPED("warped_gravestone", SoundType.NETHER_BRICKS, 2),
	TERRACOTTA("terracotta_gravestone", SoundType.STONE, 0),
	MUSHROOM("mushroom_gravestone", SoundType.STONE, 0);

	private final String id;
	private final SoundType sound;
	private final int light;

	GravestoneVariant(String id, SoundType sound, int light) {
		this.id = id;
		this.sound = sound;
		this.light = light;
	}

	public String id() {
		return id;
	}

	public SoundType sound() {
		return sound;
	}

	public int light() {
		return light;
	}

	@Nullable
	public static GravestoneVariant byName(String name) {
		for (GravestoneVariant v : values()) {
			if (v.id.equals(name) || v.name().equalsIgnoreCase(name)) {
				return v;
			}
		}
		return null;
	}

	/** Variante para una lápida nueva en esa posición. */
	public static GravestoneVariant choose(ServerLevel level, BlockPos pos) {
		String forced = GravestoneConfig.get().variant;
		if (forced != null && !forced.toLowerCase(Locale.ROOT).equals("auto")) {
			GravestoneVariant v = byName(forced);
			if (v != null) {
				return v;
			}
		}

		Holder<Biome> biome = level.getBiome(pos);
		if (level.dimension() == Level.NETHER) {
			if (biome.is(Biomes.CRIMSON_FOREST)) {
				return CRIMSON;
			}
			if (biome.is(Biomes.WARPED_FOREST)) {
				return WARPED;
			}
			return NETHER;
		}
		if (level.dimension() == Level.END) {
			return END;
		}
		boolean lush = biome.is(BiomeTags.IS_JUNGLE) || biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)
				|| biome.is(Biomes.LUSH_CAVES);
		if (pos.getY() < 0) {
			if (biome.is(Biomes.DEEP_DARK)) {
				return SCULK;
			}
			return lush ? MOSSY : DEEPSLATE;
		}
		if (biome.value().coldEnoughToSnow(pos, level.getSeaLevel())) {
			return FROST;
		}
		if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
			return PRISMARINE;
		}
		if (biome.is(Biomes.CHERRY_GROVE)) {
			return CHERRY;
		}
		if (biome.is(Biomes.MUSHROOM_FIELDS)) {
			return MUSHROOM;
		}
		if (biome.is(BiomeTags.IS_BADLANDS)) {
			return TERRACOTTA;
		}
		if (biome.is(Biomes.DESERT) || biome.is(BiomeTags.IS_BEACH)) {
			return SANDSTONE;
		}
		if (lush) {
			return MOSSY;
		}
		return CLASSIC;
	}
}

package com.chaos.gravestone;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class ModParticles {

	/** Calavera gris translúcida que sube flotando; guía de la brújula de lápida. */
	public static final SimpleParticleType SKULL = FabricParticleTypes.simple();
	/** Calavera dorada que brota de la lápida (hasta 3 bloques de altura). */
	public static final SimpleParticleType GOLD_SKULL = FabricParticleTypes.simple();

	private ModParticles() {}

	public static void register() {
		Registry.register(BuiltInRegistries.PARTICLE_TYPE,
				ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "skull"), SKULL);
		Registry.register(BuiltInRegistries.PARTICLE_TYPE,
				ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "gold_skull"), GOLD_SKULL);
	}
}

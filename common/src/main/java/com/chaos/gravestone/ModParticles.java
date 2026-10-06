package com.chaos.gravestone;

import com.chaos.gravestone.platform.Registrar;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;

public final class ModParticles {

	/** Calavera gris translúcida que sube flotando; guía de la brújula de lápida. */
	public static final SimpleParticleType SKULL = simple();
	/** Calavera dorada que brota de la lápida (hasta 4 bloques de altura). */
	public static final SimpleParticleType GOLD_SKULL = simple();

	private ModParticles() {}

	public static void register(Registrar<ParticleType<?>> registrar) {
		registrar.register(ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "skull"), SKULL);
		registrar.register(ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "gold_skull"), GOLD_SKULL);
	}

	/** El constructor vanilla es protegido; una subclase anónima basta (es lo que hace Fabric por dentro). */
	private static SimpleParticleType simple() {
		return new SimpleParticleType(false) {};
	}
}

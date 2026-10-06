package com.chaos.gravestone.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

/**
 * Calavera que sube flotando con un vaivén, crece un poco y se desvanece.
 * Gris translúcida para el rastro de la brújula; dorada (más opaca y lenta) sobre la lápida.
 */
public class SkullParticle extends TextureSheetParticle {

	private static final float GRAY_ALPHA = 0.6F;
	private static final float GOLD_ALPHA = 0.85F;
	/** Lo que puede subir una calavera dorada desde que nace (nace a 1 bloque: tope 4 sobre la base). */
	private static final double GOLD_MAX_RISE = 2.9;
	private static final double GOLD_SPEED = 0.035;

	private final boolean gold;
	private final float maxAlpha;
	private final float baseSize;
	private final float swayPhase;
	private final double startY;

	protected SkullParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites, boolean gold) {
		super(level, x, y, z);
		this.gold = gold;
		pickSprite(sprites);
		startY = y;
		swayPhase = random.nextFloat() * Mth.TWO_PI;
		gravity = 0;
		friction = 0.96F;
		hasPhysics = false;
		xd = (random.nextDouble() - 0.5) * 0.01;
		zd = (random.nextDouble() - 0.5) * 0.01;
		if (gold) {
			maxAlpha = GOLD_ALPHA;
			// Vida suficiente para llegar al tope (2,9 / 0,035 ≈ 83 ticks).
			lifetime = 80 + random.nextInt(10);
			baseSize = 0.07F + random.nextFloat() * 0.02F;
			yd = GOLD_SPEED;
		} else {
			maxAlpha = GRAY_ALPHA;
			lifetime = 30 + random.nextInt(15);
			baseSize = 0.06F + random.nextFloat() * 0.025F;
			yd = 0.02 + random.nextDouble() * 0.015;
			float gray = 0.75F + random.nextFloat() * 0.2F;
			setColor(gray, gray, gray);
		}
		quadSize = baseSize;
		alpha = 0;
	}

	@Override
	public void tick() {
		super.tick();
		if (gold && y - startY > GOLD_MAX_RISE) {
			remove();
			return;
		}
		float t = (float) age / lifetime;
		// Aparece rápido, se desvanece despacio.
		alpha = maxAlpha * Math.min(1.0F, t * 6.0F) * (1.0F - t);
		quadSize = baseSize * (1.0F + t * 0.3F);
		// Vaivén lateral mientras sube.
		xd += Math.sin(age * 0.25F + swayPhase) * 0.0015;
		zd += Math.cos(age * 0.25F + swayPhase) * 0.0015;
		yd = gold ? GOLD_SPEED : Math.max(yd, 0.015);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	/** Se ven también de noche y en cuevas. */
	@Override
	protected int getLightColor(float partialTick) {
		return LightTexture.FULL_BRIGHT;
	}

	public static class Provider implements ParticleProvider<SimpleParticleType> {

		private final SpriteSet sprites;
		private final boolean gold;

		private Provider(SpriteSet sprites, boolean gold) {
			this.sprites = sprites;
			this.gold = gold;
		}

		public static Provider gray(SpriteSet sprites) {
			return new Provider(sprites, false);
		}

		public static Provider gold(SpriteSet sprites) {
			return new Provider(sprites, true);
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
				double xd, double yd, double zd) {
			return new SkullParticle(level, x, y, z, sprites, gold);
		}
	}
}

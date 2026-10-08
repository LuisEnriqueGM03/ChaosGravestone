package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Partes del cliente comunes a los cargadores; cada uno las llama desde sus eventos.
 * La aguja de las brújulas ya no va por código: desde 1.21.4 la define el modelo de ítem
 * (assets/chaosgravestone/items/*grave_compass.json, generado por tools/TextureGen.java).
 */
public final class ChaosGravestoneClient {

	private ChaosGravestoneClient() {}

	/** Animación del tótem, pero con la calavera morada, al llegar a la lápida. */
	public static void showTotemSkull() {
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModBlocks.PURPLE_SKULL));
	}
}

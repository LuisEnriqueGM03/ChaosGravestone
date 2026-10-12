package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * Partes del cliente comunes a los cargadores; cada uno las llama desde sus eventos.
 * La aguja usa el predicado angle de Minecraft 1.20.1 y las 32 texturas
 * de los modelos de brújula generados por tools/TextureGen.java.
 */
public final class ChaosGravestoneClient {

	private ChaosGravestoneClient() {}

    public static void registerCompassModels() {
        net.minecraft.client.renderer.item.CompassItemPropertyFunction angle =
            new net.minecraft.client.renderer.item.CompassItemPropertyFunction((level, stack, entity) ->
                com.chaos.gravestone.GravestoneCompass.read(stack).map(com.chaos.gravestone.GravestoneCompass.GraveRef::pos).orElse(null));
        net.minecraft.client.renderer.item.ItemProperties.register(ModBlocks.GRAVE_COMPASS,
            new net.minecraft.resources.ResourceLocation("angle"), angle);
        net.minecraft.client.renderer.item.ItemProperties.register(ModBlocks.ENDER_GRAVE_COMPASS,
            new net.minecraft.resources.ResourceLocation("angle"), angle);
    }

	/** Animación del tótem, pero con la calavera morada, al llegar a la lápida. */
	public static void showTotemSkull() {
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModBlocks.PURPLE_SKULL));
	}
}

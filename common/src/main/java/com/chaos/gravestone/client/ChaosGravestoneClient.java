package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

/** Partes del cliente comunes a ambos cargadores; cada uno las llama desde sus eventos. */
public final class ChaosGravestoneClient {

	private ChaosGravestoneClient() {}

	/** Misma animación de aguja que la brújula vanilla, apuntando a la lápida guardada. */
	public static void registerItemProperties() {
		registerCompassNeedle(ModBlocks.GRAVE_COMPASS);
		registerCompassNeedle(ModBlocks.ENDER_GRAVE_COMPASS);
	}

	/** Animación del tótem, pero con la calavera morada, al llegar a la lápida. */
	public static void showTotemSkull() {
		Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModBlocks.PURPLE_SKULL));
	}

	private static void registerCompassNeedle(Item item) {
		ItemProperties.register(item, ResourceLocation.withDefaultNamespace("angle"),
				new CompassItemPropertyFunction((level, stack, entity) -> {
					LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
					return tracker != null ? tracker.target().orElse(null) : null;
				}));
	}
}

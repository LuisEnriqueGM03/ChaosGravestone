package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;
import com.chaos.gravestone.network.TotemSkullPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.CompassItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

public class ChaosGravestoneClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.SKULL, SkullParticle.Provider::gray);
		ParticleFactoryRegistry.getInstance().register(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
		registerCompassNeedle(ModBlocks.GRAVE_COMPASS);
		registerCompassNeedle(ModBlocks.ENDER_GRAVE_COMPASS);

		// Animación del tótem, pero con la calavera morada, al llegar a la lápida.
		ClientPlayNetworking.registerGlobalReceiver(TotemSkullPayload.TYPE, (payload, context) ->
				context.client().execute(() ->
						context.client().gameRenderer.displayItemActivation(new ItemStack(ModBlocks.PURPLE_SKULL))));

		if (SelfTest.enabled()) {
			SelfTest.register();
		}
	}

	/** Misma animación de aguja que la brújula vanilla, apuntando a la lápida guardada. */
	private static void registerCompassNeedle(Item item) {
		ItemProperties.register(item, ResourceLocation.withDefaultNamespace("angle"),
				new CompassItemPropertyFunction((level, stack, entity) -> {
					LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
					return tracker != null ? tracker.target().orElse(null) : null;
				}));
	}
}

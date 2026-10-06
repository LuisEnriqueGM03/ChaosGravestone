package com.chaos.gravestone.client;

import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;
import com.chaos.gravestone.network.TotemSkullPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.itemgroup.v1.FabricCreativeInventoryScreen;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public class ChaosGravestoneFabricClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		BlockEntityRenderers.register(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new);
		ParticleFactoryRegistry.getInstance().register(ModParticles.SKULL, SkullParticle.Provider::gray);
		ParticleFactoryRegistry.getInstance().register(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
		ChaosGravestoneClient.registerItemProperties();
		ClientPlayNetworking.registerGlobalReceiver(TotemSkullPayload.TYPE, (payload, context) ->
				context.client().execute(ChaosGravestoneClient::showTotemSkull));

		if (SelfTest.enabled()) {
			SelfTest.tabSelector = screen -> ((FabricCreativeInventoryScreen) screen).setSelectedItemGroup(ModBlocks.TAB);
			ClientTickEvents.END_CLIENT_TICK.register(SelfTest::tick);
		}
	}
}

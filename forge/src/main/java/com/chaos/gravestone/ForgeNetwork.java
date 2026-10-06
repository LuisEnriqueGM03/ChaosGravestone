package com.chaos.gravestone;

import com.chaos.gravestone.client.ChaosGravestoneClient;
import com.chaos.gravestone.network.TotemSkullPayload;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

/** Red de Forge: un SimpleChannel con el aviso de la animación del tótem. */
public final class ForgeNetwork {

	private static final SimpleChannel CHANNEL = ChannelBuilder
			.named(ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "main"))
			.networkProtocolVersion(1)
			.simpleChannel();

	private ForgeNetwork() {}

	public static void register() {
		// Sin datos: basta con que llegue. La clase de cliente solo se resuelve al recibirlo.
		CHANNEL.messageBuilder(TotemSkullPayload.class, NetworkDirection.PLAY_TO_CLIENT)
				.encoder((payload, buf) -> {})
				.decoder(buf -> new TotemSkullPayload())
				.consumerMainThread((payload, context) -> ChaosGravestoneClient.showTotemSkull())
				.add();
	}

	public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}
}

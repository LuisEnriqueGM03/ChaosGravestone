package com.chaos.gravestone.network;

import com.chaos.gravestone.ChaosGravestone;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Servidor -> cliente: muestra la animación del tótem con la calavera morada. */
public record TotemSkullPayload() implements CustomPacketPayload {

	public static final Type<TotemSkullPayload> TYPE =
			new Type<>(ResourceLocation.fromNamespaceAndPath(ChaosGravestone.MOD_ID, "totem_skull"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TotemSkullPayload> CODEC =
			StreamCodec.unit(new TotemSkullPayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}

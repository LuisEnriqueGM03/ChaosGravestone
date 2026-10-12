package com.chaos.gravestone;
import com.chaos.gravestone.client.ChaosGravestoneClient;
import com.chaos.gravestone.network.TotemSkullPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
public final class NeoForgeNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
        .named(new ResourceLocation(ChaosGravestone.MOD_ID, "main"))
        .networkProtocolVersion(() -> "1").clientAcceptedVersions("1"::equals)
        .serverAcceptedVersions("1"::equals).simpleChannel();
    private NeoForgeNetwork() {}
    public static void register() {
        CHANNEL.messageBuilder(TotemSkullPayload.class, 0, NetworkDirection.PLAY_TO_CLIENT)
            .encoder((payload, buf) -> {}).decoder(buf -> new TotemSkullPayload())
            .consumerMainThread((payload, context) -> {
                ChaosGravestoneClient.showTotemSkull();
                context.get().setPacketHandled(true);
            }).add();
    }
    public static void sendTotemSkull(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new TotemSkullPayload());
    }
}

package com.chaos.gravestone.client;
import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.TickEvent;
public final class ChaosGravestoneForgeClient {
    private ChaosGravestoneForgeClient() {}
    public static void init() {
        EntityRenderersEvent.RegisterRenderers.BUS.addListener(event ->
            event.registerBlockEntityRenderer(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new));
        RegisterParticleProvidersEvent.BUS.addListener(event -> {
            event.registerSpriteSet(ModParticles.SKULL, SkullParticle.Provider::gray);
            event.registerSpriteSet(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
        });
        if (SelfTest.enabled()) TickEvent.ClientTickEvent.Post.BUS.addListener(event -> SelfTest.tick(Minecraft.getInstance()));
    }
}

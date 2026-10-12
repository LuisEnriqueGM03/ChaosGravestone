package com.chaos.gravestone.network;
import com.chaos.gravestone.ChaosGravestone;
import net.minecraft.resources.ResourceLocation;
/** Empty server-to-client notification. Transport is supplied by each loader. */
public record TotemSkullPayload() {
    public static final ResourceLocation ID = new ResourceLocation(ChaosGravestone.MOD_ID, "totem_skull");
}

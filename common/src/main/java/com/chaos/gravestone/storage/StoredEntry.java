package com.chaos.gravestone.storage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
/** Original slot and complete item components, encoded with the registry context. */
public record StoredEntry(String providerId, String slotKey, ItemStack stack) {
    public static final Codec<StoredEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("provider").forGetter(StoredEntry::providerId),
        Codec.STRING.fieldOf("slot").forGetter(StoredEntry::slotKey),
        ItemStack.CODEC.fieldOf("item").forGetter(StoredEntry::stack)
    ).apply(instance, StoredEntry::new));
}

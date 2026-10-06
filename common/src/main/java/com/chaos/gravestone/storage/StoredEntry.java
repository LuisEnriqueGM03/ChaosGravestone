package com.chaos.gravestone.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Un objeto guardado en la lápida junto con la ranura de origen. */
public record StoredEntry(String providerId, String slotKey, ItemStack stack) {

	public CompoundTag save(HolderLookup.Provider registries) {
		CompoundTag tag = new CompoundTag();
		tag.putString("provider", providerId);
		tag.putString("slot", slotKey);
		tag.put("item", stack.save(registries));
		return tag;
	}

	public static StoredEntry load(CompoundTag tag, HolderLookup.Provider registries) {
		ItemStack stack = ItemStack.parse(registries, tag.getCompound("item")).orElse(ItemStack.EMPTY);
		return new StoredEntry(tag.getString("provider"), tag.getString("slot"), stack);
	}
}

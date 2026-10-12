package com.chaos.gravestone.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/** Un objeto guardado en la lápida junto con la ranura de origen. */
public record StoredEntry(String providerId, String slotKey, ItemStack stack) {

	public CompoundTag save() {
		CompoundTag tag = new CompoundTag();
		tag.putString("provider", providerId);
		tag.putString("slot", slotKey);
		tag.put("item", stack.save(new CompoundTag()));
		return tag;
	}

	public static StoredEntry load(CompoundTag tag) {
		ItemStack stack = ItemStack.of(tag.getCompound("item"));
		return new StoredEntry(tag.getString("provider"), tag.getString("slot"), stack);
	}
}

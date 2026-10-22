package com.chaos.gravestone.compat;

import com.chaos.gravestone.storage.FixedSlotsProvider;

import lain.mods.cos.api.CosArmorAPI;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Ranuras de Cosmetic Armor Reworked (armadura cosmética). Respeta su opción de conservarlas al morir. */
public final class CosmeticArmorProvider extends FixedSlotsProvider {

	@Override
	public String id() {
		return CosmeticArmorSupport.ID;
	}

	@Override
	protected boolean keepsOnDeath(ServerPlayer player) {
		return CosmeticArmorSupport.keepsThroughDeath();
	}

	@Override
	protected int size(ServerPlayer player) {
		CAStacksBase stacks = CosArmorAPI.getCAStacks(player.getUUID());
		return stacks == null ? 0 : stacks.getSlots();
	}

	@Override
	protected ItemStack get(ServerPlayer player, int slot) {
		return CosArmorAPI.getCAStacks(player.getUUID()).getStackInSlot(slot);
	}

	@Override
	protected void set(ServerPlayer player, int slot, ItemStack stack) {
		CosArmorAPI.getCAStacks(player.getUUID()).setStackInSlot(slot, stack);
	}
}

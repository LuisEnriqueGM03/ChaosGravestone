package com.chaos.gravestone.compat;

import java.util.ArrayList;
import java.util.List;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.storage.SlotProvider;
import com.chaos.gravestone.storage.StoredEntry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Inventario vanilla: 0-35 principal, 36-39 armadura, 40 mano secundaria. */
public final class VanillaProvider implements SlotProvider {

	public static final String ID = "vanilla";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public List<StoredEntry> capture(ServerPlayer player) {
		List<StoredEntry> out = new ArrayList<>();
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			inv.setItem(i, ItemStack.EMPTY);
			// Las brújulas de lápida no se guardan: se reentregan al reaparecer.
			var compass = GravestoneCompass.read(stack);
			if (compass.isPresent()) {
				// Se devuelve la misma (conserva la de ender); las vanilla antiguas pasan a la del mod.
				GravestoneCompass.addPending(player.getUUID(),
						stack.is(Items.COMPASS) ? GravestoneCompass.create(compass.get()) : stack);
				continue;
			}
			// Maldición de desaparición: el objeto se pierde, igual que en vanilla.
			if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			out.add(new StoredEntry(ID, "slot#" + i, stack));
		}
		return out;
	}

	@Override
	public boolean restore(ServerPlayer player, StoredEntry entry) {
		int index = parseIndex(entry.slotKey());
		Inventory inv = player.getInventory();
		if (index < 0 || index >= inv.getContainerSize() || !inv.getItem(index).isEmpty()) {
			return false;
		}
		inv.setItem(index, entry.stack().copy());
		return true;
	}

	private static int parseIndex(String key) {
		try {
			return Integer.parseInt(key.substring(key.indexOf('#') + 1));
		} catch (RuntimeException e) {
			return -1;
		}
	}
}

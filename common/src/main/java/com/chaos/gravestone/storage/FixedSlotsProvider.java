package com.chaos.gravestone.storage;

import java.util.ArrayList;
import java.util.List;

import com.chaos.gravestone.GravestoneCompass;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Base para mods con un número fijo de ranuras propias (Cosmetic Armor, la mochila de Traveler's Backpack).
 * Cada cargador implementa el acceso con la API del mod; la lógica de captura y restauración es común.
 * Clave de ranura: "slot#índice".
 */
public abstract class FixedSlotsProvider implements SlotProvider {

	/** Número de ranuras del jugador (0 si no tiene). */
	protected abstract int size(ServerPlayer player);

	protected abstract ItemStack get(ServerPlayer player, int slot);

	/** Pone el objeto (o vacía la ranura con {@link ItemStack#EMPTY}). */
	protected abstract void set(ServerPlayer player, int slot, ItemStack stack);

	/** Acceso público a las ranuras (lo usa la autoprueba). */
	public final int slotCount(ServerPlayer player) {
		return size(player);
	}

	public final ItemStack slot(ServerPlayer player, int slot) {
		return get(player, slot);
	}

	public final void setSlot(ServerPlayer player, int slot, ItemStack stack) {
		set(player, slot, stack);
	}

	/** {@code true} si el propio mod conserva estas ranuras al morir: entonces no se tocan. */
	protected boolean keepsOnDeath(ServerPlayer player) {
		return false;
	}

	@Override
	public List<StoredEntry> capture(ServerPlayer player) {
		List<StoredEntry> out = new ArrayList<>();
		if (keepsOnDeath(player)) {
			return out;
		}
		for (int i = 0; i < size(player); i++) {
			ItemStack stack = get(player, i);
			if (stack.isEmpty()) {
				continue;
			}
			set(player, i, ItemStack.EMPTY);
			// Maldición de desaparición: se pierde, igual que en vanilla.
			if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
				continue;
			}
			// Una brújula de lápida vuelve al reaparecer, como en el inventario normal.
			if (GravestoneCompass.read(stack).isPresent()) {
				GravestoneCompass.addPending(player, stack);
				continue;
			}
			out.add(new StoredEntry(id(), "slot#" + i, stack));
		}
		return out;
	}

	@Override
	public boolean restore(ServerPlayer player, StoredEntry entry) {
		int slot;
		try {
			slot = Integer.parseInt(entry.slotKey().substring(entry.slotKey().indexOf('#') + 1));
		} catch (RuntimeException e) {
			return false;
		}
		if (slot < 0 || slot >= size(player) || !get(player, slot).isEmpty()) {
			return false;
		}
		set(player, slot, entry.stack().copy());
		return true;
	}
}

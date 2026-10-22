package com.chaos.gravestone.compat;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.storage.SlotProvider;
import com.chaos.gravestone.storage.StoredEntry;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.DropRule;
import io.wispforest.accessories.api.events.OnDropCallback;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Ranuras de Accessories (también cubre los mods de Trinkets si se usa su capa de compatibilidad).
 * Clave: "ranura#índice" o "ranura#cosmetic#índice". Respeta las reglas de caída de Accessories.
 */
public final class AccessoriesProvider implements SlotProvider {

	public static final String ID = "accessories";
	private static final String COSMETIC = "cosmetic#";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public List<StoredEntry> capture(ServerPlayer player) {
		List<StoredEntry> out = new ArrayList<>();
		AccessoriesCapability capability = AccessoriesCapability.get(player);
		if (capability == null) {
			return out;
		}
		DamageSource source = player.getLastDamageSource() != null
				? player.getLastDamageSource() : player.damageSources().generic();
		for (AccessoriesContainer container : capability.getContainers().values()) {
			capture(player, source, container, container.getAccessories(), "", out);
			capture(player, source, container, container.getCosmeticAccessories(), COSMETIC, out);
		}
		return out;
	}

	private static void capture(ServerPlayer player, DamageSource source, AccessoriesContainer container,
			Container inv, String prefix, List<StoredEntry> out) {
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack stack = inv.getItem(i);
			if (stack.isEmpty()) {
				continue;
			}
			DropRule rule = dropRule(stack, container.createReference(i), source);
			if (rule == DropRule.KEEP) {
				continue;
			}
			inv.setItem(i, ItemStack.EMPTY);
			if (rule == DropRule.DESTROY) {
				continue;
			}
			// Una brújula de lápida en una ranura de accesorio vuelve al reaparecer, como en vanilla.
			if (GravestoneCompass.read(stack).isPresent()) {
				GravestoneCompass.addPending(player, stack);
				continue;
			}
			out.add(new StoredEntry(ID, container.getSlotName() + "#" + prefix + i, stack));
		}
	}

	/** Igual que Accessories al morir: regla del objeto, callback, regla de la ranura y, por defecto, maldición. */
	private static DropRule dropRule(ItemStack stack, SlotReference ref, DamageSource source) {
		DropRule rule = AccessoriesAPI.getOrDefaultAccessory(stack).getDropRule(stack, ref, source);
		rule = OnDropCallback.getAlternativeRule(rule, stack, ref, source);
		if (rule == DropRule.DEFAULT && ref.type() != null) {
			rule = ref.type().dropRule();
		}
		if (rule == DropRule.DEFAULT) {
			rule = EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
					? DropRule.DESTROY : DropRule.DROP;
		}
		return rule;
	}

	@Override
	public boolean restore(ServerPlayer player, StoredEntry entry) {
		String key = entry.slotKey();
		int hash = key.indexOf('#');
		if (hash < 0) {
			return false;
		}
		String slotName = key.substring(0, hash);
		String rest = key.substring(hash + 1);
		boolean cosmetic = rest.startsWith(COSMETIC);
		int index;
		try {
			index = Integer.parseInt(cosmetic ? rest.substring(COSMETIC.length()) : rest);
		} catch (NumberFormatException e) {
			return false;
		}
		Container inv = container(player, slotName, cosmetic);
		if (inv == null || index < 0 || index >= inv.getContainerSize() || !inv.getItem(index).isEmpty()) {
			return false;
		}
		inv.setItem(index, entry.stack().copy());
		return true;
	}

	@Nullable
	private static Container container(ServerPlayer player, String slotName, boolean cosmetic) {
		AccessoriesCapability capability = AccessoriesCapability.get(player);
		AccessoriesContainer container = capability == null ? null : capability.getContainers().get(slotName);
		if (container == null) {
			return null;
		}
		return cosmetic ? container.getCosmeticAccessories() : container.getAccessories();
	}
}

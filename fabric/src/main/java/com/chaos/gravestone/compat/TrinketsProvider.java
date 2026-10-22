package com.chaos.gravestone.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.storage.SlotProvider;
import com.chaos.gravestone.storage.StoredEntry;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketEnums.DropRule;
import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import dev.emi.trinkets.api.event.TrinketDropCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/** Ranuras de Trinkets. Clave: "grupo/ranura#índice". Respeta las reglas de caída de Trinkets. */
public final class TrinketsProvider implements SlotProvider {

	public static final String ID = "trinkets";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public List<StoredEntry> capture(ServerPlayer player) {
		List<StoredEntry> out = new ArrayList<>();
		TrinketsApi.getTrinketComponent(player).ifPresent(component -> {
			for (Map.Entry<String, Map<String, TrinketInventory>> group : component.getInventory().entrySet()) {
				for (Map.Entry<String, TrinketInventory> slot : group.getValue().entrySet()) {
					TrinketInventory inv = slot.getValue();
					for (int i = 0; i < inv.getContainerSize(); i++) {
						ItemStack stack = inv.getItem(i);
						if (stack.isEmpty()) {
							continue;
						}
						DropRule rule = dropRule(player, stack, new SlotReference(inv, i));
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
						out.add(new StoredEntry(ID, group.getKey() + "/" + slot.getKey() + "#" + i, stack));
					}
				}
			}
		});
		return out;
	}

	/** Igual que Trinkets al morir: regla del objeto, callback, regla de la ranura y, por defecto, maldición. */
	private static DropRule dropRule(ServerPlayer player, ItemStack stack, SlotReference ref) {
		DropRule rule = TrinketsApi.getTrinket(stack.getItem()).getDropRule(stack, ref, player);
		rule = TrinketDropCallback.EVENT.invoker().drop(rule, stack, ref, player);
		if (rule == DropRule.DEFAULT) {
			rule = ref.inventory().getSlotType().getDropRule();
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
		int slash = key.indexOf('/');
		int hash = key.lastIndexOf('#');
		if (slash < 0 || hash < slash) {
			return false;
		}
		String group = key.substring(0, slash);
		String slot = key.substring(slash + 1, hash);
		int index;
		try {
			index = Integer.parseInt(key.substring(hash + 1));
		} catch (NumberFormatException e) {
			return false;
		}
		return TrinketsApi.getTrinketComponent(player).map(component -> {
			Map<String, TrinketInventory> slots = component.getInventory().get(group);
			TrinketInventory inv = slots == null ? null : slots.get(slot);
			if (inv == null || index < 0 || index >= inv.getContainerSize() || !inv.getItem(index).isEmpty()) {
				return false;
			}
			inv.setItem(index, entry.stack().copy());
			return true;
		}).orElse(false);
	}
}

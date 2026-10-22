package com.chaos.gravestone.compat;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.storage.SlotProvider;
import com.chaos.gravestone.storage.StoredEntry;
import com.google.common.collect.ImmutableList;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.common.NeoForge;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.event.DropRulesEvent;
import top.theillusivec4.curios.api.type.capability.ICurio.DropRule;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

/**
 * Ranuras de Curios. Clave: "ranura#índice" o "ranura#cosmetic#índice". Respeta las reglas de caída
 * como Curios al morir: las de otros mods (DropRulesEvent), la del objeto, la de la ranura y la maldición.
 */
public final class CuriosProvider implements SlotProvider {

	public static final String ID = "curios";
	private static final String COSMETIC = "cosmetic#";

	@Override
	public String id() {
		return ID;
	}

	@Override
	public List<StoredEntry> capture(ServerPlayer player) {
		List<StoredEntry> out = new ArrayList<>();
		ICuriosItemHandler handler = CuriosApi.getCuriosInventory(player).orElse(null);
		if (handler == null) {
			return out;
		}
		DamageSource source = player.getLastDamageSource() != null
				? player.getLastDamageSource() : player.damageSources().generic();
		ImmutableList<Tuple<Predicate<ItemStack>, DropRule>> overrides =
				NeoForge.EVENT_BUS.post(new DropRulesEvent(player, handler, source, 0, false)).getOverrides();
		for (ICurioStacksHandler stacks : handler.getCurios().values()) {
			capture(player, source, overrides, stacks, stacks.getStacks(), false, out);
			capture(player, source, overrides, stacks, stacks.getCosmeticStacks(), true, out);
		}
		return out;
	}

	private static void capture(ServerPlayer player, DamageSource source,
			List<Tuple<Predicate<ItemStack>, DropRule>> overrides, ICurioStacksHandler stacks, IDynamicStackHandler inv,
			boolean cosmetic, List<StoredEntry> out) {
		String slot = stacks.getIdentifier();
		for (int i = 0; i < inv.getSlots(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty()) {
				continue;
			}
			SlotContext context = new SlotContext(slot, player, i, cosmetic, true);
			DropRule rule = dropRule(stack, context, source, overrides, stacks);
			if (rule == DropRule.ALWAYS_KEEP) {
				continue;
			}
			inv.setStackInSlot(i, ItemStack.EMPTY);
			if (rule == DropRule.DESTROY) {
				continue;
			}
			// Una brújula de lápida en una ranura de accesorio vuelve al reaparecer, como en vanilla.
			if (GravestoneCompass.read(stack).isPresent()) {
				GravestoneCompass.addPending(player, stack);
				continue;
			}
			out.add(new StoredEntry(ID, slot + "#" + (cosmetic ? COSMETIC : "") + i, stack));
		}
	}

	private static DropRule dropRule(ItemStack stack, SlotContext context, DamageSource source,
			List<Tuple<Predicate<ItemStack>, DropRule>> overrides, ICurioStacksHandler stacks) {
		DropRule rule = DropRule.DEFAULT;
		for (Tuple<Predicate<ItemStack>, DropRule> override : overrides) {
			if (override.getA().test(stack)) {
				rule = override.getB();
			}
		}
		if (rule == DropRule.DEFAULT) {
			rule = CuriosApi.getCurio(stack).map(curio -> curio.getDropRule(context, source, false))
					.orElse(DropRule.DEFAULT);
		}
		if (rule == DropRule.DEFAULT) {
			rule = stacks.getDropRule();
		}
		if (rule == DropRule.DEFAULT) {
			rule = EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)
					? DropRule.DESTROY : DropRule.ALWAYS_DROP;
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
		String slot = key.substring(0, hash);
		String rest = key.substring(hash + 1);
		boolean cosmetic = rest.startsWith(COSMETIC);
		int index;
		try {
			index = Integer.parseInt(cosmetic ? rest.substring(COSMETIC.length()) : rest);
		} catch (NumberFormatException e) {
			return false;
		}
		IDynamicStackHandler inv = handler(player, slot, cosmetic);
		if (inv == null || index < 0 || index >= inv.getSlots() || !inv.getStackInSlot(index).isEmpty()) {
			return false;
		}
		inv.setStackInSlot(index, entry.stack().copy());
		return true;
	}

	@Nullable
	private static IDynamicStackHandler handler(ServerPlayer player, String slot, boolean cosmetic) {
		return CuriosApi.getCuriosInventory(player)
				.flatMap(h -> h.getStacksHandler(slot))
				.map(s -> cosmetic ? s.getCosmeticStacks() : s.getStacks())
				.orElse(null);
	}
}

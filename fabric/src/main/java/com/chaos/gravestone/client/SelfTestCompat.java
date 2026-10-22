package com.chaos.gravestone.client;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.emi.trinkets.api.TrinketInventory;
import dev.emi.trinkets.api.TrinketsApi;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Ranuras de accesorio para LogicTest. Cada clase solo se carga si su mod está. */
final class SelfTestCompat {

	private SelfTestCompat() {}

	static final class Trinkets implements LogicTest.CompatHook {

		@Override
		public String name() {
			return "trinkets";
		}

		@Override
		@Nullable
		public String equip(ServerPlayer player, ItemStack stack) {
			var component = TrinketsApi.getTrinketComponent(player).orElse(null);
			if (component == null) {
				return null;
			}
			for (Map.Entry<String, Map<String, TrinketInventory>> group : component.getInventory().entrySet()) {
				for (Map.Entry<String, TrinketInventory> slot : group.getValue().entrySet()) {
					if (slot.getValue().getContainerSize() > 0) {
						slot.getValue().setItem(0, stack);
						return group.getKey() + "/" + slot.getKey();
					}
				}
			}
			return null;
		}

		@Override
		public ItemStack get(ServerPlayer player, String key) {
			String[] parts = key.split("/");
			return TrinketsApi.getTrinketComponent(player)
					.map(c -> c.getInventory().get(parts[0]))
					.map(g -> g.get(parts[1]))
					.map(inv -> inv.getItem(0))
					.orElse(ItemStack.EMPTY);
		}

		@Override
		public void clear(ServerPlayer player) {
			TrinketsApi.getTrinketComponent(player).ifPresent(c -> c.getInventory().values()
					.forEach(group -> group.values().forEach(TrinketInventory::clearContent)));
		}
	}

	static final class Accessories implements LogicTest.CompatHook {

		@Override
		public String name() {
			return "accessories";
		}

		@Override
		@Nullable
		public String equip(ServerPlayer player, ItemStack stack) {
			AccessoriesCapability capability = AccessoriesCapability.get(player);
			if (capability == null) {
				return null;
			}
			for (AccessoriesContainer container : capability.getContainers().values()) {
				if (container.getSize() > 0) {
					container.getAccessories().setItem(0, stack);
					return container.getSlotName();
				}
			}
			return null;
		}

		@Override
		public ItemStack get(ServerPlayer player, String slot) {
			AccessoriesCapability capability = AccessoriesCapability.get(player);
			AccessoriesContainer container = capability == null ? null : capability.getContainers().get(slot);
			return container == null ? ItemStack.EMPTY : container.getAccessories().getItem(0);
		}

		@Override
		public void clear(ServerPlayer player) {
			AccessoriesCapability capability = AccessoriesCapability.get(player);
			if (capability != null) {
				capability.getContainers().values().forEach(c -> {
					c.getAccessories().clearContent();
					c.getCosmeticAccessories().clearContent();
				});
			}
		}
	}
}

package com.chaos.gravestone.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

/** Ranuras de accesorio para LogicTest. Solo se carga si Curios está. */
final class SelfTestCompat {

	private SelfTestCompat() {}

	static final class Curios implements LogicTest.CompatHook {

		@Override
		public String name() {
			return "curios";
		}

		@Override
		@Nullable
		public String equip(ServerPlayer player, ItemStack stack) {
			return CuriosApi.getCuriosInventory(player).flatMap(h -> h.getCurios().values().stream()
					.filter(s -> s.getSlots() > 0).findFirst()).map(s -> {
						s.getStacks().setStackInSlot(0, stack);
						return s.getIdentifier();
					}).orElse(null);
		}

		@Override
		public ItemStack get(ServerPlayer player, String slot) {
			return CuriosApi.getCuriosInventory(player).flatMap(h -> h.getStacksHandler(slot))
					.map(s -> s.getStacks().getStackInSlot(0)).orElse(ItemStack.EMPTY);
		}

		@Override
		public void clear(ServerPlayer player) {
			CuriosApi.getCuriosInventory(player).ifPresent(h -> {
				for (ICurioStacksHandler s : h.getCurios().values()) {
					for (int i = 0; i < s.getSlots(); i++) {
						s.getStacks().setStackInSlot(i, ItemStack.EMPTY);
						s.getCosmeticStacks().setStackInSlot(i, ItemStack.EMPTY);
					}
				}
			});
		}
	}
}

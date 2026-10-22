package com.chaos.gravestone.compat;

import com.chaos.gravestone.storage.FixedSlotsProvider;
import com.tiviacz.travelersbackpack.capability.AttachmentUtils;
import com.tiviacz.travelersbackpack.capability.ITravelersBackpack;
import com.tiviacz.travelersbackpack.TravelersBackpack;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * La mochila que se lleva puesta con Traveler's Backpack (su propia ranura de espalda). Sus objetos van dentro
 * de la propia mochila, así que basta con guardarla entera. Si la integración con un mod de accesorios está
 * activa la mochila vive en esa ranura y la guarda el provider de ese mod.
 */
public final class TravelersBackpackProvider extends FixedSlotsProvider {

	public static final String ID = "travelersbackpack";

	@Override
	public String id() {
		return ID;
	}

	@Override
	protected boolean keepsOnDeath(ServerPlayer player) {
		return TravelersBackpack.enableIntegration();
	}

	@Override
	protected int size(ServerPlayer player) {
		return 1;
	}

	@Override
	protected ItemStack get(ServerPlayer player, int slot) {
		return AttachmentUtils.isWearingBackpack(player) ? AttachmentUtils.getWearingBackpack(player) : ItemStack.EMPTY;
	}

	@Override
	protected void set(ServerPlayer player, int slot, ItemStack stack) {
		if (stack.isEmpty()) {
			AttachmentUtils.getAttachment(player).ifPresent(ITravelersBackpack::remove);
		} else {
			AttachmentUtils.equipBackpack(player, stack);
		}
	}
}

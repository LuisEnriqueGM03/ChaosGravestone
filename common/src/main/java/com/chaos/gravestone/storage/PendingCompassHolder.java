package com.chaos.gravestone.storage;

import java.util.List;

import net.minecraft.world.item.ItemStack;

/**
 * Brújulas de lápida que esperan a que el jugador reaparezca. Lo implementa ServerPlayer
 * (ServerPlayerMixin), que las guarda en su NBT y las pasa al jugador nuevo al reaparecer.
 */
public interface PendingCompassHolder {

	/** Lista modificable. */
	List<ItemStack> chaosgravestone$pendingCompasses();
}

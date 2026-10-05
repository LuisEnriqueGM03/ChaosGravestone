package com.chaos.gravestone.storage;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;

/** Una fuente de objetos del jugador (inventario vanilla, un mod de accesorios, etc.). */
public interface SlotProvider {

	/** Identificador estable; se guarda en el NBT de la lápida. */
	String id();

	/** Lee y vacía las ranuras de esta fuente. */
	List<StoredEntry> capture(ServerPlayer player);

	/** Devuelve el objeto a su ranura. {@code false} si la ranura está ocupada o ya no existe. */
	boolean restore(ServerPlayer player, StoredEntry entry);
}

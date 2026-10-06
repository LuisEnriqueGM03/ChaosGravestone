package com.chaos.gravestone.platform;

import java.nio.file.Path;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;

/** Lo poco que depende del cargador. Implementado en fabric y neoforge (ServiceLoader). */
public interface Platform {

	/** "fabric" o "neoforge". */
	String name();

	Path configDir();

	boolean isModLoaded(String modId);

	/** Constructor de pestaña creativa (vanilla exige fila/columna; cada cargador da el suyo). */
	CreativeModeTab.Builder creativeTabBuilder();

	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
}

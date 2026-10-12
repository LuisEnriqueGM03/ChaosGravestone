package com.chaos.gravestone.platform;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiFunction;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Lo poco que depende del cargador. Implementado en fabric y neoforge (ServiceLoader). */
public interface Platform {

	/** "fabric" o "neoforge". */
	String name();

	Path configDir();

	boolean isModLoaded(String modId);

	/** Constructor de pestaña creativa (vanilla exige fila/columna; cada cargador da el suyo). */
	CreativeModeTab.Builder creativeTabBuilder();

	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);

	/** Desde 1.21.2 el constructor vanilla es privado; cada cargador lo expone a su manera. */
	<T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory,
			Set<Block> blocks);
}

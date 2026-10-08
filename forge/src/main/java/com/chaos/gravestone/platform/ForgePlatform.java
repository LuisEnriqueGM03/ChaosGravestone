package com.chaos.gravestone.platform;

import java.nio.file.Path;
import java.util.Set;
import java.util.function.BiFunction;

import com.chaos.gravestone.ForgeNetwork;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

public class ForgePlatform implements Platform {

	@Override
	public String name() {
		return "forge";
	}

	@Override
	public Path configDir() {
		return FMLPaths.CONFIGDIR.get();
	}

	@Override
	public boolean isModLoaded(String modId) {
		return ModList.get().isLoaded(modId);
	}

	@Override
	public CreativeModeTab.Builder creativeTabBuilder() {
		return CreativeModeTab.builder();
	}

	@Override
	public void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
		ForgeNetwork.sendToPlayer(player, payload);
	}

	@Override
	public <T extends BlockEntity> BlockEntityType<T> blockEntityType(BiFunction<BlockPos, BlockState, T> factory,
			Set<Block> blocks) {
		return new BlockEntityType<>(factory::apply, blocks);
	}
}

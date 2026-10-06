package com.chaos.gravestone.platform;

import java.nio.file.Path;

import com.chaos.gravestone.ForgeNetwork;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.CreativeModeTab;
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
}

package com.chaos.gravestone;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.chaos.gravestone.config.GravestoneConfig;
import com.chaos.gravestone.storage.ProviderRegistry;
import com.chaos.gravestone.storage.StoredEntry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Containers;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockState;

public final class GravestoneManager {

	private static final int SEARCH_RADIUS = 4;

	private GravestoneManager() {}

	/** Llamado al inicio de {@code ServerPlayer#die}, antes de que se suelte ningún objeto. */
	public static void onPlayerDeath(ServerPlayer player) {
		ServerLevel level = player.serverLevel();
		if (player.isSpectator() || level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			return;
		}
		// Se lee antes de vaciar nada: al inicio de die() el combat tracker aún tiene la causa.
		Component cause = player.getCombatTracker().getDeathMessage();
		List<StoredEntry> contents = ProviderRegistry.captureAll(player);
		if (contents.isEmpty()) {
			return;
		}

		BlockPos spot = findSpot(level, player.blockPosition());
		if (spot == null) {
			// Sin hueco: comportamiento vanilla (todo al suelo) en vez de perderlo.
			contents.forEach(e -> Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), e.stack()));
			return;
		}

		BlockState state = ModBlocks.get(GravestoneVariant.choose(level, spot)).defaultBlockState()
				.setValue(GravestoneBlock.FACING, player.getDirection().getOpposite());
		level.setBlockAndUpdate(spot, state);
		if (level.getBlockEntity(spot) instanceof GravestoneBlockEntity grave) {
			grave.init(player, contents, cause);
			GravestoneCompass.addPending(player.getUUID(),
					new GravestoneCompass.GraveRef(grave.getGraveId(), GlobalPos.of(level.dimension(), spot)));
		}

		if (GravestoneConfig.get().showCoordinates) {
			player.sendSystemMessage(Component.translatable("message.chaosgravestone.created",
					spot.getX(), spot.getY(), spot.getZ(), level.dimension().location().toString()));
		}
	}

	/** Hueco más cercano al punto de muerte: reemplazable y sin lava. */
	@Nullable
	private static BlockPos findSpot(ServerLevel level, BlockPos origin) {
		int minY = level.getMinBuildHeight() + 1;
		int maxY = level.getMaxBuildHeight() - 2;
		BlockPos start = new BlockPos(origin.getX(), Math.max(minY, Math.min(origin.getY(), maxY)), origin.getZ());
		BlockPos best = null;
		double bestDist = Double.MAX_VALUE;
		for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; dx++) {
			for (int dy = -SEARCH_RADIUS; dy <= SEARCH_RADIUS; dy++) {
				for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; dz++) {
					BlockPos pos = start.offset(dx, dy, dz);
					if (pos.getY() < minY || pos.getY() > maxY) {
						continue;
					}
					double dist = pos.distSqr(start);
					if (dist >= bestDist) {
						continue;
					}
					BlockState state = level.getBlockState(pos);
					if (state.canBeReplaced() && !state.getFluidState().is(FluidTags.LAVA)) {
						best = pos;
						bestDist = dist;
					}
				}
			}
		}
		return best;
	}
}

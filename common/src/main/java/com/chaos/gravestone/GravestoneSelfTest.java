package com.chaos.gravestone;
import java.util.UUID;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
/** Shared inventory and persistence assertions for visual and headless development tests. */
public final class GravestoneSelfTest {
    private GravestoneSelfTest() {}
    /** Exercises the actual capture, save/load and restoration path in a disposable test world. */
    public static void run(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        player.getInventory().clearContent();
        ItemStack named = new ItemStack(Items.DIAMOND_SWORD);
        named.setHoverName(Component.literal("NBT preservation test"));
        player.getInventory().setItem(0, named.copy());
        player.getInventory().setItem(39, new ItemStack(Items.IRON_HELMET));
        player.getInventory().setItem(40, new ItemStack(Items.TORCH, 32));

        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY).set(true, level.getServer());
        com.chaos.gravestone.GravestoneManager.onPlayerDeath(player);
        if (!ItemStack.matches(named, player.getInventory().getItem(0))) throw new IllegalStateException("keepInventory was ignored");
        level.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY).set(false, level.getServer());
        com.chaos.gravestone.GravestoneManager.onPlayerDeath(player);
        if (!(level.getBlockEntity(pos) instanceof GravestoneBlockEntity grave)) throw new IllegalStateException("Death did not create grave");
        if (!player.getInventory().isEmpty() || grave.getItemCount() != 3) throw new IllegalStateException("Inventory capture failed");
        var ref = new GravestoneCompass.GraveRef(grave.getGraveId(), GlobalPos.of(level.dimension(), pos));
        ItemStack compass = GravestoneCompass.create(ref);
        var pending = com.chaos.gravestone.storage.PendingCompasses.get(player.server);
        var reloaded = com.chaos.gravestone.storage.PendingCompasses.load(pending.save(new net.minecraft.nbt.CompoundTag()));
        if (reloaded.take(player.getUUID()).stream().noneMatch(stack -> ItemStack.matches(stack, compass)))
            throw new IllegalStateException("Pending compass persistence failed");
        GravestoneCompass.onRespawn(player);
        if (player.getInventory().countItem(ModBlocks.GRAVE_COMPASS) != 1) throw new IllegalStateException("Respawn compass delivery failed");

        net.minecraft.nbt.CompoundTag saved = grave.saveWithFullMetadata();
        if (grave.getUpdateTag().contains("entries")) throw new IllegalStateException("Inventory leaked to client");
        grave.load(saved);
        // Occupy every nearby destination and the block above the grave: teleport must fail safely.
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, -1, -2), pos.offset(2, 3, 2))) {
            if (!p.equals(pos)) level.setBlockAndUpdate(p, Blocks.STONE.defaultBlockState());
        }
        if (GravestoneCompass.teleportToGrave(player, ref)) throw new IllegalStateException("Unsafe teleport accepted");
        if (GravestoneCompass.teleportToGrave(player, new GravestoneCompass.GraveRef(UUID.randomUUID(), ref.pos())))
            throw new IllegalStateException("Replaced grave identity accepted");
        // Delivery compass occupied slot 0. Clear it to check the exact original slots.
        player.getInventory().clearContent();
        grave.restoreTo(player);
        if (!ItemStack.matches(named, player.getInventory().getItem(0))
                || !player.getInventory().getItem(39).is(Items.IRON_HELMET)
                || player.getInventory().getItem(40).getCount() != 32 || grave.getItemCount() != 0)
            throw new IllegalStateException("Grave save/load or exact slot restoration failed");
        grave.restoreTo(player);
        if (player.getInventory().countItem(Items.DIAMOND_SWORD) != 1) throw new IllegalStateException("Duplicate restoration");
        level.removeBlock(pos, false);
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 3, 2)))
            level.setBlockAndUpdate(p, Blocks.AIR.defaultBlockState());
        ChaosGravestone.LOGGER.info("[selftest] inventory: capture, keepInventory, NBT, exact slots, persistence, unsafe teleport, UUID, no duplicates -> OK");
    }

}

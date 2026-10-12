package com.chaos.gravestone.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

/** World-scoped delivery queue: survives restarts while a player is on the death screen. */
public final class PendingCompasses extends SavedData {
    private static final String NAME = "chaosgravestone_pending_compasses";
    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();

    public static PendingCompasses get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(PendingCompasses::load, PendingCompasses::new, NAME);
    }

    public void add(UUID player, ItemStack stack) {
        pending.computeIfAbsent(player, ignored -> new ArrayList<>()).add(stack.copy());
        setDirty();
    }

    public List<ItemStack> take(UUID player) {
        List<ItemStack> stacks = pending.remove(player);
        if (stacks != null) setDirty();
        return stacks == null ? List.of() : stacks;
    }

    public static PendingCompasses load(CompoundTag tag) {
        PendingCompasses data = new PendingCompasses();
        for (Tag raw : tag.getList("players", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) raw;
            if (!entry.hasUUID("player")) continue;
            List<ItemStack> stacks = new ArrayList<>();
            for (Tag item : entry.getList("stacks", Tag.TAG_COMPOUND)) {
                ItemStack stack = ItemStack.of((CompoundTag) item);
                if (!stack.isEmpty()) stacks.add(stack);
            }
            data.pending.put(entry.getUUID("player"), stacks);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag players = new ListTag();
        pending.forEach((id, stacks) -> {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("player", id);
            ListTag items = new ListTag();
            stacks.forEach(stack -> items.add(stack.save(new CompoundTag())));
            entry.put("stacks", items);
            players.add(entry);
        });
        tag.put("players", players);
        return tag;
    }
}

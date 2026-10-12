package com.chaos.gravestone.storage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
/** World-scoped delivery including all item components, surviving server restarts. */
public final class PendingCompasses extends SavedData {
    public static final Codec<PendingCompasses> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, ItemStack.CODEC.listOf())
        .fieldOf("pending").codec().xmap(PendingCompasses::new, data -> data.pending);
    public static final SavedDataType<PendingCompasses> TYPE = new SavedDataType<>(
        Identifier.fromNamespaceAndPath("chaosgravestone", "pending_compasses"), PendingCompasses::new, CODEC, null);
    private final Map<UUID, List<ItemStack>> pending = new HashMap<>();
    public PendingCompasses() {}
    private PendingCompasses(Map<UUID, List<ItemStack>> source) {
        source.forEach((id, stacks) -> pending.put(id, new ArrayList<>(stacks)));
    }
    public static PendingCompasses get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
    public void add(UUID id, ItemStack stack) {
        pending.computeIfAbsent(id, ignored -> new ArrayList<>()).add(stack.copy());
        setDirty();
    }
    public List<ItemStack> take(UUID id) {
        List<ItemStack> stacks = pending.remove(id);
        if (stacks != null) setDirty();
        return stacks == null ? List.of() : stacks;
    }
}

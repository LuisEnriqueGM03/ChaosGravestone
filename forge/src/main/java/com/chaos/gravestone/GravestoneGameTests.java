package com.chaos.gravestone;
import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.common.util.FakePlayerFactory;
@GameTestHolder(ChaosGravestone.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GravestoneGameTests {
    @GameTest(templateNamespace = "chaosgravestone", template = "empty", timeoutTicks = 100)
    public static void inventoryRoundtrip(GameTestHelper helper) {
        var player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "GraveTest"));
        BlockPos pos = helper.absolutePos(new BlockPos(3, 3, 3));
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        GravestoneSelfTest.run(player);
        helper.succeed();
    }
}

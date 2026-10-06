package com.chaos.gravestone.item;

import java.util.List;

import com.chaos.gravestone.GravestoneCompass;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Brújula de lápida + 4 perlas de ender. Se carga manteniendo clic derecho 5 s y
 * teletransporta a la lápida vinculada (ver GravestoneCompass#teleportToGrave).
 */
public class EnderGraveCompassItem extends Item {

	public static final int CHARGE_TICKS = 100;
	private static final int COOLDOWN_TICKS = 60;

	public EnderGraveCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		GraveCompassItem.addGraveTooltip(stack, tooltip, "tooltip.chaosgravestone.ender_grave_compass");
	}

	@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity) {
		return CHARGE_TICKS;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.BOW;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (GravestoneCompass.read(stack).isEmpty()) {
			if (!level.isClientSide) {
				player.displayClientMessage(Component.translatable("message.chaosgravestone.unlinked"), true);
			}
			return InteractionResultHolder.fail(stack);
		}
		player.startUsingItem(hand);
		if (!level.isClientSide) {
			level.playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.3F, 1.4F);
		}
		return InteractionResultHolder.consume(stack);
	}

	/** Mientras carga, partículas de portal que se intensifican. */
	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
		if (!level.isClientSide) {
			return;
		}
		int count = 1 + (CHARGE_TICKS - remaining) / 25;
		for (int i = 0; i < count; i++) {
			level.addParticle(ParticleTypes.PORTAL,
					entity.getRandomX(0.8), entity.getRandomY(), entity.getRandomZ(0.8),
					(level.random.nextDouble() - 0.5) * 2, -level.random.nextDouble(),
					(level.random.nextDouble() - 0.5) * 2);
		}
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		if (!level.isClientSide && entity instanceof ServerPlayer player) {
			GravestoneCompass.read(stack).ifPresent(ref -> {
				if (GravestoneCompass.teleportToGrave(player, ref)) {
					player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
				}
			});
		}
		return stack;
	}
}

package com.chaos.gravestone.item;

import java.util.List;

import com.chaos.gravestone.GravestoneCompass;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Brújula de lápida: su descripción muestra dónde está la tumba (coordenadas y dimensión). */
public class GraveCompassItem extends Item {

	public GraveCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
		addGraveTooltip(stack, tooltip, "tooltip.chaosgravestone.grave_compass");
	}

	/** Coordenadas y dimensión de la tumba + descripción (claves de idioma {@code <prefix>.1}, {@code .2}). */
	static void addGraveTooltip(ItemStack stack, List<Component> tooltip, String prefix) {
		GravestoneCompass.read(stack).ifPresentOrElse(ref -> {
			BlockPos pos = ref.pos().pos();
			ResourceLocation dim = ref.pos().dimension().location();
			Component dimName = Component.translatableWithFallback(
					"dimension.chaosgravestone." + dim.getPath(), dim.toString());
			tooltip.add(Component.translatable("tooltip.chaosgravestone.death_at",
					pos.getX(), pos.getY(), pos.getZ()).withStyle(ChatFormatting.GOLD));
			tooltip.add(Component.translatable("tooltip.chaosgravestone.dimension", dimName)
					.withStyle(ChatFormatting.GRAY));
		}, () -> tooltip.add(Component.translatable("tooltip.chaosgravestone.unlinked")
				.withStyle(ChatFormatting.RED)));
		tooltip.add(Component.translatable(prefix + ".1").withStyle(ChatFormatting.DARK_GRAY));
		tooltip.add(Component.translatable(prefix + ".2").withStyle(ChatFormatting.DARK_GRAY));
	}
}

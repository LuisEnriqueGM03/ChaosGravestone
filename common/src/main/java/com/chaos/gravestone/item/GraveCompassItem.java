package com.chaos.gravestone.item;

import java.util.List;

import com.chaos.gravestone.GravestoneCompass;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Brújula de lápida: su descripción muestra dónde está la tumba (coordenadas y dimensión). */
public class GraveCompassItem extends Item {

	public GraveCompassItem(Properties properties) {
		super(properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, net.minecraft.world.item.component.TooltipDisplay display, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
		addGraveTooltip(stack, tooltip, "tooltip.chaosgravestone.grave_compass");
	}

	/** Coordenadas y dimensión de la tumba + descripción (claves de idioma {@code <prefix>.1}, {@code .2}). */
	static void addGraveTooltip(ItemStack stack, java.util.function.Consumer<Component> tooltip, String prefix) {
		GravestoneCompass.read(stack).ifPresentOrElse(ref -> {
			BlockPos pos = ref.pos().pos();
			Identifier dim = ref.pos().dimension().identifier();
			Component dimName = Component.translatableWithFallback(
					"dimension.chaosgravestone." + dim.getPath(), dim.toString());
			tooltip.accept(Component.translatable("tooltip.chaosgravestone.death_at",
					pos.getX(), pos.getY(), pos.getZ()).withStyle(style -> style.withColor(0xFFAA00)));
			tooltip.accept(Component.translatable("tooltip.chaosgravestone.dimension", dimName)
					.withStyle(style -> style.withColor(0xAAAAAA)));
		}, () -> tooltip.accept(Component.translatable("tooltip.chaosgravestone.unlinked")
				.withStyle(style -> style.withColor(0xFF5555))));
		tooltip.accept(Component.translatable(prefix + ".1").withStyle(style -> style.withColor(0x555555)));
		tooltip.accept(Component.translatable(prefix + ".2").withStyle(style -> style.withColor(0x555555)));
	}
}

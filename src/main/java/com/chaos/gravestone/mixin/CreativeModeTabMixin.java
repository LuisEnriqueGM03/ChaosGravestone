package com.chaos.gravestone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.chaos.gravestone.ModBlocks;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

/** Vanilla guarda el icono de la pestaña la primera vez; para la nuestra se pide cada vez, y así rota. */
@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin {

	@Inject(method = "getIconItem", at = @At("HEAD"), cancellable = true)
	private void chaosgravestone$cycleIcon(CallbackInfoReturnable<ItemStack> cir) {
		if ((Object) this == ModBlocks.TAB) {
			cir.setReturnValue(ModBlocks.tabIcon());
		}
	}
}

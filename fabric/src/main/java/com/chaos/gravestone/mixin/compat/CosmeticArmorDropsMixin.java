package com.chaos.gravestone.mixin.compat;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaos.gravestone.compat.CosmeticArmorSupport;
import com.chaos.gravestone.storage.ProviderRegistry;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Cosmetic Armor Reworked Forked (Fabric) suelta sus objetos en ALLOW_DEATH, antes de que la muerte sea segura y
 * antes de que se cree la lápida. Si la lápida va a guardarlos (provider registrado), no se sueltan aquí: los
 * captura CosmeticArmorProvider al morir y, si otro mod cancela la muerte, se quedan puestos.
 * Opcional: si el mod no está, el mixin no se aplica.
 */
@Pseudo
@Mixin(targets = "lain.mods.cos.impl.InventoryManager", remap = false)
public abstract class CosmeticArmorDropsMixin {

	@Inject(method = "handlePlayerDrops", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
	private void chaosgravestone$keepForGrave(Player player, Consumer<ItemEntity> drops, boolean flag, CallbackInfo ci) {
		if (ProviderRegistry.get(CosmeticArmorSupport.ID) != null) {
			ci.cancel();
		}
	}
}

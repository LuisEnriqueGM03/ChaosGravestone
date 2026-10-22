package com.chaos.gravestone.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.chaos.gravestone.GravestoneCompass;
import com.chaos.gravestone.GravestoneManager;
import com.chaos.gravestone.storage.PendingCompassHolder;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements PendingCompassHolder {

	@Unique
	private static final String CHAOSGRAVESTONE$PENDING_KEY = "chaosgravestone:pending_compasses";

	@Unique
	private final List<ItemStack> chaosgravestone$pending = new ArrayList<>();

	@Override
	public List<ItemStack> chaosgravestone$pendingCompasses() {
		return chaosgravestone$pending;
	}

	/**
	 * Fabric: aquí se crea la lápida (los mods que evitan la muerte ya actuaron antes, en
	 * ALLOW_DEATH). NeoForge y Forge la crean en LivingDeathEvent para respetar a quien la cancele.
	 */
	@Inject(method = "die", at = @At("HEAD"))
	private void chaosgravestone$onDie(DamageSource source, CallbackInfo ci) {
		if (!GravestoneManager.loaderHandlesDeath) {
			GravestoneManager.onPlayerDeath((ServerPlayer) (Object) this);
		}
	}

	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	private void chaosgravestone$savePending(CompoundTag tag, CallbackInfo ci) {
		if (chaosgravestone$pending.isEmpty()) {
			return;
		}
		ServerPlayer self = (ServerPlayer) (Object) this;
		ListTag list = new ListTag();
		for (ItemStack stack : chaosgravestone$pending) {
			if (!stack.isEmpty()) {
				list.add(stack.save(self.registryAccess()));
			}
		}
		tag.put(CHAOSGRAVESTONE$PENDING_KEY, list);
	}

	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	private void chaosgravestone$loadPending(CompoundTag tag, CallbackInfo ci) {
		chaosgravestone$pending.clear();
		ServerPlayer self = (ServerPlayer) (Object) this;
		for (Tag t : tag.getList(CHAOSGRAVESTONE$PENDING_KEY, Tag.TAG_COMPOUND)) {
			ItemStack.parse(self.registryAccess(), t).ifPresent(chaosgravestone$pending::add);
		}
	}

	/** Al reaparecer se crea un ServerPlayer nuevo: se le pasan las brújulas pendientes del viejo. */
	@Inject(method = "restoreFrom", at = @At("TAIL"))
	private void chaosgravestone$copyPending(ServerPlayer old, boolean keepEverything, CallbackInfo ci) {
		chaosgravestone$pending.clear();
		chaosgravestone$pending.addAll(((PendingCompassHolder) old).chaosgravestone$pendingCompasses());
	}

	/** Una brújula vinculada que se tira desaparece (ver GravestoneCompass#vanishOnToss). */
	@Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",
			at = @At("HEAD"), cancellable = true)
	private void chaosgravestone$vanishCompass(ItemStack stack, boolean dropAround, boolean includeThrowerName,
			CallbackInfoReturnable<ItemEntity> cir) {
		if (GravestoneCompass.vanishOnToss((ServerPlayer) (Object) this, stack)) {
			cir.setReturnValue(null);
		}
	}
}

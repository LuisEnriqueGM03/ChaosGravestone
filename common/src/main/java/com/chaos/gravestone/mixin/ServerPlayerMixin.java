package com.chaos.gravestone.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.chaos.gravestone.GravestoneManager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

	@Inject(method = "die", at = @At("HEAD"))
	private void chaosgravestone$onDie(DamageSource source, CallbackInfo ci) {
		GravestoneManager.onPlayerDeath((ServerPlayer) (Object) this);
	}
}

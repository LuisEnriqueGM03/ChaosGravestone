package com.chaos.gravestone.client;

import java.lang.reflect.Field;

import com.chaos.gravestone.ChaosGravestone;
import com.chaos.gravestone.ModBlocks;
import com.chaos.gravestone.ModParticles;

import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.loot.LootModifierManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/** Parte de cliente de Forge: renderer de la lápida, partículas y aguja de las brújulas. */
public final class ChaosGravestoneForgeClient {

	private ChaosGravestoneForgeClient() {}

	/**
	 * Solo autoprueba: en el entorno de desarrollo de Forge 1.21.1 (ForgeGradle) no llegan los eventos de
	 * arranque del servidor (AddReloadListenerEvent, ServerStartingEvent...), así que Forge nunca crea su
	 * LootModifierManager y cualquier botín (hojas que se pudren, bloques que se rompen) tumba el servidor.
	 * Pasa también sin este mod. Se deja uno vacío; si el evento llega, Forge lo sustituye.
	 */
	private static void devLootWorkaround() {
		try {
			Field field = Class.forName("net.minecraftforge.common.ForgeInternalHandler").getDeclaredField("INSTANCE");
			field.setAccessible(true);
			if (field.get(null) == null) {
				field.set(null, new LootModifierManager(RegistryAccess.EMPTY));
			}
		} catch (ReflectiveOperationException e) {
			ChaosGravestone.LOGGER.warn("[selftest] no se pudo preparar el LootModifierManager de Forge", e);
		}
	}

	public static void init(IEventBus modBus) {
		modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
				event.registerBlockEntityRenderer(ModBlocks.GRAVESTONE_ENTITY, GravestoneRenderer::new));
		modBus.addListener((RegisterParticleProvidersEvent event) -> {
			event.registerSpriteSet(ModParticles.SKULL, SkullParticle.Provider::gray);
			event.registerSpriteSet(ModParticles.GOLD_SKULL, SkullParticle.Provider::gold);
		});
		modBus.addListener((FMLClientSetupEvent event) ->
				event.enqueueWork(ChaosGravestoneClient::registerItemProperties));

		if (SelfTest.enabled()) {
			devLootWorkaround();
			MinecraftForge.EVENT_BUS.addListener((TickEvent.ClientTickEvent.Post event) ->
					SelfTest.tick(Minecraft.getInstance()));
			// Simula un mod de revivir: cancela la muerte antes que nadie y deja al jugador con vida.
			MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, LivingDeathEvent.class, event -> {
				if (LogicTest.cancelNextDeath && event.getEntity() instanceof ServerPlayer player) {
					player.setHealth(1.0F);
					event.setCanceled(true);
				}
			});
		}
	}
}

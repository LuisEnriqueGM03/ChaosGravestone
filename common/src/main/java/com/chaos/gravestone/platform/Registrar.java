package com.chaos.gravestone.platform;

import net.minecraft.resources.ResourceLocation;

/**
 * Registra un objeto en un registro concreto. Fabric lo hace directo con Registry.register;
 * NeoForge dentro de su RegisterEvent (allí los registros se congelan al arrancar).
 */
@FunctionalInterface
public interface Registrar<T> {

	void register(ResourceLocation id, T value);
}

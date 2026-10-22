package com.chaos.gravestone.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.chaos.gravestone.ChaosGravestone;

/**
 * Lo común de Cosmetic Armor Reworked (original en Forge/NeoForge y su fork en Fabric): misma API
 * ({@code lain.mods.cos.api}) en los tres cargadores. Su opción "CosArmorKeepThroughDeath" no es API y cambia
 * según el cargador, así que se lee por reflexión; si falla, se asume que no conserva nada.
 */
public final class CosmeticArmorSupport {

	public static final String ID = "cosmeticarmor";

	private CosmeticArmorSupport() {}

	/** {@code true} si Cosmetic Armor está configurado para conservar sus ranuras al morir. */
	public static boolean keepsThroughDeath() {
		try {
			Class<?> configs = Class.forName("lain.mods.cos.impl.ModConfigs");
			// Fork de Fabric: getter estático.
			for (Method method : configs.getMethods()) {
				if (method.getName().equals("getCosArmorKeepThroughDeath") && method.getParameterCount() == 0) {
					return Boolean.TRUE.equals(method.invoke(null));
				}
			}
			// Forge/NeoForge: BooleanValue del ConfigSpec con get().
			Field field = configs.getField("CosArmorKeepThroughDeath");
			Object value = field.get(null);
			return value != null && Boolean.TRUE.equals(value.getClass().getMethod("get").invoke(value));
		} catch (ReflectiveOperationException | RuntimeException e) {
			ChaosGravestone.LOGGER.debug("No se pudo leer CosArmorKeepThroughDeath", e);
			return false;
		}
	}
}

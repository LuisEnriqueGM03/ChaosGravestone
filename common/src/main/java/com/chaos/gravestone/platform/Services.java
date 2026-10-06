package com.chaos.gravestone.platform;

import java.util.ServiceLoader;

/** Acceso a la implementación del cargador actual (META-INF/services en cada módulo). */
public final class Services {

	public static final Platform PLATFORM = ServiceLoader.load(Platform.class, Services.class.getClassLoader())
			.findFirst()
			.orElseThrow(() -> new IllegalStateException("No hay implementación de Platform para este cargador"));

	private Services() {}
}

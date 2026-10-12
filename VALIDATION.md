# Validación de Chaos Gravestone 1.1.0 para Minecraft 1.20.1

Autor del mod: **Goddark83**.

Los tres JAR se compilan para Java 17. Fabric requiere Fabric API 0.92.12+1.20.1; los ports de NeoForge y Forge usan 47.1.106 y 47.4.26 respectivamente.

## Comprobaciones realizadas

- Compilación y empaquetado de Fabric, NeoForge y Forge.
- Autoprueba en una partida desechable para cada cargador: captura del inventario, respeto de keepInventory, conservación del NBT, restauración a las ranuras originales y prevención de duplicados.
- Serialización de las brújulas pendientes, validación del UUID de la tumba y rechazo del teletransporte a un lugar obstruido.
- Rotura de la lápida por el dueño con contenido al suelo y eliminación de la brújula.
- Receta de ender con conservación del vínculo, rechazo de un patrón incorrecto y teletransporte al destino seguro.
- Capturas de las variantes, partículas y animación de llegada, sin errores de carga de modelos.
- GameTest en los servidores de Forge y NeoForge: la prueba obligatoria de inventario y persistencia pasó en ambos.
- Inspección de los JAR: autor, versión, Java 17, metadata, refmaps de producción, receta y modelos de brújula.

## Alcance

Las pruebas visuales usan un servidor integrado. Los GameTests usan un servidor de pruebas sin clientes. No se ha realizado una sesión multijugador con dos jugadores ni una prueba de inventarios de accesorios; esas integraciones siguen pendientes.

En este equipo se desactivó la ventana de carga inicial de Forge y NeoForge en sus carpetas de prueba. Esa configuración no forma parte de los JAR publicados.

## Repetir

```bash
./gradlew -Ploader=all build
./gradlew -Ploader=fabric :fabric:runSelftest
./gradlew -Ploader=forge :forge:runSelftest :forge:runGametest
./gradlew -Ploader=neoforge :neoforge:runSelftest :neoforge:runGametest
python tools/upload-releases.py --validate
```

# Chaos Gravestone

Mod de lápidas para **Minecraft 1.20.1**, para **Fabric**, **NeoForge** y **Forge**. Al morir queda una lápida protegida con todo tu inventario; al abrirla, cada objeto vuelve a su ranura.

- Lápida solo para su dueño (configurable); si la rompe, todo cae al suelo.
- 18 estilos que se eligen según bioma, profundidad y dimensión, con nombre, fecha, hora y causa de la muerte grabados.
- **Brújula de lápida:** apunta a tu tumba y la guía con un rastro de calaveras.
- **Brújula de lápida de ender** (brújula + 4 perlas de ender en cruz): mantén clic derecho 5 s para teletransportarte a la tumba.
- Pestaña creativa "Lápidas" para decorar.
- Español e inglés.

Integración con Accessories, Trinkets/Curios, Travelers Backpack y Cosmetic Armor: pendiente.

Autor: **Goddark83**. Las ramas `fabric-1.20.1`, `neoforge-1.20.1` y `forge-1.20.1` seleccionan su cargador de forma predeterminada mediante `default_loader` en `gradle.properties`.

## Estructura

Basada en [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template):

| Módulo | Contenido |
|---|---|
| `common` | Casi todo el mod (lápidas, brújulas, partículas, renderer, recursos). Se compila dentro de cada cargador para Minecraft 1.20.1. |
| `fabric` | Arranque, eventos y red con Fabric API. |
| `neoforge` | Arranque, `RegisterEvent`, eventos y red con NeoForge. |
| `forge` | Arranque, `RegisterEvent`, eventos y red con Forge. Usa ModDev Legacy y comparte el código y los recursos de `common`. |

Lo que depende del cargador pasa por `common/.../platform/Platform` (una implementación por cargador, vía `ServiceLoader`).

Fabric usa Loom; Forge y NeoForge usan ModDev Legacy. NeoForge 1.20.1 conserva los paquetes `net.minecraftforge` y `META-INF/mods.toml`.

## Compilar

Necesita JDK 25 para ejecutar Gradle y JDK 17 instalado. Los tres JAR se compilan para Java 17.

```bash
./gradlew build                       # compila el cargador predeterminado de esta rama
./gradlew -Ploader=all build          # compila Fabric, NeoForge y Forge
./gradlew -Ploader=fabric :fabric:runClient
./gradlew -Ploader=neoforge :neoforge:runClient
./gradlew -Ploader=forge :forge:runClient
./gradlew -Ploader=fabric :fabric:runSelftest
./gradlew -Ploader=neoforge :neoforge:runSelftest
./gradlew -Ploader=forge :forge:runSelftest
./gradlew -Ploader=forge :forge:runGametest
./gradlew -Ploader=neoforge :neoforge:runGametest
```

## Texturas

Las texturas, modelos y blockstates de las lápidas, las partículas y las brújulas los genera `tools/TextureGen.java`:

```bash
java tools/TextureGen.java
```

## Configuración

`config/chaosgravestone.json`: protección, desbloqueo por tiempo, coordenadas al morir y estilo (`"auto"` o un id fijo).

## Licencia

MIT

## Minecraft 1.20.1

Fabric API 0.92.12+1.20.1, NeoForge 47.1.106 y Forge 47.4.26. Instalar el JAR del cargador elegido en cliente y servidor. No mezclar JAR de distintos cargadores.

Las brújulas pendientes se guardan con el mundo para sobrevivir a reinicios. El teletransporte se cancela cuando no hay un lugar seguro y verifica la identidad de la tumba. Si falla la colocación de la lápida, se sueltan los objetos para conservarlos. La protección de rotura se aplica también en creativo.

Las autopruebas visuales crean una partida `selftest` en la carpeta de ejecución del cargador y guardan capturas en `screenshots/`. Las pruebas del servidor usan `run-gametest-1.20.1`. Solo actúan cuando se ejecutan las tareas de desarrollo indicadas.

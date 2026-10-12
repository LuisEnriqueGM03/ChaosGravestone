# Chaos Gravestone

Mod de lápidas para **Minecraft 26.2**, para **Fabric**, **NeoForge** y **Forge**. Al morir queda una lápida protegida con todo tu inventario; al abrirla, cada objeto vuelve a su ranura.

- Lápida solo para su dueño (configurable); si la rompe, todo cae al suelo.
- 18 estilos que se eligen según bioma, profundidad y dimensión, con nombre, fecha, hora y causa de la muerte grabados.
- **Brújula de lápida:** apunta a tu tumba y la guía con un rastro de calaveras.
- **Brújula de lápida de ender** (brújula + 4 perlas de ender en cruz): mantén clic derecho 5 s para teletransportarte a la tumba.
- Pestaña creativa "Lápidas" para decorar.
- Español e inglés.

Integración con Accessories, Trinkets/Curios, Travelers Backpack y Cosmetic Armor: pendiente.

Autor: **Goddark83**. Las ramas `fabric-26.2`, `neoforge-26.2` y `forge-26.2` seleccionan su cargador de forma predeterminada mediante `default_loader` en `gradle.properties`.

## Estructura

Basada en [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template):

| Módulo | Contenido |
|---|---|
| `common` | Casi todo el mod (lápidas, brújulas, partículas, renderer, recursos). Se compila dentro de cada cargador para Minecraft 26.2. |
| `fabric` | Arranque, eventos y red con Fabric API. |
| `neoforge` | Arranque, `RegisterEvent`, eventos y red con NeoForge. |
| `forge` | Arranque, `RegisterEvent`, eventos y red con Forge. Usa ForgeGradle 7 y comparte el código y los recursos de `common`. |

Lo que depende del cargador pasa por `common/.../platform/Platform` (una implementación por cargador, vía `ServiceLoader`).

Fabric usa Loom sin remapeo, NeoForge usa ModDevGradle y Forge usa ForgeGradle 7. Minecraft 26.2 proporciona nombres oficiales sin ofuscación.

## Compilar

Necesita JDK 25 para compilar y ejecutar Gradle. Los tres JAR se compilan para Java 25.

```bash
./gradlew build                       # compila el cargador predeterminado de esta rama
./gradlew -Ploader=all build          # compila Fabric, NeoForge y Forge
./gradlew -Ploader=fabric :fabric:runClient
./gradlew -Ploader=neoforge :neoforge:runClient
./gradlew -Ploader=forge :forge:runClient
./gradlew -Ploader=fabric :fabric:runSelftest
./gradlew -Ploader=neoforge :neoforge:runSelftest
./gradlew -Ploader=forge :forge:runClient -Pselftest
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

## Minecraft 26.2

Fabric API 0.161.0+26.2, NeoForge 26.2.0.90 y Forge 65.1.3. Instalar el JAR del cargador elegido en cliente y servidor. No mezclar JAR de distintos cargadores.

Las brújulas pendientes se guardan con el mundo para sobrevivir a reinicios. El teletransporte se cancela cuando no hay un lugar seguro y verifica la identidad de la tumba. Si falla la colocación de la lápida, se sueltan los objetos para conservarlos. La protección de rotura se aplica también en creativo.

Las autopruebas visuales crean una partida `selftest` en la carpeta de ejecución del cargador y guardan capturas en `screenshots/`. Solo actúan cuando se ejecutan las tareas de desarrollo indicadas.

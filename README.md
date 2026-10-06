# Chaos Gravestone

Mod de lápidas para **Minecraft 1.21.1**, para **Fabric**, **NeoForge** y **Forge**. Al morir queda una lápida protegida con todo tu inventario; al abrirla, cada objeto vuelve a su ranura.

- Lápida solo para su dueño (configurable); si la rompe, todo cae al suelo.
- 18 estilos que se eligen según bioma, profundidad y dimensión, con nombre, fecha, hora y causa de la muerte grabados.
- **Brújula de lápida:** apunta a tu tumba y la guía con un rastro de calaveras.
- **Brújula de lápida de ender** (brújula + 4 perlas de ender en cruz): mantén clic derecho 5 s para teletransportarte a la tumba.
- Pestaña creativa "Lápidas" para decorar.
- Español e inglés.

Integración con Accessories, Trinkets/Curios, Travelers Backpack y Cosmetic Armor: pendiente.

## Estructura

Basada en [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template):

| Módulo | Contenido |
|---|---|
| `common` | Casi todo el mod (lápidas, brújulas, partículas, renderer, recursos). Compila contra Minecraft vanilla. |
| `fabric` | Arranque, eventos y red con Fabric API. |
| `neoforge` | Arranque, `RegisterEvent`, eventos y red con NeoForge. |
| `forge` | Arranque, `RegisterEvent`, eventos y red con Forge. **Build aparte** (ForgeGradle), que usa el código y los recursos de `common`. |

Lo que depende del cargador pasa por `common/.../platform/Platform` (una implementación por cargador, vía `ServiceLoader`).

Forge va en un build separado para que actualizar Fabric/NeoForge (o Forge) nunca obligue a tocar el otro.

## Compilar

Necesita JDK 25 para ejecutar Gradle (lo exige Fabric Loom) y JDK 21 instalado (el mod se compila para Java 21).

```bash
./gradlew build                      # fabric/build/libs y neoforge/build/libs
./gradlew :fabric:runClient          # cliente de desarrollo Fabric (run/)
./gradlew :neoforge:runClient        # cliente de desarrollo NeoForge (neoforge/run/)
./gradlew :fabric:runSelftest        # autoprueba visual (capturas en run/screenshots)
./gradlew :neoforge:runSelftest      # autoprueba visual (capturas en neoforge/run/screenshots)

./gradlew -p forge build             # forge/build/libs
./gradlew -p forge runClient         # cliente de desarrollo Forge (forge/run/)
./gradlew -p forge runClient -Pselftest   # autoprueba visual (capturas en forge/run/screenshots)
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

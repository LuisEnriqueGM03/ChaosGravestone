# Chaos Gravestone

Mod de lápidas para **Minecraft 1.21.1 (Fabric)**. Al morir queda una lápida protegida con todo tu inventario; al abrirla, cada objeto vuelve a su ranura.

- Lápida solo para su dueño (configurable); si la rompe, todo cae al suelo.
- 18 estilos que se eligen según bioma, profundidad y dimensión, con nombre, fecha, hora y causa de la muerte grabados.
- **Brújula de lápida:** apunta a tu tumba y la guía con un rastro de calaveras.
- **Brújula de lápida de ender** (brújula + 4 perlas de ender en cruz): mantén clic derecho 5 s para teletransportarte a la tumba.
- Pestaña creativa "Lápidas" para decorar.
- Español e inglés.

Integración con Accessories, Trinkets, Travelers Backpack y Cosmetic Armor: pendiente.

## Compilar

Necesita JDK 25 (lo exige Fabric Loom).

```bash
./gradlew build          # build/libs/chaosgravestone-<version>.jar
./gradlew runClient      # cliente de desarrollo
./gradlew runSelftest    # autoprueba visual: capturas en run/screenshots
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

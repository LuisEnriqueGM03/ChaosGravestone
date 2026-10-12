# Validación de Chaos Gravestone 1.1.0 para Minecraft 26.1

Autor: **Goddark83**. Java **25**.

## Cargadores

- Fabric Loader 0.19.5 y Fabric API 0.145.1+26.1.
- NeoForge 26.1.0.19-beta, publicado como beta por el estado del propio cargador.
- Forge 62.0.9.

Las ramas `fabric-26.1`, `neoforge-26.1` y `forge-26.1` seleccionan su cargador mediante `default_loader`. Los metadatos se limitan a Minecraft 26.1 exacta.

## Pruebas

Las autopruebas crean una partida desechable por cargador y comprueban:

- Captura de inventario, armadura y mano secundaria; respeto de keepInventory.
- Guardado y carga de los componentes de objetos y restauración a las ranuras originales sin duplicados.
- Serialización de las brújulas pendientes de entrega.
- Rechazo de un destino obstruido y de una tumba cuyo UUID no coincide.
- Rotura por el dueño con contenido al suelo y eliminación de la brújula.
- Receta de ender vinculada, rechazo de un patrón incorrecto y teletransporte a un destino seguro.
- Capturas del texto de las lápidas, brújulas, variantes y partículas.

Los JAR se inspeccionan para comprobar autor, versión, Java 25, metadata, receta, servicios de plataforma y definiciones de brújula. Al no estar ofuscado Minecraft 26.1, no requieren refmap de obfuscación.

## Alcance

Las pruebas de juego usan un servidor integrado. No se ha realizado una sesión multijugador con dos jugadores ni pruebas con inventarios de accesorios; esas integraciones siguen pendientes.

## Repetir

```bash
./gradlew -Ploader=all build
./gradlew -Ploader=fabric :fabric:runSelftest
./gradlew -Ploader=neoforge :neoforge:runSelftest
./gradlew -Ploader=forge :forge:runClient -Pselftest
python tools/upload-releases.py --validate
```

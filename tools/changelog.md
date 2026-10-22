## Chaos Gravestone 1.2.0 (Minecraft 1.21.1)

### Nuevo
- **Experiencia en la lápida.** Al morir, tu experiencia se guarda en la lápida en vez de caer en orbes y se te devuelve al abrirla. Si alguien rompe la lápida, la experiencia cae como orbes.
  - Configurable en `config/chaosgravestone.json`: `storeExperience` (activarlo o no, por defecto sí) y `experiencePercent` (porcentaje que se guarda, por defecto 100 %).
  - Si mueres sin objetos pero con experiencia, también se crea la lápida.
- **Compatibilidad con mods de accesorios y ranuras extra.** Lo que llevas puesto va a la lápida y vuelve a su misma ranura al abrirla, respetando las reglas de cada mod (objetos que se conservan al morir, maldición de desaparición...):
  - **Fabric:** Trinkets y Accessories (también los dos a la vez).
  - **NeoForge:** Curios (incluidas las ranuras cosméticas). Los mods que usan Curios, como Sophisticated Backpacks o Simple Hats, funcionan con ella.
  - **Fabric, NeoForge y Forge:** Traveler's Backpack (la mochila puesta va entera, con todo lo de dentro) y Cosmetic Armor Reworked (en Fabric, su fork *Cosmetic Armor Reworked Forked*). Simple Hats en Fabric usa Trinkets o Accessories.
  - Todos son opcionales: el mod funciona igual sin ellos.
- **La brújula ya no se pierde.** Las brújulas que esperan a que reaparezcas se guardan con tu personaje: si sales del mundo o se reinicia el servidor antes de reaparecer, te llegan igual (incluida la brújula de ender que llevabas encima).
- **Tirar una brújula de lápida la hace desaparecer** (solo existe para su dueño). Si mueres con ella encima, vuelve a tus manos al reaparecer.

### Cambios y correcciones
- **NeoForge y Forge:** la lápida se crea después de que otros mods hayan podido cancelar la muerte (por ejemplo, mods de revivir). Antes, si otro mod te salvaba, seguías vivo pero con el inventario vacío.
- **El Wither y el Ender Dragon ya no pueden romper lápidas.**
- Al abrir la lápida, cada objeto vuelve a su ranura aunque una brújula ocupe ese hueco (la brújula se aparta a otro sitio libre).
- Autor del mod: Goddark83.

Incluye además: lápida protegida con restauración de objetos a su ranura, 18 estilos según bioma y dimensión, brújula de lápida con rastro de calaveras y brújula de ender que teletransporta.

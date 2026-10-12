import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import javax.imageio.ImageIO;

/**
 * Genera las texturas pixel-art de las lápidas (32x32, 2 píxeles por píxel de modelo),
 * sus blockstates y modelos, y la brújula de calavera.
 * Uso: java tools/TextureGen.java   (desde la raíz del proyecto)
 *
 * Todas las variantes comparten geometría y la posición de la placa, para que el texto
 * del renderer caiga siempre en el mismo sitio. Cada variante aporta paleta, emblema y detalle.
 */
public class TextureGen {

	static final String ASSETS = "common/src/main/resources/assets/chaosgravestone/";
	static final String BLOCK_TEX = ASSETS + "textures/block/";
	static final int S = 32;

	/** Detalle sobre la piedra. VEINS: vetas brillantes (lava, sculk) que en el frente van por el marco. */
	enum Overlay { MOSS, HEAVY_MOSS, SAND, SNOW, CRACKS, VEINS, END, SPECKLE, PETALS }

	enum Emblem { SKULL, CROSS, ANKH, FLOWER, TRIDENT, MUSHROOM, CROWN, GEM }

	/** Paleta y estilo de una variante. El id coincide con GravestoneVariant en el mod. */
	record Variant(String id, int[] stone, int stoneLight, int stoneDark, int crack,
			int[] plate, int plateShadow, int frame, int frameLight,
			int bone, int boneShade, int eye, int glow, Emblem emblem,
			int[] brick, int mortar, int brickLight,
			int[] detail, Overlay overlay) {}

	static final int[] MOSS = { 0x4E7A2B, 0x5F9133, 0x3F6624, 0x6FA33C };

	static final List<Variant> VARIANTS = List.of(
			new Variant("gravestone",
					new int[] { 0xA4A49F, 0xA0A09B, 0xA8A8A3, 0x9D9D98, 0xA4A49F, 0x97978F }, 0xC2C2BC, 0x74747A, 0x6A6A6E,
					new int[] { 0x8E8E8B, 0x878784, 0x95958F }, 0x6E6E6D, 0x2F2D38, 0x46434F,
					0xE4E3DA, 0xBDBCB2, 0x1C1A22, 0x9CF06A, Emblem.SKULL,
					new int[] { 0x4C4B53, 0x45444C, 0x52515A, 0x48474F }, 0x2C2B31, 0x5E5D66,
					MOSS, Overlay.MOSS),
			new Variant("mossy_gravestone",
					new int[] { 0x8C8F84, 0x83877B, 0x92958A, 0x7A7E72, 0x888B80 }, 0xA6A99E, 0x5E6258, 0x55594F,
					new int[] { 0x777A70, 0x70736A, 0x7D8075 }, 0x5A5D54, 0x2E3328, 0x434A3B,
					0xC9CCBE, 0xA3A698, 0x1E2219, 0x9CF06A, Emblem.CROSS,
					new int[] { 0x5D6157, 0x565A50, 0x63675C, 0x52564C }, 0x30332B, 0x6E7267,
					MOSS, Overlay.HEAVY_MOSS),
			new Variant("sandstone_gravestone",
					new int[] { 0xD8C99A, 0xD2C291, 0xDDCFA2, 0xCDBC8A, 0xD5C595 }, 0xEADFB6, 0xAE9A68, 0xA08A58,
					new int[] { 0xC4B27E, 0xBDAA75, 0xCAB985 }, 0xA28E5D, 0x6B4E2A, 0x8A683C,
					0xF0C44A, 0xB8862A, 0x5A3A16, 0xFFE38A, Emblem.ANKH,
					new int[] { 0xBFA673, 0xB69D6A, 0xC7AE7B, 0xB09766 }, 0x8A7045, 0xD3BB88,
					new int[] { 0xE6D7A8, 0xDCCB95, 0xEFE2B8 }, Overlay.SAND),
			new Variant("frost_gravestone",
					new int[] { 0xBFD6E6, 0xB4CCDE, 0xC9DEEC, 0xADC6D9, 0xBAD1E2 }, 0xE6F2FA, 0x86A3BC, 0x7F9BB4,
					new int[] { 0xA5BFD3, 0x9DB7CC, 0xADC7DA }, 0x8199B0, 0x2C3E57, 0x3F5675,
					0xF2F8FC, 0xC9D8E4, 0x1A2638, 0x7FE6FF, Emblem.SKULL,
					new int[] { 0x8FB3D9, 0x86AAD1, 0x99BCE0, 0x82A6CD }, 0x5D7FA6, 0xB2CFEA,
					new int[] { 0xF4F8FB, 0xE6EEF4, 0xFFFFFF }, Overlay.SNOW),
			new Variant("deepslate_gravestone",
					new int[] { 0x4F4F55, 0x48484E, 0x56565C, 0x434349, 0x4C4C52 }, 0x6C6C73, 0x2E2E33, 0x29292D,
					new int[] { 0x3E3E44, 0x38383E, 0x44444A }, 0x2A2A2F, 0x18181C, 0x2C2C32,
					0xCFCFC8, 0xA4A49D, 0x111114, 0xFFC14D, Emblem.SKULL,
					new int[] { 0x3A3A40, 0x34343A, 0x404046, 0x303036 }, 0x1E1E22, 0x4C4C53,
					new int[] { 0x2A2A2E, 0x232327 }, Overlay.CRACKS),
			new Variant("nether_gravestone",
					new int[] { 0x3A3236, 0x342C30, 0x40373B, 0x2E272A, 0x372F33 }, 0x524649, 0x1F191B, 0x1A1416,
					new int[] { 0x2B2326, 0x261F22, 0x30282B }, 0x1C1618, 0x5A1414, 0x7A2020,
					0xD9CFC4, 0xAFA397, 0x150A08, 0xFF4A1F, Emblem.SKULL,
					new int[] { 0x45191D, 0x3D1519, 0x4F1E22, 0x391317 }, 0x240A0C, 0x5E2629,
					new int[] { 0xFF7A1F, 0xFFB43A, 0xE8541A }, Overlay.VEINS),
			new Variant("end_gravestone",
					new int[] { 0x2A1F3A, 0x251B34, 0x302442, 0x21182E, 0x2C2140 }, 0x45345E, 0x150F1F, 0x120C1A,
					new int[] { 0x1F1630, 0x1B132A, 0x241A37 }, 0x130D1D, 0xA77BCA, 0xC79BEA,
					0xE8E2F2, 0xBDB3CF, 0x10091A, 0xD07CFF, Emblem.SKULL,
					new int[] { 0xDCDDA2, 0xD3D497, 0xE3E4AC, 0xCDCE91 }, 0xA9AA78, 0xEDEEBE,
					new int[] { 0xC79BEA, 0xE2C4FF, 0x9E6CC9 }, Overlay.END),
			new Variant("prismarine_gravestone",
					new int[] { 0x5FA597, 0x579B8D, 0x66AE9F, 0x4F9284, 0x63A99B }, 0x8FD1C2, 0x3A6E64, 0x3F7A6D,
					new int[] { 0x3E7468, 0x386C61, 0x447C70 }, 0x2E5A51, 0x1E3B44, 0x2E5763,
					0xF0FFFB, 0xB6E8DC, 0x0F2A2E, 0xA8F0FF, Emblem.TRIDENT,
					new int[] { 0x335B4E, 0x2E5347, 0x3A6456, 0x2A4D41 }, 0x1C3A31, 0x46705F,
					new int[] { 0xD6F5EE, 0xA8E6D9, 0x7FD3C2 }, Overlay.SPECKLE),
			new Variant("cherry_gravestone",
					new int[] { 0xE9D3D6, 0xE2CACD, 0xEFDCDF, 0xDCC2C6, 0xE6CFD2 }, 0xF7E9EB, 0xB89297, 0xC5A2A7,
					new int[] { 0xD8B9BE, 0xD1B0B5, 0xDEC2C6 }, 0xB89297, 0x5C2F3A, 0x7C4452,
					0xFFB7CF, 0xE68AAA, 0x4A1A2A, 0xFFE27A, Emblem.FLOWER,
					new int[] { 0xE0A3A8, 0xD8999F, 0xE8ADB2, 0xD2949A }, 0x9C5E66, 0xF0BFC3,
					new int[] { 0xFFB7CF, 0xF59AB9, 0xFFD1E0 }, Overlay.PETALS),
			new Variant("amethyst_gravestone",
					new int[] { 0x8A62C4, 0x8059BA, 0x9469CE, 0x7651AE, 0x8E66C8 }, 0xC79BF2, 0x553A80, 0x5E4290,
					new int[] { 0x6E4AA3, 0x67449A, 0x7550AC }, 0x4F3577, 0x2A1840, 0x3D2459,
					0xE9D6FF, 0xC2A6E8, 0x1E0F33, 0xFFFFFF, Emblem.GEM,
					new int[] { 0x4D3A66, 0x46345E, 0x55406F, 0x40305A }, 0x2A1F3A, 0x62507E,
					new int[] { 0xF2E6FF, 0xD9B8FF, 0xB98CEB }, Overlay.SPECKLE),
			new Variant("copper_gravestone",
					new int[] { 0x5EA88A, 0x56A082, 0x67B193, 0x4F977A, 0x62AC8E }, 0x8ACFB2, 0x3C7A61, 0x417F66,
					new int[] { 0xB4684D, 0xAA6046, 0xBD7154 }, 0x8C4A34, 0x2F4A40, 0x426B5B,
					0xF0C29A, 0xC98E66, 0x3A1E12, 0x7FF0C8, Emblem.SKULL,
					new int[] { 0xC06C4F, 0xB66447, 0xCA7658, 0xAC5D41 }, 0x6E3624, 0xDB8A6A,
					new int[] { 0x6FC7A5, 0x56B08F, 0x8EDBBB }, Overlay.SPECKLE),
			new Variant("quartz_gravestone",
					new int[] { 0xEDE8E1, 0xE6E1D9, 0xF2EEE8, 0xE0DAD2, 0xEAE5DE }, 0xFFFFFF, 0xBDB4A8, 0xC9C0B5,
					new int[] { 0xDDD5CB, 0xD6CEC3, 0xE3DCD2 }, 0xBDB4A8, 0x8C7E6C, 0xA89A86,
					0xD8B04A, 0xA8832A, 0x5A3F10, 0xFFE58A, Emblem.CROSS,
					new int[] { 0xE3DDD4, 0xDAD3C9, 0xEBE5DD, 0xD4CDC2 }, 0xB2A898, 0xF5F1EB,
					new int[] { 0xC9C0B5, 0xD2C9BE }, Overlay.CRACKS),
			new Variant("gilded_gravestone",
					new int[] { 0x2F2A2E, 0x2A2529, 0x353034, 0x262125, 0x322D31 }, 0x4A4347, 0x161214, 0x1A1618,
					new int[] { 0x262125, 0x221D21, 0x2B2629 }, 0x161214, 0xE0A82E, 0xFFD45E,
					0xFFD45E, 0xC98F1E, 0x3A2606, 0xFFF4B0, Emblem.CROWN,
					new int[] { 0xE8B83A, 0xDDAD30, 0xF2C445, 0xD4A22A }, 0x9C6E12, 0xFFDE6E,
					new int[] { 0xFFD45E, 0xE8B83A, 0xFFF0A0 }, Overlay.SPECKLE),
			new Variant("sculk_gravestone",
					new int[] { 0x1F2E36, 0x1B2930, 0x23343D, 0x18252B, 0x213139 }, 0x31474F, 0x0E171B, 0x0B1418,
					new int[] { 0x15232A, 0x122027, 0x182830 }, 0x0B1418, 0x0F4C5A, 0x1A6E80,
					0x9FD9E3, 0x6FAEBB, 0x05181D, 0x29DFEB, Emblem.SKULL,
					new int[] { 0x2B2E33, 0x26292E, 0x313439, 0x222529 }, 0x15171A, 0x3C4046,
					new int[] { 0x29DFEB, 0x0FA3B5, 0x7FF6FF }, Overlay.VEINS),
			new Variant("crimson_gravestone",
					new int[] { 0x6B1E24, 0x611A20, 0x752329, 0x58171C, 0x6F2026 }, 0x93323A, 0x3E0E12, 0x3A0C10,
					new int[] { 0x5A181D, 0x531519, 0x611B21 }, 0x3E0E12, 0x2A0A0E, 0x4A1218,
					0xE8D8C8, 0xBDA898, 0x220406, 0xFF6A3A, Emblem.SKULL,
					new int[] { 0x7A3550, 0x713049, 0x823A57, 0x6A2C44 }, 0x40182A, 0x96486A,
					new int[] { 0xC4262E, 0xE04A3A, 0xFF8A3A }, Overlay.HEAVY_MOSS),
			new Variant("warped_gravestone",
					new int[] { 0x1F6B66, 0x1C625D, 0x22746F, 0x195954, 0x20706A }, 0x2F958E, 0x10403C, 0x0E3A36,
					new int[] { 0x1A5A55, 0x17534E, 0x1D605B }, 0x10403C, 0x2A0F3A, 0x3E1A52,
					0xD8F0EC, 0xA8CEC8, 0x081A18, 0xFF7AE0, Emblem.SKULL,
					new int[] { 0x2B6B66, 0x27625D, 0x307570, 0x245A55 }, 0x123A37, 0x3A8780,
					new int[] { 0x16C4B0, 0x3FE0CB, 0x0E9A8A }, Overlay.HEAVY_MOSS),
			new Variant("terracotta_gravestone",
					new int[] { 0xA1593A, 0x985335, 0xAA6040, 0x8E4D31, 0x9F583A }, 0xC27A57, 0x6E3A24, 0x6A3722,
					new int[] { 0x8C4C31, 0x84472E, 0x925136 }, 0x6E3A24, 0x3A1E12, 0x56301E,
					0xF2E2C8, 0xCDB89A, 0x2A140A, 0xFFB347, Emblem.SKULL,
					new int[] { 0xBA6630, 0xB05F2B, 0xC46E36, 0xA75828 }, 0x7A3F18, 0xD8854A,
					new int[] { 0xC8743E, 0xD8854A, 0xB86630 }, Overlay.SAND),
			new Variant("mushroom_gravestone",
					new int[] { 0xD9D2C3, 0xD2CBBB, 0xE0DACC, 0xCAC2B2, 0xD6CFC0 }, 0xEEE9DE, 0xA89F8D, 0xB3AA98,
					new int[] { 0xC8BFAE, 0xC0B7A5, 0xCFC6B5 }, 0xA89F8D, 0x7A1E1E, 0x9C2A2A,
					0xC8302A, 0xE6DCC8, 0x3A0A08, 0xF5F0E6, Emblem.MUSHROOM,
					new int[] { 0x6F6168, 0x675A61, 0x776970, 0x605359 }, 0x45393F, 0x85767E,
					new int[] { 0x9C8A94, 0xB8A7B0, 0x7E6E77 }, Overlay.MOSS));

	public static void main(String[] args) throws Exception {
		new File(BLOCK_TEX).mkdirs();
		new File(ASSETS + "models/block").mkdirs();
		new File(ASSETS + "models/item").mkdirs();
		new File(ASSETS + "blockstates").mkdirs();

		// Vista previa: una fila por variante (frente, lado, techo, base, techo de la base).
		BufferedImage preview = new BufferedImage(5 * 34, VARIANTS.size() * 34, BufferedImage.TYPE_INT_ARGB);
		for (int row = 0; row < VARIANTS.size(); row++) {
			Variant v = VARIANTS.get(row);
			BufferedImage[] tex = { front(v), side(v), top(v), base(v, false), base(v, true) };
			String[] names = { "front", "side", "top", "base", "base_top" };
			for (int i = 0; i < tex.length; i++) {
				ImageIO.write(tex[i], "png", new File(BLOCK_TEX + v.id() + "_" + names[i] + ".png"));
				preview.getGraphics().drawImage(tex[i], i * 34 + 1, row * 34 + 1, null);
			}
			writeModels(v.id());
		}
		new File("build").mkdirs();
		ImageIO.write(preview, "png", new File("build/texture-preview.png"));
		compass();
		particles();
		System.out.println(VARIANTS.size() + " variantes generadas; vista previa en build/texture-preview.png");
	}

	static void writeModels(String id) throws Exception {
		write(ASSETS + "models/block/" + id + ".json", """
				{
					"parent": "chaosgravestone:block/gravestone_template",
					"textures": {
						"front": "chaosgravestone:block/%1$s_front",
						"side": "chaosgravestone:block/%1$s_side",
						"top": "chaosgravestone:block/%1$s_top",
						"base": "chaosgravestone:block/%1$s_base",
						"base_top": "chaosgravestone:block/%1$s_base_top",
						"particle": "chaosgravestone:block/%1$s_side"
					}
				}
				""".formatted(id));
		write(ASSETS + "models/item/" + id + ".json", """
				{
					"parent": "chaosgravestone:block/%s"
				}
				""".formatted(id));
		new File(ITEM_DEF).mkdirs();
		itemDefinition(id, "chaosgravestone:item/" + id);
		write(ASSETS + "blockstates/" + id + ".json", """
				{
					"variants": {
						"facing=north": { "model": "chaosgravestone:block/%1$s" },
						"facing=east": { "model": "chaosgravestone:block/%1$s", "y": 90 },
						"facing=south": { "model": "chaosgravestone:block/%1$s", "y": 180 },
						"facing=west": { "model": "chaosgravestone:block/%1$s", "y": 270 }
					}
				}
				""".formatted(id));
	}

	/**
	 * Cara frontal; el modelo usa la región UV [2,1,14,13] = píxeles 4..27 x 2..25.
	 * La placa ocupa las filas 6..23; el emblema 7..13 y el texto (renderer) va debajo.
	 */
	static BufferedImage front(Variant v) {
		BufferedImage img = image();
		Random r = new Random(v.id().hashCode());
		fill(img, r, v.stone(), 0, 0, S, S);
		cracks(img, r, 3, v.crack());

		// Bisel exterior de la losa.
		hline(img, 4, 27, 2, v.stoneLight());
		vline(img, 4, 2, 25, v.stoneLight());
		hline(img, 4, 27, 25, v.stoneDark());
		vline(img, 27, 2, 25, v.stoneDark());

		// Marco y placa.
		rect(img, 6, 4, 25, 24, v.frame());
		hline(img, 7, 24, 5, v.frameLight());
		fill(img, r, v.plate(), 7, 6, 25, 24);
		hline(img, 7, 24, 6, v.plateShadow());
		vline(img, 7, 6, 23, v.plateShadow());

		emblem(img, v, 12, 7);

		switch (v.overlay()) {
			case MOSS -> {
				moss(img, r, v.detail(), 4, 22, 27, 25, 0.35);
				moss(img, r, v.detail(), 4, 2, 6, 8, 0.25);
				moss(img, r, v.detail(), 25, 2, 27, 5, 0.2);
			}
			case HEAVY_MOSS -> {
				moss(img, r, v.detail(), 4, 19, 27, 25, 0.5);
				vines(img, r, v.detail(), 4, 27, 2, 9);
			}
			case SAND -> {
				moss(img, r, v.detail(), 4, 21, 27, 25, 0.45);
				moss(img, r, v.detail(), 4, 2, 27, 25, 0.03);
			}
			case SNOW -> {
				snowCap(img, r, v.detail(), 4, 27, 2, 3);
				moss(img, r, v.detail(), 4, 23, 27, 25, 0.5);
			}
			case CRACKS -> cracks(img, r, 4, v.detail()[0]);
			case VEINS -> lavaCracks(img, r, v.detail(), 3, true);
			case END, SPECKLE -> {
				// Solo fuera de la placa, para no ensuciar el texto.
				moss(img, r, v.detail(), 4, 2, 27, 3, 0.15);
				moss(img, r, v.detail(), 4, 25, 27, 25, 0.15);
				moss(img, r, v.detail(), 4, 2, 5, 25, 0.1);
				moss(img, r, v.detail(), 26, 2, 27, 25, 0.1);
			}
			case PETALS -> {
				moss(img, r, v.detail(), 4, 2, 27, 3, 0.5);
				moss(img, r, v.detail(), 4, 22, 27, 25, 0.3);
				moss(img, r, v.detail(), 4, 2, 5, 25, 0.08);
				moss(img, r, v.detail(), 26, 2, 27, 25, 0.08);
			}
		}
		return img;
	}

	static BufferedImage side(Variant v) {
		BufferedImage img = image();
		Random r = new Random(v.id().hashCode() + 1);
		fill(img, r, v.stone(), 0, 0, S, S);
		cracks(img, r, 2, v.crack());
		switch (v.overlay()) {
			case MOSS, SAND -> {
				for (int y = 18; y < S; y++) {
					moss(img, r, v.detail(), 0, y, S - 1, y, (y - 18) / 16.0);
				}
			}
			case HEAVY_MOSS -> {
				for (int y = 12; y < S; y++) {
					moss(img, r, v.detail(), 0, y, S - 1, y, 0.15 + (y - 12) / 24.0);
				}
				vines(img, r, v.detail(), 0, S - 1, 0, 12);
			}
			case SNOW -> {
				snowCap(img, r, v.detail(), 0, S - 1, 0, 3);
				for (int y = 22; y < S; y++) {
					moss(img, r, v.detail(), 0, y, S - 1, y, (y - 22) / 10.0);
				}
			}
			case CRACKS -> cracks(img, r, 3, v.detail()[0]);
			case VEINS -> lavaCracks(img, r, v.detail(), 2, false);
			case END -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.025);
			case SPECKLE -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.05);
			case PETALS -> {
				snowCap(img, r, v.detail(), 0, S - 1, 0, 2);
				moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.04);
			}
		}
		return img;
	}

	static BufferedImage top(Variant v) {
		BufferedImage img = image();
		Random r = new Random(v.id().hashCode() + 2);
		fill(img, r, v.stone(), 0, 0, S, S);
		switch (v.overlay()) {
			case MOSS -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.08);
			case HEAVY_MOSS -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.45);
			case SAND -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.3);
			case SNOW -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.85);
			case VEINS -> lavaCracks(img, r, v.detail(), 1, false);
			case END -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.03);
			case SPECKLE -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.06);
			case PETALS -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, 0.5);
			case CRACKS -> cracks(img, r, 2, v.detail()[0]);
		}
		return img;
	}

	/** Ladrillo: hiladas de 4 píxeles (2 de modelo), juntas desplazadas. */
	static BufferedImage base(Variant v, boolean top) {
		BufferedImage img = image();
		Random r = new Random(v.id().hashCode() + (top ? 4 : 3));
		fill(img, r, v.brick(), 0, 0, S, S);
		for (int row = 0; row < S / 4; row++) {
			int y = row * 4;
			hline(img, 0, S - 1, y, v.mortar());
			hline(img, 0, S - 1, y + 1, v.brickLight());
			int offset = (row % 2) * 4;
			for (int x = offset; x < S; x += 8) {
				vline(img, x, y, y + 3, v.mortar());
			}
		}
		switch (v.overlay()) {
			case MOSS, HEAVY_MOSS, SAND, SNOW, PETALS -> {
				double chance = switch (v.overlay()) {
					case HEAVY_MOSS -> 0.6;
					case SNOW -> 0.8;
					case SAND -> 0.4;
					case PETALS -> 0.35;
					default -> 0.45;
				};
				if (top) {
					moss(img, r, v.detail(), 0, 0, S - 1, S - 1, chance);
				} else {
					// Cuelga del borde superior.
					for (int x = 0; x < S; x++) {
						int len = r.nextInt(v.overlay() == Overlay.HEAVY_MOSS ? 6 : 4);
						for (int y = 0; y <= len; y++) {
							set(img, x, y, pick(r, v.detail()));
						}
					}
				}
			}
			case VEINS -> lavaCracks(img, r, v.detail(), top ? 2 : 1, false);
			case END -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, top ? 0.03 : 0.0);
			case SPECKLE -> moss(img, r, v.detail(), 0, 0, S - 1, S - 1, top ? 0.06 : 0.02);
			case CRACKS -> cracks(img, r, 2, v.detail()[0]);
		}
		return img;
	}

	/** Emblema de 8x7 en la parte alta de la placa. */
	static void emblem(BufferedImage img, Variant v, int x0, int y0) {
		String[] rows = switch (v.emblem()) {
			case SKULL -> new String[] {
					".BBBBBB.",
					"BBBBBBBB",
					"BEGBBEGB",
					"BEEBBEEB",
					"BBBEEBBs",
					".sBBBBs.",
					".BxBxBx.",
			};
			case CROSS -> new String[] {
					"...BB...",
					"...BB...",
					".BBBBBB.",
					".sBBBBs.",
					"...BB...",
					"...BB...",
					"...sB...",
			};
			case ANKH -> new String[] {
					"...BB...",
					"..B..B..",
					"..B..B..",
					"...BB...",
					"BBBBBBBB",
					"...BB...",
					"...sB...",
			};
			// Flor de cerezo: pétalos B, centro G.
			case FLOWER -> new String[] {
					"..B..B..",
					".BBBBBB.",
					"BBBGGBBB",
					".BBGGBB.",
					".BBBBBB.",
					"..B..B..",
					"...ss...",
			};
			case TRIDENT -> new String[] {
					"B..B..B.",
					"B..B..B.",
					"BBBBBBB.",
					"...B....",
					"...B....",
					"...B....",
					"...s....",
			};
			// Seta: sombrero B con motas G, pie s.
			case MUSHROOM -> new String[] {
					"..BBBB..",
					".BGBBGB.",
					"BBBBBBBB",
					"BGBBBBGB",
					"...ss...",
					"...ss...",
					"..ssss..",
			};
			case CROWN -> new String[] {
					"B..G..B.",
					"BB.B.BB.",
					"BBBBBBB.",
					"BGBGBGB.",
					"BBBBBBB.",
					".sssss..",
					"........",
			};
			case GEM -> new String[] {
					"..BBBB..",
					".BGBBsB.",
					"BGBBBBss",
					".BBBBss.",
					"..BBss..",
					"...Bs...",
					"........",
			};
		};
		for (int y = 0; y < rows.length; y++) {
			for (int x = 0; x < rows[y].length(); x++) {
				int rgb = switch (rows[y].charAt(x)) {
					case 'B' -> v.bone();
					case 's' -> v.boneShade();
					case 'E', 'x' -> v.eye();
					case 'G' -> v.glow();
					default -> -1;
				};
				if (rgb != -1) {
					set(img, x0 + x, y0 + y, rgb);
				}
			}
		}
	}

	// ---- detalles ----

	/** Lianas de musgo que cuelgan desde arriba. */
	static void vines(BufferedImage img, Random r, int[] palette, int x0, int x1, int y0, int maxLen) {
		for (int x = x0; x <= x1; x++) {
			if (r.nextDouble() < 0.4) {
				int len = 1 + r.nextInt(maxLen);
				for (int y = y0; y < y0 + len; y++) {
					set(img, x, y, pick(r, palette));
				}
			}
		}
	}

	/** Capa de nieve irregular en la parte superior. */
	static void snowCap(BufferedImage img, Random r, int[] palette, int x0, int x1, int y0, int depth) {
		for (int x = x0; x <= x1; x++) {
			int d = depth - 1 + r.nextInt(2);
			for (int y = y0; y < y0 + d; y++) {
				set(img, x, y, pick(r, palette));
			}
		}
	}

	/** Grietas con lava brillante. En el frente la lava va por el marco, para no tapar el texto. */
	static void lavaCracks(BufferedImage img, Random r, int[] lava, int count, boolean front) {
		if (front) {
			for (int x = 6; x <= 25; x++) {
				for (int y : new int[] { 4, 24, 25 }) {
					if (r.nextDouble() < 0.3) {
						set(img, x, y, pick(r, lava));
					}
				}
			}
			for (int y = 4; y <= 24; y++) {
				for (int x : new int[] { 5, 6, 25, 26 }) {
					if (r.nextDouble() < 0.2) {
						set(img, x, y, pick(r, lava));
					}
				}
			}
			return;
		}
		for (int i = 0; i < count; i++) {
			int x = 2 + r.nextInt(S - 4);
			int y = 2 + r.nextInt(S - 8);
			for (int step = 0; step < 6; step++) {
				set(img, x, y, pick(r, lava));
				x += r.nextInt(3) - 1;
				y += 1;
			}
		}
	}

	static void cracks(BufferedImage img, Random r, int count, int color) {
		for (int i = 0; i < count; i++) {
			int x = 2 + r.nextInt(S - 4);
			int y = 2 + r.nextInt(S - 4);
			for (int step = 0; step < 5; step++) {
				set(img, x, y, color);
				x += r.nextInt(3) - 1;
				y += 1;
			}
		}
	}

	static void moss(BufferedImage img, Random r, int[] palette, int x0, int y0, int x1, int y1, double chance) {
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (r.nextDouble() < chance) {
					set(img, x, y, pick(r, palette));
				}
			}
		}
	}

	// ---- partícula de calavera ----

	/**
	 * Sprites 8x8 en grises para la partícula de guía. Son opacos: la transparencia y el
	 * desvanecido los pone SkullParticle. Dos variantes: mandíbula cerrada y abierta.
	 */
	static void particles() throws Exception {
		String dir = ASSETS + "textures/particle/";
		new File(dir).mkdirs();
		new File(ASSETS + "particles").mkdirs();
		String[][] sprites = {
				{
						".KKKKKK.",
						"KWWWWWWK",
						"KWEEWEEK",
						"KWEEWEEK",
						"KWWWEWWK",
						".KWWWWK.",
						".KWKWKK.",
						"..KKKK..",
				},
				{
						".KKKKKK.",
						"KWWWWWWK",
						"KWEEWEEK",
						"KWEEWEEK",
						"KWWWEWWK",
						".KWWWWK.",
						"..KKKK..",
						".KWKWKK.",
				},
		};
		// Gris: rastro de la brújula. Dorada: brota de la lápida (W cuerpo, K contorno, E huecos).
		particleSet(dir, "skull", sprites, 0xE2E2E2, 0x7A7A7A, 0x3C3C3C);
		particleSet(dir, "gold_skull", sprites, 0xFFD54A, 0xB8860B, 0x5C3A00);
	}

	static void particleSet(String dir, String name, String[][] sprites, int body, int outline, int holes)
			throws Exception {
		StringBuilder textures = new StringBuilder();
		for (int i = 0; i < sprites.length; i++) {
			BufferedImage img = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
			for (int y = 0; y < 8; y++) {
				for (int x = 0; x < 8; x++) {
					int rgb = switch (sprites[i][y].charAt(x)) {
						case 'W' -> body;
						case 'K' -> outline;
						case 'E' -> holes;
						default -> -1;
					};
					if (rgb != -1) {
						img.setRGB(x, y, 0xFF000000 | rgb);
					}
				}
			}
			// Brillo en la frente de la dorada.
			if (body == 0xFFD54A) {
				img.setRGB(2, 1, 0xFFFFF4C2);
				img.setRGB(3, 1, 0xFFFFF4C2);
			}
			ImageIO.write(img, "png", new File(dir + name + "_" + i + ".png"));
			textures.append(i > 0 ? ", " : "").append("\"chaosgravestone:").append(name).append('_').append(i).append('"');
		}
		write(ASSETS + "particles/" + name + ".json", """
				{
					"textures": [%s]
				}
				""".formatted(textures));
	}

	// ---- brújula de calavera ----

	static final String ITEM_TEX = ASSETS + "textures/item/";
	static final String ITEM_MODEL = ASSETS + "models/item/";
	/** Definiciones de ítem (1.21.4+): qué modelo usa cada ítem. */
	static final String ITEM_DEF = ASSETS + "items/";
	static final int FRAMES = 32;

	/** Colores de una calavera: K contorno, B hueso, s sombra, E/x huecos, G brillo de los ojos. */
	record SkullPalette(int outline, int bone, int shade, int eye, int glow) {}

	static final SkullPalette BONE_SKULL = new SkullPalette(0x1C1A22, 0xE4E3DA, 0xBDBCB2, 0x1C1A22, 0x9CF06A);
	static final SkullPalette ENDER_SKULL = new SkullPalette(0x1C1A22, 0xE4E3DA, 0xBDBCB2, 0x1C1A22, 0xD07CFF);
	static final SkullPalette PURPLE_SKULL = new SkullPalette(0x24103A, 0xA56BE0, 0x7440AE, 0x12061F, 0xF2D2FF);

	static void compass() throws Exception {
		new File(ITEM_TEX).mkdirs();
		new File(ITEM_DEF).mkdirs();
		// Normal: orbe verde. De ender: orbe y ojos morados (además, el ítem lleva brillo de encantado).
		compass("grave_compass", BONE_SKULL, 0xD8FFB0, 0x7BE04A, 0x3F8A2A);
		compass("ender_grave_compass", ENDER_SKULL, 0xF6E2FF, 0xB45CF0, 0x6A2A9E);

		// Calavera morada de la animación del tótem al teletransportarse.
		BufferedImage skull = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
		stampSkull(skull, PURPLE_SKULL);
		ImageIO.write(skull, "png", new File(ITEM_TEX + "purple_skull.png"));
		write(ITEM_MODEL + "purple_skull.json", """
				{
					"parent": "minecraft:item/generated",
					"textures": { "layer0": "chaosgravestone:item/purple_skull" }
				}
				""");
		itemDefinition("purple_skull", "chaosgravestone:item/purple_skull");
	}

	/**
	 * 32 fotogramas como la brújula vanilla: el fotograma 16 apunta arriba y el giro es horario.
	 * Un orbe orbita la calavera señalando la lápida. También escribe los modelos.
	 */
	static void compass(String name, SkullPalette palette, int orb, int orbGlow, int orbTrail) throws Exception {
		for (int k = 0; k < FRAMES; k++) {
			BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			stampSkull(img, palette);
			// Estela: dos posiciones anteriores, más tenues.
			orb(img, k - 2, orbTrail, -1);
			orb(img, k - 1, orbGlow, -1);
			orb(img, k, orb, orbGlow);
			ImageIO.write(img, "png", new File(ITEM_TEX + String.format("%s_%02d.png", name, k)));
			write(ITEM_MODEL + String.format("%s_%02d.json", name, k), """
					{
						"parent": "minecraft:item/generated",
						"textures": { "layer0": "chaosgravestone:item/%s_%02d" }
					}
					""".formatted(name, k));
		}

		write(ITEM_MODEL + name + ".json", """
				{
					"parent": "minecraft:item/generated",
					"textures": { "layer0": "chaosgravestone:item/%s_16" }
				}
				""".formatted(name));

		// Definición de ítem (1.21.4+): igual que items/compass.json de vanilla, apuntando a la
		// "piedra imán" del componente lodestone_tracker, que es la lápida. Sin destino gira sin control.
		StringBuilder entries = new StringBuilder();
		entries.append(entry(0.0, name, 16)).append(",\n");
		for (int i = 1; i < FRAMES; i++) {
			entries.append(entry(i - 0.5, name, (16 + i) % FRAMES)).append(",\n");
		}
		entries.append(entry(FRAMES - 0.5, name, 16)).append('\n');
		write(ITEM_DEF + name + ".json", """
				{
					"model": {
						"type": "minecraft:range_dispatch",
						"property": "minecraft:compass",
						"target": "lodestone",
						"scale": 32.0,
						"entries": [
				%s			]
					}
				}
				""".formatted(entries));
	}

	static String entry(double threshold, String name, int frame) {
		return String.format(Locale.ROOT,
				"\t\t\t\t{ \"threshold\": %.1f, \"model\": { \"type\": \"minecraft:model\", \"model\": \"chaosgravestone:item/%s_%02d\" } }",
				threshold, name, frame);
	}

	/** Definición de ítem simple (1.21.4+): un único modelo. */
	static void itemDefinition(String id, String model) throws Exception {
		write(ITEM_DEF + id + ".json", """
				{
					"model": { "type": "minecraft:model", "model": "%s" }
				}
				""".formatted(model));
	}

	static void stampSkull(BufferedImage img, SkullPalette p) {
		String[] rows = {
				"..KKKKKK..",
				".KBBBBBBK.",
				"KBBBBBBBsK",
				"KBEEBBEEsK",
				"KBEGBBEGsK",
				"KBBBEEBBsK",
				".KsBBBBsK.",
				"..KBxBxK..",
				"..KxBxBK..",
				"...KKKK...",
		};
		for (int y = 0; y < rows.length; y++) {
			for (int x = 0; x < rows[y].length(); x++) {
				int rgb = switch (rows[y].charAt(x)) {
					case 'K' -> p.outline();
					case 'B' -> p.bone();
					case 's' -> p.shade();
					case 'E', 'x' -> p.eye();
					case 'G' -> p.glow();
					default -> -1;
				};
				if (rgb != -1) {
					img.setRGB(3 + x, 3 + y, 0xFF000000 | rgb);
				}
			}
		}
	}

	/** Orbe en la órbita (radio 6) para el fotograma k; glow != -1 añade un halo en cruz. */
	static void orb(BufferedImage img, int k, int rgb, int glow) {
		double angle = 2 * Math.PI * (k - 16) / FRAMES;
		int x = Math.max(0, Math.min(15, (int) Math.round(7.5 + 6.0 * Math.sin(angle))));
		int y = Math.max(0, Math.min(15, (int) Math.round(7.5 - 6.0 * Math.cos(angle))));
		if (glow != -1) {
			int[][] around = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
			for (int[] d : around) {
				int gx = x + d[0];
				int gy = y + d[1];
				if (gx >= 0 && gy >= 0 && gx < 16 && gy < 16) {
					img.setRGB(gx, gy, 0xFF000000 | glow);
				}
			}
		}
		img.setRGB(x, y, 0xFF000000 | rgb);
	}

	// ---- utilidades ----

	static BufferedImage image() {
		return new BufferedImage(S, S, BufferedImage.TYPE_INT_ARGB);
	}

	static int pick(Random r, int[] palette) {
		return palette[r.nextInt(palette.length)];
	}

	static void set(BufferedImage img, int x, int y, int rgb) {
		if (x >= 0 && y >= 0 && x < img.getWidth() && y < img.getHeight()) {
			img.setRGB(x, y, 0xFF000000 | rgb);
		}
	}

	static void fill(BufferedImage img, Random r, int[] palette, int x0, int y0, int x1, int y1) {
		for (int y = y0; y < y1; y++) {
			for (int x = x0; x < x1; x++) {
				set(img, x, y, pick(r, palette));
			}
		}
	}

	static void hline(BufferedImage img, int x0, int x1, int y, int rgb) {
		for (int x = x0; x <= x1; x++) {
			set(img, x, y, rgb);
		}
	}

	static void vline(BufferedImage img, int x, int y0, int y1, int rgb) {
		for (int y = y0; y <= y1; y++) {
			set(img, x, y, rgb);
		}
	}

	static void rect(BufferedImage img, int x0, int y0, int x1, int y1, int rgb) {
		hline(img, x0, x1, y0, rgb);
		hline(img, x0, x1, y1, rgb);
		vline(img, x0, y0, y1, rgb);
		vline(img, x1, y0, y1, rgb);
	}

	static void write(String path, String content) throws Exception {
		Files.writeString(Path.of(path), content);
	}
}

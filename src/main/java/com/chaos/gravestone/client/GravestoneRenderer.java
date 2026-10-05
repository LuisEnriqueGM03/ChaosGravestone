package com.chaos.gravestone.client;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.chaos.gravestone.block.GravestoneBlock;
import com.chaos.gravestone.block.GravestoneBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

/**
 * Graba en la placa, bajo la calavera de la textura: nombre, fecha y hora, causa, día y objetos.
 * Las medidas siguen tools/TextureGen.java (placa de x 4..12, texto de y 8.4 a 4.7).
 */
public class GravestoneRenderer implements BlockEntityRenderer<GravestoneBlockEntity> {

	private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	/** Ancho útil de la placa, en bloques. */
	private static final float MAX_WIDTH = 7.4F / 16.0F;
	/** Borde superior del texto (justo bajo la calavera), en bloques. */
	private static final float TOP = 8.4F / 16.0F;

	private static final int COLOR_NAME = 0xFFFFFFFF;
	private static final int COLOR_TEXT = 0xFFEDEDED;
	private static final int COLOR_OUTLINE = 0xFF000000;

	private final Font font;

	public GravestoneRenderer(BlockEntityRendererProvider.Context context) {
		this.font = context.getFont();
	}

	@Override
	public void render(GravestoneBlockEntity grave, float partialTick, PoseStack pose, MultiBufferSource buffers,
			int packedLight, int packedOverlay) {
		if (grave.isDecorative()) {
			return;
		}
		Direction facing = grave.getBlockState().getValue(GravestoneBlock.FACING);
		// Letras siempre a plena luz (como un cartel con tinta luminosa): legibles también de noche.
		int light = LightTexture.FULL_BRIGHT;

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		// La cara frontal del modelo está en el centro del bloque; un poco por delante para no parpadear.
		pose.translate(0, TOP, 0.002);

		float y = 0;
		y = line(pose, buffers, light, y,
				Component.literal(grave.getOwnerName()).withStyle(Style.EMPTY.withBold(true)), COLOR_NAME, 0.0062F);
		if (grave.getDeathMillis() > 0) {
			var when = Instant.ofEpochMilli(grave.getDeathMillis()).atZone(ZoneId.systemDefault());
			y = line(pose, buffers, light, y, Component.literal(DATE_TIME.format(when)), COLOR_TEXT, 0.0045F);
		}
		y += 0.008F;

		Component cause = grave.getDeathCause();
		if (cause != null) {
			float scale = 0.0036F;
			List<FormattedCharSequence> wrapped = font.split(cause, (int) (MAX_WIDTH / scale));
			for (int i = 0; i < Math.min(2, wrapped.size()); i++) {
				y = line(pose, buffers, light, y, wrapped.get(i), COLOR_TEXT, scale);
			}
		}

		if (grave.getDeathMillis() > 0) {
			line(pose, buffers, light, y, Component.translatable("text.chaosgravestone.footer",
					grave.getDeathDay(), grave.getItemCount()), COLOR_TEXT, 0.0036F);
		}
		pose.popPose();
	}

	private float line(PoseStack pose, MultiBufferSource buffers, int light, float y, Component text, int color,
			float scale) {
		return line(pose, buffers, light, y, text.getVisualOrderText(), color, scale);
	}

	/** Dibuja una línea centrada; si no cabe, la encoge. Devuelve la altura acumulada. */
	private float line(PoseStack pose, MultiBufferSource buffers, int light, float y, FormattedCharSequence text,
			int color, float scale) {
		int width = font.width(text);
		float s = Math.min(scale, MAX_WIDTH / Math.max(1, width));
		pose.pushPose();
		pose.translate(0, -y, 0);
		pose.scale(s, -s, s);
		// Blanco con reborde negro, igual que los carteles con tinta luminosa.
		font.drawInBatch8xOutline(text, -width / 2.0F, 0, color, COLOR_OUTLINE, pose.last().pose(), buffers, light);
		pose.popPose();
		return y + (font.lineHeight + 1) * s;
	}
}

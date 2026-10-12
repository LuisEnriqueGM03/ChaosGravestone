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
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
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
public class GravestoneRenderer implements BlockEntityRenderer<GravestoneBlockEntity, GravestoneRenderer.State> {

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
		this.font = context.font();
	}

    public static class State extends BlockEntityRenderState {
        boolean decorative;
        Direction facing;
        String ownerName;
        long deathMillis;
        long deathDay;
        Component deathCause;
        int itemCount;
    }
    @Override public State createRenderState() { return new State(); }
    @Override
    public void extractRenderState(GravestoneBlockEntity grave, State state, float partialTick, Vec3 camera, ModelFeatureRenderer.CrumblingOverlay overlay) {
        BlockEntityRenderer.super.extractRenderState(grave, state, partialTick, camera, overlay);
        state.decorative = grave.isDecorative();
        state.facing = grave.getBlockState().getValue(GravestoneBlock.FACING);
        state.ownerName = grave.getOwnerName();
        state.deathMillis = grave.getDeathMillis();
        state.deathDay = grave.getDeathDay();
        state.deathCause = grave.getDeathCause();
        state.itemCount = grave.getItemCount();
    }
    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector buffers, CameraRenderState camera) {
		if (state.decorative) {
			return;
		}
		Direction facing = state.facing;
		// Letras siempre a plena luz (como un cartel con tinta luminosa): legibles también de noche.
		int light = LightCoordsUtil.FULL_BRIGHT;

		pose.pushPose();
		pose.translate(0.5, 0, 0.5);
		pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
		// La cara frontal del modelo está en el centro del bloque; un poco por delante para no parpadear.
		pose.translate(0, TOP, 0.002);

		float y = 0;
		y = line(pose, buffers, light, y,
				Component.literal(state.ownerName).withStyle(Style.EMPTY.withBold(true)), COLOR_NAME, 0.0062F);
		if (state.deathMillis > 0) {
			var when = Instant.ofEpochMilli(state.deathMillis).atZone(ZoneId.systemDefault());
			y = line(pose, buffers, light, y, Component.literal(DATE_TIME.format(when)), COLOR_TEXT, 0.0045F);
		}
		y += 0.008F;

		Component cause = state.deathCause;
		if (cause != null) {
			float scale = 0.0036F;
			List<FormattedCharSequence> wrapped = font.split(cause, (int) (MAX_WIDTH / scale));
			for (int i = 0; i < Math.min(2, wrapped.size()); i++) {
				y = line(pose, buffers, light, y, wrapped.get(i), COLOR_TEXT, scale);
			}
		}

		if (state.deathMillis > 0) {
			line(pose, buffers, light, y, Component.translatable("text.chaosgravestone.footer",
					state.deathDay, state.itemCount), COLOR_TEXT, 0.0036F);
		}
		pose.popPose();
	}

	private float line(PoseStack pose, SubmitNodeCollector buffers, int light, float y, Component text, int color,
			float scale) {
		return line(pose, buffers, light, y, text.getVisualOrderText(), color, scale);
	}

	/** Dibuja una línea centrada; si no cabe, la encoge. Devuelve la altura acumulada. */
	private float line(PoseStack pose, SubmitNodeCollector buffers, int light, float y, FormattedCharSequence text,
			int color, float scale) {
		int width = font.width(text);
		float s = Math.min(scale, MAX_WIDTH / Math.max(1, width));
		pose.pushPose();
		pose.translate(0, -y, 0);
		pose.scale(s, -s, s);
		// Blanco con reborde negro, igual que los carteles con tinta luminosa.
		buffers.submitText(pose, -width / 2.0F, 0, text, false, Font.DisplayMode.NORMAL, light, color, 0, COLOR_OUTLINE);
		pose.popPose();
		return y + (font.lineHeight + 1) * s;
	}
}

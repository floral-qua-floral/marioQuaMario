package com.fqf.charaformact.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.Colors;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DebugHudUtil {
	public static class Pair {
		private final DrawContext CONTEXT;
		private int line;

		public Pair(DrawContext context) {
			this.CONTEXT = context;
		}
	}

	public static void lineBreak(Pair pair) {
		pair.line++;
	}

	public static List<Object> parenthesize(Object... texts) {
		return ImmutableList.builderWithExpectedSize(texts.length + 2).add("(").add(texts).add(")").build();
	}

	public static void renderDebugText(Pair pair, Object... texts) {
		renderDebugTextCol(pair, Colors.WHITE, texts);
	}

	public static void renderDebugTextCol(Pair pair, int color, Object... texts) {
		Window window = MinecraftClient.getInstance().getWindow();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		String text = parseOut(texts);
		int length = textRenderer.getWidth(text);
		int x = window.getScaledWidth() - length - 2;
		int y = window.getScaledHeight() - ++pair.line * (textRenderer.fontHeight + 3);

		pair.CONTEXT.drawTextWithShadow(textRenderer, text, x, y, color);
	}

	public static String parseOut(Object... texts) {
		StringBuilder builder = new StringBuilder();
		boolean startNextWithSpace = false;
		boolean prevWasNumber = false;
		for(Object parse : texts) {
			String append = parseOutSingle(parse, startNextWithSpace, prevWasNumber);
			builder.append(append);
			startNextWithSpace = !append.endsWith("=") && !append.endsWith("(");
			prevWasNumber = parse instanceof Number;
		}
		return builder.toString();
	}

	private static String parseOutSingle(Object text, boolean startNextWithSpace, boolean prevWasNumber) {
		String prefix = startNextWithSpace ? " " : "";
		return switch(text) {
			case Optional<?> optional -> optional.map(object -> parseOutSingle(object, startNextWithSpace, prevWasNumber)).orElse("MISSING");
			case Number number -> (prevWasNumber ? ", " : prefix) + String.format("%.2f", number.doubleValue());
			case Collection<?> collection -> parseOut(collection.toArray());
			case Vec3d vec3d -> parseOut(parenthesize(vec3d.x, vec3d.y, vec3d.z));
			case Vec3i vec3i -> parseOut(parenthesize(vec3i.getX(), vec3i.getY(), vec3i.getZ()));
			case String string -> (string.startsWith(":") ? "" : prefix) + string;
			default -> prefix + text;
		};
	}
}

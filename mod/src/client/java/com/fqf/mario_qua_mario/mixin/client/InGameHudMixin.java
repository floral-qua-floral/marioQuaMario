package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@WrapOperation(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud$HeartType;getTexture(ZZZ)Lnet/minecraft/util/Identifier;"))
	public Identifier drawPowerHeart(InGameHud.HeartType heartType, boolean hardcore, boolean half, boolean blinking, Operation<Identifier> original) {
		Identifier powerHeartID = getPowerHeart(heartType, hardcore, half, blinking);

		return powerHeartID == null ? original.call(heartType, hardcore, half, blinking) : powerHeartID;
	}

	@Unique
	private static @Nullable Identifier getPowerHeart(InGameHud.HeartType heartType, boolean hardcore, boolean half, boolean blinking) {
		ClientPlayerEntity mario = MinecraftClient.getInstance().player;
		if(mario == null || !mario.mqm$getMarioData().isEnabled()) {
			return null;
		}

		PowerUpDefinition.PowerHeart powerHeart = mario.mqm$getMarioData().getPowerUp().HEART;

		if(heartType == InGameHud.HeartType.CONTAINER)
			return blinking ? powerHeart.containerBlinkingTexture() : powerHeart.containerTexture();
		else if(heartType == InGameHud.HeartType.NORMAL) {
			if(hardcore) {
				if(half) return blinking ? powerHeart.hardcoreHalfBlinkingTexture() : powerHeart.hardcoreHalfTexture();
				else return blinking ? powerHeart.hardcoreFullBlinkingTexture() : powerHeart.hardcoreFullTexture();
			}
			else {
				if(half) return blinking ? powerHeart.halfBlinkingTexture() : powerHeart.halfTexture();
				else return blinking ? powerHeart.fullBlinkingTexture() : powerHeart.fullTexture();
			}
		}
		return null;
	}

	@Inject(method = "renderMainHud", at = @At("TAIL"))
	public void renderSpeedometerWithServerData(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if(!MarioQuaMario.CONFIG.isSpecialHUDEnabled()) return;

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity clientMario = client.player;
		if(clientMario == null) return;
		MarioMainClientData clientData = clientMario.mqm$getMarioData();
		if(!clientData.isEnabled()) return;

		double horizontalSpeed = Vector2d.distance(clientMario.getX(), clientMario.getZ(), clientMario.prevX, clientMario.prevZ);
		double verticalSpeed = clientMario.getY() - clientMario.prevY;

		renderText(context, 1, "C: ", horizontalSpeed, verticalSpeed);

		IntegratedServer integratedServer = MinecraftClient.getInstance().getServer();
		if(integratedServer == null) return;
		Entity serverMario = Objects.requireNonNull(integratedServer.getWorld(clientMario.getWorld().getRegistryKey())).getEntityById(clientMario.getId());
		if(serverMario == null) return;
		MarioServerPlayerData serverData = ((ServerPlayerEntity) serverMario).mqm$getMarioData();

		renderText(context, 0, "S: ", serverMario.getVelocity().horizontalLength(), serverMario.getVelocity().y);

		renderText(context, 3, clientMario.getPose() + " (" + clientMario.getHeight() + ") VS "
				+ serverMario.getPose() + " (" + serverMario.getHeight() + ")");


		renderText(context, 7, clientData.getActionID() + " VS " + serverData.getActionID(),
				clientData.getActionID().equals(serverData.getActionID()) ? Colors.WHITE : Colors.LIGHT_RED);
		renderText(context, 6, clientMario.mqm$getAnimationData().isAnimating(clientMario) ? "Animating" : "Not Animating");
		renderText(context, 5, "FallDistance (C, S): ", clientMario.fallDistance, serverMario.fallDistance);
	}

	@Unique
	private void renderText(DrawContext context, int linesFromBottom, String label, double firstValue, double... extraValues) {
		StringBuilder toDisplay = new StringBuilder(label + String.format("%.2f", firstValue));
		for(double value : extraValues) {
			toDisplay.append(", ").append(String.format("%.2f", value));
		}
		renderText(context, linesFromBottom, toDisplay.toString());
	}

	@Unique
	private void renderText(DrawContext context, int linesFromBottom, String text) {
		this.renderText(context, linesFromBottom, text, Colors.WHITE);
	}

	@Unique
	private void renderText(DrawContext context, int linesFromBottom, String text, int color) {
		Window window = MinecraftClient.getInstance().getWindow();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		int length = textRenderer.getWidth(text);
		int x = window.getScaledWidth() - length - 2;
		int y = window.getScaledHeight() - (linesFromBottom + 1) * (textRenderer.fontHeight + 3);

		context.drawTextWithShadow(textRenderer, text, x, y, color);
	}
}

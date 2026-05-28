package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact.cfadata.CfaMainClientData;
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
		ClientPlayerEntity mainPlayer = MinecraftClient.getInstance().player;
		if(mainPlayer == null || !mainPlayer.cfa$getCfaData().isEnabled()) {
			return null;
		}

		FormDefinition.FormHeart formHeart = mainPlayer.cfa$getCfaData().getForm().HEART;

		if(heartType == InGameHud.HeartType.CONTAINER)
			return blinking ? formHeart.containerBlinkingTexture() : formHeart.containerTexture();
		else if(heartType == InGameHud.HeartType.NORMAL) {
			if(hardcore) {
				if(half) return blinking ? formHeart.hardcoreHalfBlinkingTexture() : formHeart.hardcoreHalfTexture();
				else return blinking ? formHeart.hardcoreFullBlinkingTexture() : formHeart.hardcoreFullTexture();
			}
			else {
				if(half) return blinking ? formHeart.halfBlinkingTexture() : formHeart.halfTexture();
				else return blinking ? formHeart.fullBlinkingTexture() : formHeart.fullTexture();
			}
		}
		return null;
	}

	@Inject(method = "renderMainHud", at = @At("TAIL"))
	public void renderSpeedometerWithServerData(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if(!CharaFormAct.CONFIG.isSpecialHUDEnabled()) return;

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity mainPlayer = client.player;
		if(mainPlayer == null) return;
		CfaMainClientData clientData = mainPlayer.cfa$getCfaData();
		if(!clientData.isEnabled()) return;

		double horizontalSpeed = Vector2d.distance(mainPlayer.getX(), mainPlayer.getZ(), mainPlayer.prevX, mainPlayer.prevZ);
		double verticalSpeed = mainPlayer.getY() - mainPlayer.prevY;

		renderText(context, 1, "C: ", horizontalSpeed, verticalSpeed);

		IntegratedServer integratedServer = MinecraftClient.getInstance().getServer();
		if(integratedServer == null) return;
		Entity serverSidedMainPlayer = Objects.requireNonNull(integratedServer.getWorld(mainPlayer.getWorld().getRegistryKey())).getEntityById(mainPlayer.getId());
		if(serverSidedMainPlayer == null) return;
		CfaServerPlayerData serverData = ((ServerPlayerEntity) serverSidedMainPlayer).cfa$getCfaData();

		renderText(context, 0, "S: ", serverSidedMainPlayer.getVelocity().horizontalLength(), serverSidedMainPlayer.getVelocity().y);

		renderText(context, 3, mainPlayer.getPose() + " (" + mainPlayer.getHeight() + ") VS "
				+ serverSidedMainPlayer.getPose() + " (" + serverSidedMainPlayer.getHeight() + ")");


		renderText(context, 7, clientData.getActionID() + " VS " + serverData.getActionID(),
				clientData.getActionID().equals(serverData.getActionID()) ? Colors.WHITE : Colors.LIGHT_RED);
		renderText(context, 6, mainPlayer.cfa$getAppearanceData().getCurrentAnimation() != null ? "Animating" : "Not Animating");
		renderText(context, 5, "FallDistance (C, S): ", mainPlayer.fallDistance, serverSidedMainPlayer.fallDistance);

		renderText(context, 9, "In unloaded chunk: " + clientData.isInUnloadedChunks());
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

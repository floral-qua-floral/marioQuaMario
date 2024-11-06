package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.powerup.PowerUpDefinition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@WrapOperation(method = "drawHeart", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud$HeartType;getTexture(ZZZ)Lnet/minecraft/util/Identifier;"))
	public Identifier usePowerUpHeart(InGameHud.HeartType instance, boolean hardcore, boolean half, boolean blinking, Operation<Identifier> original) {
		// Cancel if the power-up hearts are disabled in the config or if this is a special heart
		if(!MarioQuaMario.CONFIG.shouldUsePowerUpHearts() || (instance != InGameHud.HeartType.CONTAINER && instance != InGameHud.HeartType.NORMAL))
			return original.call(instance, hardcore, half, blinking);

		MarioClientData data = MarioClientData.getInstance();
		// Cancel if there's no Mario Client Data or if the player isn't Mario
		if(data == null || !data.isEnabled())
			return original.call(instance, hardcore, half, blinking);

		if(instance == InGameHud.HeartType.CONTAINER) {
			PowerUpDefinition.PowerHeart heartContainer = data.getPowerUp().HEART_EMPTY;
			if(heartContainer == null) return original.call(instance, hardcore, half, blinking);
			else return heartContainer.getTexture(half, blinking);
		}
		else if(hardcore) {
			return data.getPowerUp().HEART_HARDCORE.getTexture(half, blinking);
		}
		else {
			return data.getPowerUp().HEART.getTexture(half, blinking);
		}
	}

	@Inject(method = "render", at = @At("TAIL"))
	public void renderSpeedometerWithServerData(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {


		MinecraftClient client = MinecraftClient.getInstance();
		PlayerEntity mario = client.player;
		if(mario == null) return;

		double horizontalSpeed = Vector2d.distance(mario.getX(), mario.getZ(), mario.prevX, mario.prevZ);
		double verticalSpeed = mario.getY() - mario.prevY;

		renderText(context, 3, "C: ", horizontalSpeed);
		renderText(context, 1, "C(v): ", verticalSpeed);

		IntegratedServer integratedServer = MinecraftClient.getInstance().getServer();
		if(integratedServer == null) return;
		Entity serverMario = Objects.requireNonNull(integratedServer.getWorld(mario.getWorld().getRegistryKey())).getEntityById(mario.getId());
		if(serverMario == null) return;

		renderText(context, 2, "S: ", serverMario.getVelocity().horizontalLength());
		renderText(context, 0, "S(v): ", serverMario.getVelocity().y);

		renderText(context, 6, mario.getHeight() + " VS " + mario.getHeight());
		renderText(context, 5, mario.getPose() + " VS " + mario.getPose());
	}

	@Unique
	private void renderText(DrawContext context, int linesFromBottom, String label, double value) {
		renderText(context, linesFromBottom, String.format(label + "%.2f", value));
	}

	@Unique
	private void renderText(DrawContext context, int linesFromBottom, String text) {
		Window window = MinecraftClient.getInstance().getWindow();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

//		String text = String.format(label + "%.2f", value);

		int length = textRenderer.getWidth(text);
		int x = window.getScaledWidth() - length - 2;
		int y = window.getScaledHeight() - (linesFromBottom + 1) * (textRenderer.fontHeight + 3);

		context.drawTextWithShadow(textRenderer, text, x, y, Colors.WHITE);
	}
}

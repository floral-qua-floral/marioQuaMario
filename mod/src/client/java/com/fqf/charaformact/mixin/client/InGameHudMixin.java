package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.cfadata.injections.AdvCfaServerDataHolder;
import com.fqf.charaformact.cfadata.util.ActiveAnimation;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact.cfadata.CfaMainClientData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static com.fqf.charaformact.util.DebugHudUtil.*;

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

	@Unique private static Optional<String> mostRecentActionDisagreement = Optional.empty();

	@Inject(method = "renderMainHud", at = @At("TAIL"))
	public void renderSpeedometerWithServerData(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
		if(!CharaFormAct.CONFIG.isSpecialHUDEnabled()) return;

		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity mainPlayer = client.player;
		if(mainPlayer == null) return;
		CfaMainClientData clientData = mainPlayer.cfa$getCfaData();
		if(!clientData.isEnabled()) return;

		Optional<ServerPlayerEntity> serverPlayer = Optional.ofNullable(MinecraftClient.getInstance().getServer()).map(
				integratedServer -> integratedServer.getPlayerManager().getPlayer(mainPlayer.getUuid()));
		Optional<CfaServerPlayerData> serverData = serverPlayer.map(AdvCfaServerDataHolder::cfa$getCfaData);

		double horizontalSpeed = Vector2d.distance(mainPlayer.getX(), mainPlayer.getZ(), mainPlayer.prevX, mainPlayer.prevZ);
		double verticalSpeed = mainPlayer.getY() - mainPlayer.prevY;

		Pair pair = new Pair(context);

		// This is going upwards from the bottom!

		// Speedometer & Fall Distance
		renderDebugText(pair, "S-Spd: H=", serverPlayer.map(Entity::getVelocity).map(Vec3d::horizontalLength),
				"V=", serverPlayer.map(Entity::getVelocity).map(Vec3d::getY));
		renderDebugText(pair, "C-Spd: H=", horizontalSpeed, "V=", verticalSpeed);
		renderDebugText(pair, "FallDistance: C=", mainPlayer.fallDistance, "S=", serverPlayer.map(sp -> sp.fallDistance));
		lineBreak(pair);

		// Pose
		renderDebugText(pair, "Pose: C=", mainPlayer.getPose(), parenthesize(mainPlayer.getHeight()), "S=",
				serverPlayer.map(Entity::getPose), parenthesize(serverPlayer.map(Entity::getHeight)));
		// Animation
		ActiveAnimation currentAnimation = mainPlayer.cfa$getAppearanceData().getCurrentAnimation();
		String animationString; int animationColor;
		if(currentAnimation == null) { animationString = "(Not Animating)"; animationColor = Colors.LIGHT_GRAY; }
		else {
			animationString = "Anim: ";
			if(currentAnimation.ANIMATION.ID == null) { animationString += "Unidentified"; animationColor = Colors.WHITE; }
			else { animationString += currentAnimation.ANIMATION.ID.toString(); animationColor = Colors.YELLOW; }
			if(currentAnimation.EXECUTION_FLAGS.contains(AnimationFlag.Execution.MIRROR)) animationString += " (Mirrored)";
		}
		renderDebugTextCol(pair, animationColor, animationString);
		// Action
		Optional<Boolean> matching = serverData.map(CfaPlayerData::getActionID).map(id -> id.equals(clientData.getActionID()));
		if(matching.orElse(false)) mostRecentActionDisagreement = Optional.of(parseOut("C=",
				clientData.getActionID(), ", S=", serverData.map(CfaPlayerData::getActionID)));
		if(serverPlayer.isPresent()) renderDebugTextCol(pair, mostRecentActionDisagreement.isPresent() ? Colors.WHITE : Colors.GRAY,
				"Last disagreement:", mostRecentActionDisagreement.orElse("(No disagreements yet)"));
		else renderDebugTextCol(pair, Colors.GRAY, "(Cannot watch for disagreements)");
		renderDebugTextCol(pair, matching.map(bool -> bool ? Colors.WHITE : Colors.LIGHT_RED).orElse(Colors.LIGHT_GRAY),
				"Act: C=", clientData.getActionID(), "S=", serverData.map(CfaPlayerData::getActionID));
		lineBreak(pair);

		// Chunk status
		boolean inUnloadedChunks = clientData.isInUnloadedChunks();
		renderDebugTextCol(pair, inUnloadedChunks ? Colors.LIGHT_RED : Colors.LIGHT_GRAY, "Chunk is", inUnloadedChunks
				? "NOT LOADED!" : "loaded...");
		lineBreak(pair);

		// KEEP THIS LAST: Modesty data
		if(clientData.getModestyData().renderDebugHud(pair))
			renderDebugText(pair, "Equipment items covering:");
		else
			renderDebugTextCol(pair, Colors.GRAY, "(Naked??)");
	}
}

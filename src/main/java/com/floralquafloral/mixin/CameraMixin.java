package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.registries.states.action.ActionDefinition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class CameraMixin {
	@Shadow private boolean thirdPerson;
	@Shadow private float lastTickDelta;

	@WrapOperation(method = "setRotation", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"))
	public Quaternionf applyCameraAnimation(Quaternionf instance, float angleY, float angleX, float angleZ, Operation<Quaternionf> original) {
		MarioMainClientData data = MarioMainClientData.getInstance();
		if(data != null && data.cameraAnimation != null && !thirdPerson && attemptApplyRotations(data)) {
			MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
			return instance.rotationYXZ(
					angleY - (float) Math.toRadians(data.CAMERA_ROTATIONS[0]),
					angleX - (float) Math.toRadians(data.CAMERA_ROTATIONS[1]),
					angleZ - (float) Math.toRadians(data.CAMERA_ROTATIONS[2]));
		}
		else return original.call(instance, angleY, angleX, angleZ);
	}

	@Unique private boolean attemptApplyRotations(MarioMainClientData data) {
		float timeSinceAnimStart = Math.max((data.getMario().getWorld().getTime() + MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true)) - data.cameraAnimationStartTime, 0);

		data.cameraAnimationProgression += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
		timeSinceAnimStart = data.cameraAnimationProgression;

		if(timeSinceAnimStart > data.cameraAnimation.DURATION_TICKS * data.cameraAnimationLoops) {
			data.cameraAnimationLoops++;
			if(data.cameraAnimationDoneLooping) {
				data.cameraAnimation = null;
				return false;
			}
		}

		float progress = timeSinceAnimStart / data.cameraAnimation.DURATION_TICKS;

		data.cameraAnimation.CALCULATOR.setRotationalOffsets(progress, data.CAMERA_ROTATIONS);
//		MarioQuaMario.LOGGER.info("\n\n{},\n{}", timeSinceAnimStart, data.TEMPORARY2);
//		if(data.TEMPORARY2 == timeSinceAnimStart) data.TEMPORARY++;
		data.TEMPORARY2 += MinecraftClient.getInstance().getRenderTickCounter().getLastFrameDuration();
		return true;
	}
}

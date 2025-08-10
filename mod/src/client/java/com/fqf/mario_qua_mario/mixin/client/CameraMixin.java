package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.bapping.BlockBappingClientUtil;
import com.fqf.mario_qua_mario.bapping.BumpedBlockParticle;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(Camera.class)
public abstract class CameraMixin {
	@Shadow public abstract Vec3d getPos();

	@Shadow public abstract float getPitch();

	@Shadow public abstract float getYaw();

	@Shadow protected abstract void setPos(Vec3d pos);

	@Shadow private float pitch;
	@Shadow private float yaw;
	@Shadow @Final private Quaternionf rotation;
	@Shadow @Final private static Vector3f HORIZONTAL;
	@Shadow @Final private static Vector3f VERTICAL;
	@Shadow @Final private static Vector3f DIAGONAL;
	@Shadow @Final private Vector3f diagonalPlane;
	@Shadow @Final private Vector3f verticalPlane;
	@Shadow @Final private Vector3f horizontalPlane;
	@Shadow private Entity focusedEntity;
	@Unique private final Arrangement CAMERA_ARRANGEMENT = new Arrangement();

	// This is a bit of a weird way to do things, but I dunno, maybe it'll work better with other mods or something ???
	@Inject(method = "update", at = @At("RETURN"))
	private void prepareCameraAnimations(
			BlockView area,
			Entity focusedEntity,
			boolean thirdPerson, boolean inverseView, float tickDelta,
			CallbackInfo ci
	) {
		if(!thirdPerson && focusedEntity instanceof ClientPlayerEntity mario && mario.mqm$getMarioData().animatingCamera()) {
			Vec3d marioPos = new Vec3d(
					MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX()),
					MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY()),
					MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ())
			);
			Vec3d cameraRelativePos = this.getPos().subtract(marioPos);
			this.CAMERA_ARRANGEMENT.setPos((float) cameraRelativePos.x, (float) cameraRelativePos.y, (float) cameraRelativePos.z);
			this.CAMERA_ARRANGEMENT.setAngles(this.getPitch() * MathHelper.RADIANS_PER_DEGREE, this.getYaw() * MathHelper.RADIANS_PER_DEGREE, 0);
			mario.mqm$getMarioData().mutateCamera(this.CAMERA_ARRANGEMENT, tickDelta);
			this.setPos(marioPos.add(this.CAMERA_ARRANGEMENT.x, this.CAMERA_ARRANGEMENT.y, this.CAMERA_ARRANGEMENT.z));
			this.setRotationRads(this.CAMERA_ARRANGEMENT.pitch, MathHelper.PI + this.CAMERA_ARRANGEMENT.yaw, this.CAMERA_ARRANGEMENT.roll);
			MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
		}
		BumpedBlockParticle particle = BlockBappingClientUtil.getBumpedBlockUnder(focusedEntity);
		if(particle != null) {
			this.setPos(particle.applyOffset(this.getPos(), tickDelta));
			MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
		}
	}

	@Unique
	private void setRotationRads(float pitch, float yaw, float roll) {
		this.pitch = pitch * MathHelper.DEGREES_PER_RADIAN;
		this.yaw = yaw * MathHelper.DEGREES_PER_RADIAN;
		this.rotation.rotationYXZ(-yaw, -pitch, -roll);
		HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
		VERTICAL.rotate(this.rotation, this.verticalPlane);
		DIAGONAL.rotate(this.rotation, this.diagonalPlane);
	}
}

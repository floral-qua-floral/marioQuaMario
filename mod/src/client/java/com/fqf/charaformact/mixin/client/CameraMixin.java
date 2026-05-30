package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.bapping.BlockBappingClientUtil;
import com.fqf.charaformact.cfadata.util.AdvancedArrangement;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
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
	@Unique private final AdvancedArrangement CAMERA_ARRANGEMENT = new AdvancedArrangement();

	// This is a bit of a weird way to do things, but I dunno, maybe it'll work better with other mods or something ???
	@Inject(method = "update", at = @At("RETURN"))
	private void prepareCameraAnimations(
			BlockView area,
			Entity focusedEntity,
			boolean thirdPerson, boolean inverseView, float tickDelta,
			CallbackInfo ci
	) {
		if(!thirdPerson) {
			if(focusedEntity instanceof ClientPlayerEntity player && player.cfa$getCfaData().animatingCamera()) {
				Vec3d playerPos = new Vec3d(
						MathHelper.lerp(tickDelta, focusedEntity.prevX, focusedEntity.getX()),
						MathHelper.lerp(tickDelta, focusedEntity.prevY, focusedEntity.getY()),
						MathHelper.lerp(tickDelta, focusedEntity.prevZ, focusedEntity.getZ())
				);

				player.cfa$getCfaData().preCameraAnimYaw = this.getYaw();

				Vec3d cameraRelativePos = this.getPos().subtract(playerPos);
				this.CAMERA_ARRANGEMENT.setPos((float) cameraRelativePos.x, (float) cameraRelativePos.y, (float) cameraRelativePos.z);
				this.CAMERA_ARRANGEMENT.setAngles(this.getPitch(), this.getYaw(), 0);

				player.cfa$getCfaData().mutateCamera(this.CAMERA_ARRANGEMENT, tickDelta);

				this.setPos(playerPos.add(this.CAMERA_ARRANGEMENT.x, this.CAMERA_ARRANGEMENT.y, this.CAMERA_ARRANGEMENT.z));
				this.CAMERA_ARRANGEMENT.multiplyAngles(MathHelper.RADIANS_PER_DEGREE);
				this.setRotationRads(this.CAMERA_ARRANGEMENT.pitch, this.CAMERA_ARRANGEMENT.yaw, this.CAMERA_ARRANGEMENT.roll);

				player.cfa$getCfaData().postCameraAnimYaw = this.getYaw();

				MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
			}

			Vec3d bumpingOffset = BlockBappingClientUtil.calculateDubiousOffsetUnder(focusedEntity, tickDelta);
			if(bumpingOffset != Vec3d.ZERO) {
				this.setPos(this.getPos().add(bumpingOffset));
				MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate();
			}
		}


	}

	@Unique
	private void setRotationRads(float pitch, float yaw, float roll) {
		this.pitch = pitch * MathHelper.DEGREES_PER_RADIAN;
		this.yaw = yaw * MathHelper.DEGREES_PER_RADIAN;
		this.rotation.rotationYXZ(MathHelper.PI - yaw, -pitch, -roll);
		HORIZONTAL.rotate(this.rotation, this.horizontalPlane);
		VERTICAL.rotate(this.rotation, this.verticalPlane);
		DIAGONAL.rotate(this.rotation, this.diagonalPlane);
	}
}

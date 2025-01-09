package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.BodyPartAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.util.ArrangementSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
	@Shadow @Final private ModelPart cloak;

	public PlayerEntityModelMixin(ModelPart root) {
		super(root);
		throw new AssertionError("Calling constructor on mixin?!");
	}

	@Unique public @NotNull ArrangementSet oldArrangements;
	@Unique public @NotNull ArrangementSet arrangements;

	@Unique public float animationTime;
	@Unique public int loops;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void constructorHook(ModelPart root, boolean thinArms, CallbackInfo ci) {
		this.arrangements = new ArrangementSet();
		this.oldArrangements = new ArrangementSet();
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void setAnglesHook(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		PlayerEntity mario = (PlayerEntity) livingEntity;
		MarioPlayerData data = mario.mqm$getMarioData();
		PlayermodelAnimation animation = data.getAction().ANIMATION;

		if(!data.isEnabled() || animation == null) return;

		if(data.resetAnimation) {
			this.oldArrangements = this.arrangements;
			this.arrangements = new ArrangementSet();
		}

//		this.applyAnimation(data, animation.wholeMutator(), arrangements.EVERYTHING, null);
		this.applyAnimation(data, animation.headAnimation(), arrangements.HEAD, this.head);
		this.applyAnimation(data, animation.torsoAnimation(), arrangements.BODY, this.body);

		if(shouldAnimateArm(this.rightArmPose, this.leftArmPose))
			this.applyAnimation(data, animation.rightArmAnimation(), arrangements.RIGHT_ARM, this.rightArm);
		if(shouldAnimateArm(this.leftArmPose, this.rightArmPose))
			this.applyAnimation(data, animation.leftArmAnimation(), arrangements.LEFT_ARM, this.leftArm);

		this.applyAnimation(data, animation.rightLegAnimation(), arrangements.RIGHT_LEG, this.rightLeg);
		this.applyAnimation(data, animation.leftLegAnimation(), arrangements.LEFT_LEG, this.leftLeg);

		this.applyAnimation(data, animation.capeAnimation(), arrangements.CAPE, this.cloak);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
//		matrices.translate(0, -0.3, 0);
		if(MinecraftClient.getInstance().player.isSneaking()) matrices.multiply(new Quaternionf(0, 0, 0, 1));
		super.render(matrices, vertices, light, overlay, color);
	}

	@Unique
	private void applyAnimation(MarioPlayerData data, @Nullable Arrangement.Mutator mutator, Arrangement arrangement, ModelPart part) {
		if(mutator == null) return;
		arrangement.setAngles(MathHelper.DEGREES_PER_RADIAN * part.pitch, MathHelper.DEGREES_PER_RADIAN * part.yaw, MathHelper.DEGREES_PER_RADIAN * part.roll);
		arrangement.setPos(part.pivotX, part.pivotY, part.pivotZ);
		mutator.mutate(data, arrangement, this.animationTime, this.loops);
		part.setAngles(MathHelper.RADIANS_PER_DEGREE * arrangement.pitch, MathHelper.RADIANS_PER_DEGREE * arrangement.yaw, MathHelper.RADIANS_PER_DEGREE * arrangement.roll);
		part.setPivot(arrangement.x, arrangement.y, arrangement.z);
	}
	@Unique
	private void applyAnimation(MarioPlayerData data, @Nullable BodyPartAnimation animation, Arrangement arrangement, ModelPart part) {
		if(animation == null) return;
		this.applyAnimation(data, animation.mutator(), arrangement, part);
	}
	@Unique
	private void applyAnimation(MarioPlayerData data, @Nullable LimbAnimation animation, Arrangement arrangement, ModelPart part) {
		if(animation == null) return;
		this.applyAnimation(data, animation.mutator(), arrangement, part);
	}

	@Unique
	private boolean shouldAnimateArm(ArmPose armPose, ArmPose otherArmPose) {
		return !armPose.isTwoHanded() && !otherArmPose.isTwoHanded() && switch (armPose) {
			case EMPTY, ITEM -> true;
			case BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH -> false;
		};
	}
}

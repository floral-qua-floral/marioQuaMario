package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.actions.airborne.WallJump;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.fqf.mario_qua_mario.util.ClimbVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClimbPole implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_pole");

	private static Vec3d getMaximumOffset(CfaData data, double contraction) {
		Box cameraBox = data.getPlayer().getBoundingBox().contract(contraction, 0, contraction);
		Vec3d offset = Vec3d.fromPolar(0, data.getPlayer().getYaw()).multiply(-0.7);
		return Entity.adjustMovementForCollisions(data.getPlayer(), offset, cameraBox, data.getPlayer().getWorld(), List.of());
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					Vec3d offset = getMaximumOffset(data, 0.1).multiply(16);
					arrangement.z += (float) offset.horizontalLength();
				},
				(posture, data, animationTime, helper) -> {
					ClimbVars vars = data.retrieveStateData(ClimbVars.class);
					double factorForInput = 1 / ClimbPole.CLIMB_SPEED.get(data);
					vars.progress += (float) (data.getYVel() * factorForInput) / 2;
					float climbProgress = MathHelper.sin(vars.progress);

					posture.TORSO.yaw += climbProgress * -15;

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.y += climbProgress * 1.9F * sideFactor;
						arrangement.addAngles(
								-140 + 20 * climbProgress * sideFactor,
								-20 * sideFactor,
								0
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								0,
								-3 + climbProgress * -3 * sideFactor,
								-3.7F
						);
						arrangement.pitch -= 10 + climbProgress * 20 * sideFactor;
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = 60;
						if(data.getVelocity().lengthSquared() > 0.1)
							posture.TAIL.roll += climbProgress * 30;
					}
				}
		);
	}
	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return new CameraAnimationSet(
				() -> CameraAnimationOption.AUTHENTIC,
				new CameraAnimation(new CameraProgressHandler((data, ticksPassed) -> ticksPassed), (data, arrangement, progress) -> {
					Vec3d collOffset = getMaximumOffset(data, 0.2);
					arrangement.addPos((float) collOffset.x, (float) collOffset.y, (float) collOffset.z);
				}),
				null,
				null
		);
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}
	@Override public @NotNull GenericActionType getGenericActionType() {
		return GenericActionType.UNSPECIFIED;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	public static final CfaStat CLIMB_SPEED = new CfaStat(0.2, StatCategory.UP, StatCategory.SPEED, StatCategory.CLIMBING);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ClimbVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public boolean travelHook(CfaTravelData data) {
		double forwardInput = data.getInputs().getForwardInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
		data.setYVel(forwardInput * CLIMB_SPEED.get(data));
		data.centerLaterally();
		data.setForwardStrafeVel(0, 0);
		data.getPlayer().fallDistance = 0;
		return true;
	}

	private static void releasePole(CfaTravelData data) {
		data.goTo(data.getPlayer().getPos().add(getMaximumOffset(data, 0)));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, CastableHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						WallJump.ID,
						data -> (data.getInputs().getForwardInput() < -0.25 || Math.abs(data.getInputs().getStrafeInput()) > 0.25)
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							releasePole(data);
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							double speed = WallJump.WALL_JUMP_SPEED.get(data);
							data.setForwardStrafeVel(data.getInputs().getForwardInput() * speed,
									data.getInputs().getStrafeInput() * speed);
						},
						(data, isSelf, seed) -> {
							data.voice(Voicelines.WALL_JUMP, seed);
							data.playJumpSound(seed);
						}
				),
				new ActionTransitionDetails(
						Fall.ID,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							releasePole(data);
							data.getInputs().DUCK.isPressed(); // Unbuffer Duck
						},
						null
				),
				new ActionTransitionDetails(
						Jump.ID,
						data -> data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							releasePole(data);
							data.setYVel(1);
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
						}
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, CastableHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						SpecialFall.ID,
						data -> !ClimbTransitions.inNonSolidClimbable(data, false),
						EvaluatorEnvironment.COMMON,
						ClimbPole::releasePole,
						null
				),
				new ActionTransitionDetails(
						SubWalk.ID,
						data -> data.getPlayer().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY,
						ClimbPole::releasePole,
						null
				)
		);
	}
}

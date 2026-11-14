package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SizeChangeBehavior;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.event.GameEvent;
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
					LinearClimbData vars = data.get(LINEAR_CLIMB_DATA);
					float progress = MathHelper.sin(vars.progress);

					posture.TORSO.yaw += progress * -15;

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.y += progress * 1.9F * sideFactor;
						arrangement.addAngles(
								-140 + 20 * progress * sideFactor,
								-20 * sideFactor,
								0
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								0,
								-3 + progress * -3 * sideFactor,
								-3.7F
						);
						arrangement.pitch -= 10 + progress * 20 * sideFactor;
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = 60;
						if(data.getVelocity().lengthSquared() > 0.1)
							posture.TAIL.roll += progress * 30;
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

	@Override public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.FULL_CLIMBING);
		builder.add(ActionFlag.PROHIBIT_FLIGHT, ActionFlag.ALLOW_STEALTH, ActionFlag.NO_STEP);
	}

	private static class LinearClimbData {
		private float progress;
		private float nextStepSoundUnder = MathHelper.PI;
		private float nextStepSoundOver = MathHelper.PI;

		private double prevY;

		private LinearClimbData(CfaData data) {
			this.prevY = data.getPlayer().getY();
		}
	}

	private static final StateAttachmentKey<LinearClimbData> LINEAR_CLIMB_DATA =
			StateAttachmentKey.action(LinearClimbData::new);

	@Override
	public void commonTick(CfaData data) {
		data.forceBodyAlignment(true);
		LinearClimbData climb = data.get(LINEAR_CLIMB_DATA);

		climb.progress += (float) (5 * (data.getPlayer().getY() - climb.prevY));
		if(climb.progress > climb.nextStepSoundOver || climb.progress < climb.nextStepSoundUnder) {
			float closestInterval = Math.round(climb.progress / MathHelper.PI) * MathHelper.PI;
			climb.nextStepSoundUnder = closestInterval - MathHelper.PI;
			climb.nextStepSoundOver = closestInterval + MathHelper.PI;

			if(!data.getPlayer().isSneaky()) {
				if(data.isClient() && data instanceof CfaClientData clientData)
					ClimbTransitions.playStepSound(clientData, data.getPlayer().getRandom().nextLong(), data.getPlayer().getBlockStateAtPos());
				else
					data.getPlayer().emitGameEvent(GameEvent.STEP);
			}
		}

		climb.prevY = data.getPlayer().getY();
	}

	public record ClimbStatSet(CfaStat ascend, CfaStat descend, CfaStat sidle) {
		public static final ClimbStatSet REGULAR = new ClimbStatSet(0.15, 0.2, 0.175);
		public static final ClimbStatSet SPRINT = new ClimbStatSet(0.26, 0.36, 0.285);
		public static final ClimbStatSet SNEAK = new ClimbStatSet(0.075, 0.12, 0.11);

		public static ClimbStatSet choose(CfaTravelData data) {
			if(data.getInputs().DUCK.isHeld()) return SNEAK;
			else if(data.getPlayer().isSprinting()) return SPRINT;
			else return REGULAR;
		}

		public CfaStat getVertical(double input) {
			return (input >= 0 ? this.ascend() : this.descend());
		}

		private ClimbStatSet(double ascend, double descend, double sidle) {
			this(stat(ascend, StatCategory.ASCEND), stat(descend, StatCategory.DESCEND), stat(sidle, StatCategory.SIDLE));
		}
		private static CfaStat stat(double value, StatCategory direction) {
			return new CfaStat(value, direction, StatCategory.CLIMBING, StatCategory.SPEED);
		}
	}

	@Override public void travelHook(CfaTravelData data) {
		double forwardInput = data.getInputs().getForwardInput();
		data.setYVel(forwardInput * ClimbStatSet.choose(data).getVertical(forwardInput).get(data));
		data.centerLaterally();
		data.setForwardStrafeVel(0, 0);
		data.getPlayer().fallDistance = 0;
	}

	private static void releasePole(CfaTravelData data) {
		data.goTo(data.getPlayer().getPos().add(getMaximumOffset(data, 0)));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						WallJump.ID,
						data -> (data.getInputs().getForwardInput() < -0.25 || Math.abs(data.getInputs().getStrafeInput()) > 0.25)
								&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							releasePole(data);
							data.setYVel(WallJump.WALL_JUMP_VEL.get(data));
							double speed = WallJump.WALL_JUMP_SPEED.get(data);
							data.setForwardStrafeVel(data.getInputs().getNormalizedForwardInput() * speed,
									data.getInputs().getNormalizedStrafeInput() * speed);
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
						SizeChangeBehavior.AUTOMATIC,
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
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							releasePole(data);
							data.setYVel(ClimbTransitions.CLIMB_JUMP_UP_VEL.get(data));
						},
						(data, isSelf, seed) ->
								data.playJumpSound(seed)
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						SpecialFall.ID,
						data -> !ClimbTransitions.inNonSolidClimbable(data, false),
						EvaluatorEnvironment.COMMON,
						SizeChangeBehavior.AUTOMATIC,
						ClimbPole::releasePole,
						null
				),
				new ActionTransitionDetails(
						SubWalk.ID,
						data -> data.getPlayer().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						ClimbPole::releasePole,
						null
				)
		);
	}
}

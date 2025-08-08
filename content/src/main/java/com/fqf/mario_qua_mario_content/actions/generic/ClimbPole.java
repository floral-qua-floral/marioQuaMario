package com.fqf.mario_qua_mario_content.actions.generic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.airborne.*;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import com.fqf.mario_qua_mario_content.util.ClimbVars;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ClimbPole implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_pole");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static Vec3d getMaximumOffset(IMarioData data, double contraction) {
		Box cameraBox = data.getMario().getBoundingBox().contract(contraction, 0, contraction);
		Vec3d offset = Vec3d.fromPolar(0, data.getMario().getYaw()).multiply(-0.7);
		return Entity.adjustMovementForCollisions(data.getMario(), offset, cameraBox, data.getMario().getWorld(), List.of());
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch -= 140 + progress * -20 * factor;
			arrangement.yaw -= 20 * factor;
			arrangement.y += progress * 1.9F * factor;
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch -= 10 + progress * 20 * factor;
			arrangement.y -= 3F + progress * 3F * factor;
			arrangement.z -= 3.7F;
	    });
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> {
					ClimbVars vars = data.getVars(ClimbVars.class);
					double factorForInput = 1 / ClimbPole.CLIMB_SPEED.get(data);
					vars.progress += (float) (data.getYVel() * factorForInput) / 2;
					return MathHelper.sin(vars.progress);
				}),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					Vec3d offset = getMaximumOffset(data, 0.1).multiply(16);
					arrangement.z += (float) offset.horizontalLength();
				}),
				null,
				new BodyPartAnimation((data1, arrangement1, progress1) -> {
					arrangement1.yaw += progress1 * -15;
				}),
				makeArmAnimation(1), makeArmAnimation(-1),
				makeLegAnimation(1), makeLegAnimation(-1),
				new LimbAnimation(true, (data, arrangement, progress) -> {
					arrangement.pitch = 60;
					if(data.getVelocity().lengthSquared() > 0.1)
						arrangement.roll += progress * 30;
				})
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
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
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat CLIMB_SPEED = new CharaStat(0.2, StatCategory.UP, StatCategory.SPEED, StatCategory.CLIMBING);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ClimbVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		data.forceBodyAlignment(true);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public boolean travelHook(IMarioTravelData data) {
		double forwardInput = data.getInputs().getForwardInput() * (data.getInputs().DUCK.isHeld() ? 0.3 : 1);
		data.setYVel(forwardInput * CLIMB_SPEED.get(data));
		data.centerLaterally();
		data.setForwardStrafeVel(0, 0);
		data.getMario().fallDistance = 0;
		return true;
	}

	private static void releasePole(IMarioTravelData data) {
		data.goTo(data.getMario().getPos().add(getMaximumOffset(data, 0)));
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions() {
		return List.of(
				new TransitionDefinition(
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
				new TransitionDefinition(
						Fall.ID,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							releasePole(data);
							data.getInputs().DUCK.isPressed(); // Unbuffer Duck
						},
						null
				),
				new TransitionDefinition(
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
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> !ClimbTransitions.inNonSolidClimbable(data, false),
						EvaluatorEnvironment.COMMON,
						ClimbPole::releasePole,
						null
				),
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getMario().isOnGround(),
						EvaluatorEnvironment.CLIENT_ONLY,
						ClimbPole::releasePole,
						null
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}

package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class LavaBoost extends Fall implements AirborneActionDefinition {
    public static final Identifier ID = MarioQuaMarioContent.makeID("lava_boost");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.addAngles(120, factor * 88, factor * 90);
			arrangement.addPos(0, 1.25F, 1);
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.addAngles(-57.75F + factor * MathHelper.sin(progress) * 40, 0, 0);
	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            null,
	            new ProgressHandler((data, ticksPassed) -> ticksPassed / 2.2F),
	            new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.pitch += 28.45F;
					arrangement.y -= 6.75F;
	            }),
	            null,
	            null,
	            makeArmAnimation(1), makeArmAnimation(-1),
	            makeLegAnimation(1), makeLegAnimation(-1),
	            null
	    );
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
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

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static CharaStat BOOST_VEL = new CharaStat(1.4, JUMP_VELOCITY);

	public static CharaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_FORWARD_SPEED = Fall.FORWARD_DRIFT_SPEED.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_SPEED = Fall.BACKWARD_DRIFT_SPEED.variate(0.5);
	public static CharaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_STRAFE_SPEED = Fall.STRAFE_DRIFT_SPEED.variate(0.5);

	public static CharaStat BOOST_REDIRECTION = new CharaStat(3, DRIFTING, REDIRECTION);

	private static class LavaBoostVars {
		private double bounceVel;
	}
	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new LavaBoostVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		data.getVars(LavaBoostVars.class).bounceVel = data.getYVel() * -0.6;
		if(data.getYVel() < 0) Fall.drift(data, helper);
		else helper.airborneAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				BOOST_REDIRECTION
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(
						ID,
						data ->
								data.getYVel() <= 0
								&& data.getVars(LavaBoostVars.class).bounceVel > 0.06
								&& Fall.LANDING.evaluator().shouldTransition(data),
						null,
						data -> {
							data.setYVel(data.getVars(LavaBoostVars.class).bounceVel);
							data.setForwardStrafeVel(data.getForwardVel() * 0.5, data.getStrafeVel() * 0.5);
						},
						(data, isSelf, seed) -> {}
				),
				Fall.LANDING.variate(null, data -> data.getYVel() <= 0 && Fall.LANDING.evaluator().shouldTransition(data))
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
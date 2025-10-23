package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.aquatic.AquaticPoundDrop;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario_content.actions.grounded.GroundPoundLand;
import com.fqf.mario_qua_mario_content.stomp_types.GroundPound;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class GroundPoundDrop implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("ground_pound_drop");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static PlayermodelAnimation makeAnimation(AnimationHelper helper) {
		return GroundPoundFlip.makeAnimation(helper).variate(
				null,
				new ProgressHandler((data, ticksPassed) -> 1),
				null, null, null,
				null, null, null, null,
				null
		);
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return makeAnimation(helper);
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

	@Override public @Nullable BappingRule getBappingRule() {
		return BappingRule.GROUND_POUND;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return GroundPound.ID;
	}

	public static final CharaStat GROUND_POUND_VEL = new CharaStat(-1.5, TERMINAL_VELOCITY, STOMP);
	public static final CharaStat GROUND_POUND_STRAINING_VEL = new CharaStat(0.1, DRIFTING);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, null, Fall.FALL_SPEED);
		double strainVel = GROUND_POUND_STRAINING_VEL.get(data);
		data.setForwardStrafeVel(strainVel * data.getInputs().getForwardInput(), strainVel * data.getInputs().getStrafeInput());
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> data.getYVel() > 0 || data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.getInputs().DUCK.isPressed(), // Unbuffer duck to make Ground Pound stalling harder
						(data, isSelf, seed) -> data.stopStoredSound(MarioContentSFX.GROUND_POUND_DROP)
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Fall.LANDING.variate(
						GroundPoundLand.ID,
						null, EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioContentSFX.GROUND_POUND_DROP);
							data.playSound(MarioContentSFX.GROUND_POUND_LAND, seed);
						}
				),
				Submerged.SUBMERGE.variate(
						AquaticPoundDrop.ID,
						null, null,
						data -> {
							data.setYVel(data.getYVel() * 0.6);
						},
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioContentSFX.GROUND_POUND_DROP);
							data.storeSound(data.playSound(MarioContentSFX.AQUATIC_GROUND_POUND_DROP, seed));
						}
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
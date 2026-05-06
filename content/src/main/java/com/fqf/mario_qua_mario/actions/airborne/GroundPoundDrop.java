package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundDrop;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.GroundPoundLand;
import com.fqf.mario_qua_mario.collision_attacks.GroundPound;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class GroundPoundDrop implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_drop");
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
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return GroundPound.ID;
	}

	public static final CfaStat GROUND_POUND_VEL = new CfaStat(-1.5, TERMINAL_VELOCITY, COLLISION_ATTACK);
	public static final CfaStat GROUND_POUND_STRAINING_VEL = new CfaStat(0.1, DRIFTING);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
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
						(data, isSelf, seed) -> data.stopStoredSound(MarioSFX.GROUND_POUND_DROP)
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
							data.stopStoredSound(MarioSFX.GROUND_POUND_DROP);
							data.playSound(MarioSFX.GROUND_POUND_LAND, seed);
						}
				),
				Submerged.SUBMERGE.variate(
						AquaticPoundDrop.ID,
						null, null,
						data -> {
							data.setYVel(data.getYVel() * 0.6);
						},
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioSFX.GROUND_POUND_DROP);
							data.storeSound(data.playSound(MarioSFX.AQUATIC_GROUND_POUND_DROP, seed));
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
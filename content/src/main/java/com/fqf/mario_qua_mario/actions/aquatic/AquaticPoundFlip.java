package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class AquaticPoundFlip implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound_flip");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	private static final float AQUATIC_FLIP_DURATION = 7;
	@Override public @Nullable AnimationDefinition getAnimation() {
		return GroundPoundFlip.makeAnimation(AQUATIC_FLIP_DURATION);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return GroundPoundFlip.makeCameraAnimations(AQUATIC_FLIP_DURATION + 2.5F);
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
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new GroundPoundFlip.FlipTimerVars(data);
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		data.retrieveStateData(GroundPoundFlip.FlipTimerVars.class).actionTimer++;
		data.setYVel(0.075);
	}

	public static final TransitionDefinition AQUATIC_GROUND_POUND = GroundPoundFlip.GROUND_POUND.variate(
			AquaticPoundFlip.ID,
			null,
			null,
			null,
			(data, isSelf, seed) -> data.playSound(MarioSFX.AQUATIC_GROUND_POUND_FLIP, seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of(
				GroundPoundFlip.makeDropTransition(AquaticPoundDrop.ID, AQUATIC_FLIP_DURATION)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				Submerged.EXIT_WATER.variate(GroundPoundFlip.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

}
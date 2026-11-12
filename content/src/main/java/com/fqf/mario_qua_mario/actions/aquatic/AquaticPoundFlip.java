package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip.FLIP_TIMER;
import static com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip.STORED_FALL_DISTANCE;

public class AquaticPoundFlip implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound_flip");

	private static final float AQUATIC_FLIP_DURATION = 7;
	@Override public @Nullable AnimationDefinition defineAnimation() {
		return GroundPoundFlip.makeAnimation(AQUATIC_FLIP_DURATION);
	}

	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return GroundPoundFlip.makeCameraAnimations(AQUATIC_FLIP_DURATION + 2.5F);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override
	public void onEnter(CfaData data, ImmutableSet.Builder<StateAttachmentKey<?>> attachmentsToKeep) {
		attachmentsToKeep.add(FLIP_TIMER, STORED_FALL_DISTANCE);
		data.get(STORED_FALL_DISTANCE); // Ensure it's stored
	}

	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		data.get(FLIP_TIMER).increment();
		data.setYVel(0.075);
	}

	public static final ActionTransitionDetails AQUATIC_GROUND_POUND = GroundPoundFlip.GROUND_POUND.variate(
			AquaticPoundFlip.ID,
			null,
			null,
			null, null,
			(data, isSelf, seed) -> data.playSound(MarioSFX.AQUATIC_GROUND_POUND_FLIP, seed)
	);

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(GroundPoundFlip.makeDropTransition(AquaticPoundDrop.ID, AQUATIC_FLIP_DURATION));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(Submerged.EXIT_WATER.variate(GroundPoundFlip.ID, null));
	}
}
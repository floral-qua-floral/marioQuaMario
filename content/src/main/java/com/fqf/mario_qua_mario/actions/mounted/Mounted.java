package com.fqf.mario_qua_mario.actions.mounted;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Backflip;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Mounted implements MountedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("mounted");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.ALLOW;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	@Override public @NotNull MutableText defineDismountHint(
			MutableText vanillaHint, Text sneakKeybind, Text jumpKeybind,
			Text attackKeybind, Text forwardKeybind, Text backwardKeybind
	) {
		return Text.translatable("mount.onboard.mario", sneakKeybind, jumpKeybind);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, MountedActionHelper helper) {
		builder.add(new TransitionDefinition(
				SubWalk.ID,
				data -> helper.getMount(data) == null || helper.getMount(data).isRemoved(),
				EvaluatorEnvironment.COMMON,
				data -> {
					MarioQuaMario.LOGGER.warn("Transitioned to SubWalk because mount was missing?!");
				},
				(data, isSelf, seed) -> {}
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, MountedActionHelper helper) {
		TransitionDefinition backflip = Backflip.makeBackflipTransition((GroundedActionDefinition.GroundedActionHelper) helper);
		builder.add(
				backflip.variate(
						null,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						null,
						data -> {
							helper.dismount(data, false);
							Objects.requireNonNull(backflip.travelExecutor()).execute(data);
						},
						null
				)
		);
	}
}

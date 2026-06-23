package com.fqf.mario_qua_mario.actions.mounted;

import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Backflip;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.google.common.collect.ImmutableList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Mounted implements MountedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("mounted");


	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}

	@Override public @NotNull MutableText defineDismountHint(
			MutableText vanillaHint, Text sneakKeybind, Text jumpKeybind,
			Text attackKeybind, Text forwardKeybind, Text backwardKeybind
	) {
		return Text.translatable("mount.onboard.mario", sneakKeybind, jumpKeybind);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, MountedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
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
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, MountedActionHelper helper) {
		ActionTransitionDetails backflip = Backflip.makeBackflipTransition((GroundedActionDefinition.GroundedActionHelper) helper);
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

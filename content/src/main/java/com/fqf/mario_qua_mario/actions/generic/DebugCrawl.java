package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SizeChangeBehavior;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DebugCrawl extends Debug {
	public static final Identifier ID = MarioQuaMario.makeID("debug_crawl");

	@Override
	public float defineHitboxHeight() {
		return 0.4F;
	}

	@Override
	public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.addPos(0, 0, 12);
					arrangement.pitch = -90;
				},
				(posture, data, animationTime, helper) -> Debug.tPose(posture)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						Debug.ID,
						data -> true,
						EvaluatorEnvironment.COMMON,
						SizeChangeBehavior.AUTOMATIC,
						null,
						(data, isSelf, seed) -> data.voice(Voicelines.BURNT, seed)
				)
		);
	}

	@Override
	public void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {

	}
}

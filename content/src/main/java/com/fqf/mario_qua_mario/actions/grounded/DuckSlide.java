package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.CfaMarioAddon;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Backflip;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.airborne.LongJump;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterDuck;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class DuckSlide implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_slide");

	@Override public float defineHitboxHeight() {
		return DuckWaddle.DUCK_HEIGHT;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(true, false);
	}

	@Override
	public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.SNEAKING);
		builder.addAll(ActionFlag.SLIDING);
		builder.add(ActionFlag.NO_LEDGE_CLIP);
	}

	public static final StateAttachmentKey<MutableInt> SKID_SOUND_TIMER = StateAttachmentKey.action(() -> new MutableInt());
	public static void slideSound(CfaClientData data) {
		float slidingSpeed = (float) ((CfaReadableMotionData) data).getHorizVelSquared();
		data.sustainSound(MarioSFX.SKID, data.getPlayer(), SoundCategory.PLAYERS,
				1.0F + Math.min(0.15F, 0.5F * slidingSpeed),
				Math.min(1.0F, ((float) data.get(DuckSlide.SKID_SOUND_TIMER).getAndIncrement()) / 3.0F) * Math.min(1.0F, 0.7F * slidingSpeed) * 0.4F
		);
	}

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {
		slideSound(data);
		Vec3d deltaPos = Skid.getDeltaPos(data, true);
		double speed = deltaPos.horizontalLength();
		if(speed > 0.01)
			Skid.spawnParticles(data, Math.min(speed * speed, 3), deltaPos, 1.5, 0.5);
	}

	public static final CfaStat SLIDE_THRESHOLD = new CfaStat(0.25, DUCKING, THRESHOLD);
	public static final CfaStat SLIDE_BOOST = new CfaStat(-0.15);

	public static final CfaStat SLIDE_DRAG = new CfaStat(0.04333, DUCKING, DRAG);
	public static final CfaStat SLIDE_DRAG_MIN = new CfaStat(0.01, DUCKING, DRAG);
	public static final CfaStat SLIDE_REDIRECTION = new CfaStat(4.0, DUCKING, REDIRECTION);

	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.get(CfaMarioAddon.ACTION_TIMER).increment();
		helper.applyDrag(
				data, SLIDE_DRAG, SLIDE_DRAG_MIN,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckWaddle.UNDUCK,
				new ActionTransitionDetails(
						DuckWaddle.ID,
						data -> data.getHorizVelSquared() == 0,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Backflip.makeBackflipTransition(helper),
				new ActionTransitionDetails(
						LongJump.ID,
						data ->
								data.getInputs().getForwardInput() > 0.4
										&& data.get(CfaMarioAddon.ACTION_TIMER).intValue() < 5
										&& data.getForwardVel() > LongJump.LONG_JUMP_THRESHOLD.get(data)
										&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							helper.performJump(data, LongJump.LONG_JUMP_VEL, null);
							data.setForwardVel(data.getForwardVel() * 0.92 + 0.098);
							data.getInputs().DUCK.isPressed(); // Force unbuffer DUCK
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(Voicelines.LONG_JUMP, seed);
						}
				),
				DuckJump.makeDuckJumpTransition(helper)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckFall.DUCK_FALL,
				UnderwaterDuck.DUCK_SUBMERGE
		);
	}

	public static final TransitionInjectionDefinition GROUNDED_INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, DuckWaddle.ID,
			ActionCategory.GROUNDED,
			(nearbyTransition, helper) -> nearbyTransition.variate(
					DuckSlide.ID,
					data -> {
						double threshold = SLIDE_THRESHOLD.get(data);
						return data.getHorizVelSquared() > threshold * threshold
								&& nearbyTransition.evaluator().test(data);
					},
					EvaluatorEnvironment.CLIENT_ONLY,
					null,
					null, null
			)
	);
	public static final TransitionInjectionDefinition AIRBORNE_INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, DuckWaddle.ID,
			ActionCategory.AIRBORNE,
			(nearbyTransition, helper) -> nearbyTransition.variate(
					DuckSlide.ID,
					data -> {
						double threshold = SLIDE_THRESHOLD.get(data);
						return (data.isServer() || (data.getHorizVelSquared() > threshold * threshold))
								&& nearbyTransition.evaluator().test(data);
					},
					EvaluatorEnvironment.CLIENT_CHECKED,
					null, null, null
			)
	);

}

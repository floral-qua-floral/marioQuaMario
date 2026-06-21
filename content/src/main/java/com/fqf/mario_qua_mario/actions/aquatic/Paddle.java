package com.fqf.mario_qua_mario.actions.aquatic;


import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Paddle implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("paddle");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> arrangement.addPos(0, -2, -4),
				(posture, data, animationTime, helper) -> {
					float progress = animationTime / 1.5F;

					posture.TORSO.addAngles(
							27.5F,
							MathHelper.sin(progress) * 5,
							0
					);

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.roll *= -1;
						arrangement.addAngles(
								17.5F - sideFactor * MathHelper.sin(progress) * 1,
								0,
								sideFactor * 2
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								sideFactor * -0.675F,
								-2,
								2F
						);
						arrangement.pitch *= 0.5F;
						arrangement.addAngles(
								50 + sideFactor * MathHelper.sin(progress) * 30,
								sideFactor * 6,
								0
						);
					});
				}
		);
	}
	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final CfaStat PADDLE_FALL_SPEED = Submerged.FALL_SPEED.variate(0.5);

	private static final double INTENDED_IMMERSION_LEVEL = 0.75;

	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		if(data.getPlayer().getWorld().getTime() % 3 == 0)
			data.playSound(MarioSFX.SWIM_PADDLE, data.getPlayer().getRandom().nextLong());
	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		if(data.getImmersionPercent() >= 0.95) helper.applyGravity(data, Submerged.FALL_ACCEL, Submerged.FALL_SPEED.variate(0.1));
		else {
			// Attempt to swim at the right height to keep the player's eyes just above the water?
			double immersionLevel = data.getImmersionLevel();
			double deltaY = immersionLevel - 0.8 * data.getPlayer().getEyeHeight(EntityPose.STANDING);

			data.setYVel(deltaY / 2);
		}
		helper.applyWaterDrag(data, Submerged.DRAG, Submerged.DRAG_MIN);
		Submerged.drift(data, helper);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				AquaticPoundFlip.AQUATIC_GROUND_POUND,
				new ActionTransitionDetails(
						Submerged.ID,
						data -> !data.getInputs().JUMP.isHeld() || data.getForwardVel() < -0.1,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				Submerged.EXIT_WATER,
				Fall.LANDING.variate(UnderwaterWalk.ID, null)
		);
	}
}
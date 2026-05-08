package com.fqf.mario_qua_mario.actions.aquatic;


import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Paddle implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("paddle");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll *= -1;
			arrangement.addAngles(
					17.5F - factor * MathHelper.sin(progress) * 1,
					0,
					factor * 2
			);
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch *= 0.5F;
			arrangement.addPos(
					factor * -0.675F,
					-2,
					2F
			);
			arrangement.addAngles(
					50 + factor * MathHelper.sin(progress) * 30,
					factor * 6,
					0
			);
	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> ticksPassed / 1.5F),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.y -= 2;
					arrangement.z -= 4;
				}),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.pitch += 27.5F;
					arrangement.yaw += MathHelper.sin(progress) * 5;
				}),
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

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static final CfaStat PADDLE_FALL_SPEED = Submerged.FALL_SPEED.variate(0.5);

	private static final double INTENDED_IMMERSION_LEVEL = 0.75;

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

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

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				AquaticPoundFlip.AQUATIC_GROUND_POUND,
				new TransitionDefinition(
						Submerged.ID,
						data -> !data.getInputs().JUMP.isHeld() || data.getForwardVel() < -0.1,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				Submerged.EXIT_WATER,
				Fall.LANDING.variate(UnderwaterWalk.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
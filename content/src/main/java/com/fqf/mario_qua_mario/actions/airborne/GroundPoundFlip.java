package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundFlip implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("ground_pound_flip");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static final float FLIP_DURATION = 5;
	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.addAngles(
					progress * -67.75F,
					0,
					MathHelper.lerp(progress, factor * 20, factor * -20)
			);
			arrangement.addPos(
					0,
					progress * 1,
					progress * 2.5F
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.addAngles(
					progress * -90,
					progress * factor * 16.75F,
					0
			);
		});
	}
	public static PlayermodelAnimation makeAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> Math.min(ticksPassed / FLIP_DURATION, 1)),
				new EntireBodyAnimation(0.5F, (data, arrangement, progress) -> {
					arrangement.pitch = progress * -360;
					arrangement.y = progress * -8;
				}),
				null,
				null,
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				null
		);
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return makeAnimation(helper);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
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

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		ActionTimerVars.get(data).actionTimer++;
	}

	public static final TransitionDefinition GROUND_POUND = new TransitionDefinition(
			ID,
			data -> data.getInputs().DUCK.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setForwardStrafeVel(0, 0);
				data.setYVel(0.15);
			},
			(data, isSelf, seed) -> data.playSound(MarioContentSFX.GROUND_POUND_FLIP, seed)
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("ground_pound_drop"),
						data -> ActionTimerVars.get(data).actionTimer >= FLIP_DURATION,
						EvaluatorEnvironment.COMMON,
						data -> {
							data.setYVel(GroundPoundDrop.GROUND_POUND_VEL.get(data));
							data.getInputs().JUMP.isPressed();
						},
						(data, isSelf, seed) -> data.storeSound(data.playSound(MarioContentSFX.GROUND_POUND_DROP, seed))
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
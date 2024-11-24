package com.floralquafloral.registries.states.action.baseactions.aquatic;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;

import java.util.List;

public class UnderwaterWalk extends AquaticActionDefinition {
	public static CharaStat REDUCED_FORWARD_ACCEL = AquaticStats.FORWARD_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_FORWARD_SPEED = AquaticStats.FORWARD_SWIM_SPEED.variate(0.475);
	public static CharaStat REDUCED_BACKWARD_ACCEL = AquaticStats.BACKWARD_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_BACKWARD_SPEED = AquaticStats.BACKWARD_SWIM_SPEED.variate(0.475);
	public static CharaStat REDUCED_STRAFE_ACCEL = AquaticStats.STRAFE_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_STRAFE_SPEED = AquaticStats.STRAFE_SWIM_SPEED.variate(0.475);

	public static CharaStat REDUCED_REDIRECTION = AquaticStats.SWIM_REDIRECTION.variate(0.7);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "underwater_walk");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}
	@Override public BumpingRule getBumpingRule() {
		return null;
	}

	@Override public double getGravity() {
		return -0.035;
	}
	@Override public double getTerminalVelocity() {
		return -0.675;
	}
	@Override public double getDrag() {
		return 0.11;
	}
	@Override public double getDragMinimum() {
		return 0.01;
	}

	@Override public void aquaticTravel(MarioTravelData data) {
		data.getTimers().actionTimer++;
		aquaticAccel(data,
				REDUCED_FORWARD_ACCEL,
				REDUCED_FORWARD_SPEED,
				REDUCED_STRAFE_ACCEL,
				REDUCED_STRAFE_SPEED,
				REDUCED_BACKWARD_ACCEL,
				REDUCED_BACKWARD_SPEED,
				1, 1, REDUCED_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}
	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:underwater_duck",
						data -> data.getInputs().DUCK.isHeld(),
						GroundedActionDefinition.GroundedTransitions.DUCK_WADDLE.EXECUTOR_TRAVELLERS,
						(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.DUCK, seed)
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				Swim.SWIM
		);
	}



	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AquaticTransitions.EXIT_WATER,
				AquaticTransitions.FALL
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public boolean interceptAttack(
			MarioData data, @Nullable MarioClientSideData clientData, @Nullable MarioTravelData travelData,
			@Nullable Entity entityTarget, @Nullable BlockPos blockTarget
	) {
		return false;
	}
}

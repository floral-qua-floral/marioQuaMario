package com.floralquafloral.registries.states.action.baseactions.aquatic;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UnderwaterDuck extends AquaticActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "underwater_duck");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
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
		return 0.145;
	}
	@Override public double getDragMinimum() {
		return 0.01;
	}

	@Override public void aquaticTravel(MarioTravelData data) {
		data.getTimers().actionTimer++;
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}
	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:underwater_walk",
						DuckWaddle.UNDUCK.EVALUATOR,
						DuckWaddle.UNDUCK.EXECUTOR_TRAVELLERS,
						DuckWaddle.UNDUCK.EXECUTOR_CLIENTS
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
				new ActionTransitionDefinition("qua_mario:duck_fall",
						AquaticTransitions.EXIT_WATER.EVALUATOR,
						AquaticTransitions.EXIT_WATER.EXECUTOR_TRAVELLERS,
						AquaticTransitions.EXIT_WATER.EXECUTOR_CLIENTS
				),
				AquaticTransitions.FALL
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

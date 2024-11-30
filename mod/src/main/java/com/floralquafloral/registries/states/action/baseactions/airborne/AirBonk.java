package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AirBonk extends Fall {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "bonk_air");
	}
	@Override public @Nullable String getAnimationName() {
		return "bonk-air";
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return new BumpingRule(0, 1);
	}

	@Override public void airborneTravel(MarioTravelData data) {

	}

	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				new ActionTransitionDefinition("qua_mario:bonk_ground",
						AerialTransitions.BASIC_LANDING.EVALUATOR,
						AerialTransitions.BASIC_LANDING.EXECUTOR_TRAVELLERS,
						AerialTransitions.BASIC_LANDING.EXECUTOR_CLIENTS
				)
		);
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

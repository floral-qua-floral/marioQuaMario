package com.floralquafloral.registries.states.action.baseactions.aquatic;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Submerged extends AquaticActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "submerged");
	}
	@Override public @Nullable String getAnimationName() {
		return "submerged";
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
		return BumpingRule.SWIMMING;
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
		aquaticAccel(data);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}
	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AquaticTransitions.AQUATIC_GROUND_POUND,
				Swim.SWIM
		);
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AquaticTransitions.EXIT_WATER,
				AquaticTransitions.LANDING
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

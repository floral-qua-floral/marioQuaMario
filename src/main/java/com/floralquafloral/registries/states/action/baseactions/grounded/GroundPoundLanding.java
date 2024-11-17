package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class GroundPoundLanding extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound_landing");
	}
	@Override public @Nullable String getAnimationName() {
		return "ground-pound-landing";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	@Override
	public void groundedTravel(MarioTravelData data) {
		data.getTimers().actionTimer++;
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioServerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> data.getTimers().actionTimer > 3 && data.getInputs().DUCK.isHeld() && ((MarioMainClientData) data).canRepeatPound
				),
				new ActionTransitionDefinition("qua_mario:basic",
						data -> (data.getTimers().actionTimer > 4 && !data.getInputs().DUCK.isHeld())
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> GroundedTransitions.FALL.EVALUATOR.shouldTransition(data) && data.getInputs().DUCK.isHeld()
				),
				GroundedTransitions.FALL
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}

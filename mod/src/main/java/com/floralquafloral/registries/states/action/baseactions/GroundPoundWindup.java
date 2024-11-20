package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.ActionDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.util.MixedEasing.*;

public class GroundPoundWindup implements ActionDefinition {
	@Override @NotNull public Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "ground_pound_windup");
	}
	@Override @Nullable public String getAnimationName() {
		return "ground-pound-windup";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return new CameraAnimationSet(
				new CameraAnimation(
						false, 0.35F,
						(progress, offsets) -> offsets[1] = mixedEase(progress, SINE, CUBIC) * 360
				),
				null,
				null
		);
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	@Override
	public void travelHook(MarioTravelData data) {
		data.getTimers().actionTimer++;
		data.setForwardStrafeVel(0, 0);
		data.setYVel(0);
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

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

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> data.getTimers().actionTimer > 4
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}

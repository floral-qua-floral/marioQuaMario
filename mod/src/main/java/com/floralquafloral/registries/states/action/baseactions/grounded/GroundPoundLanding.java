package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;

import java.util.List;

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

	@Override public void serverTick(MarioAuthoritativeData data) {}

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
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:aquatic_ground_pound",
						data -> GroundedTransitions.ENTER_WATER.EVALUATOR.shouldTransition(data)
								&& data.getTimers().actionTimer > 3
								&& data.getInputs().DUCK.isHeld()
								&& ((MarioMainClientData) data).canRepeatPound,
						data -> data.setYVel(-0.25)
				),
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> data.getTimers().actionTimer > 3
								&& data.getInputs().DUCK.isHeld()
								&& ((MarioMainClientData) data).canRepeatPound,
						data -> data.setYVel(-0.25)
				),
				new ActionTransitionDefinition("qua_mario:underwater_walk",
						data -> GroundedTransitions.ENTER_WATER.EVALUATOR.shouldTransition(data)
								&& data.getTimers().actionTimer > 4
								&& !data.getInputs().DUCK.isHeld()
				),
				new ActionTransitionDefinition("qua_mario:basic",
						data -> (data.getTimers().actionTimer > 4
								&& !data.getInputs().DUCK.isHeld())
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
		);
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:aquatic_ground_pound",
						data -> GroundedTransitions.ENTER_WATER.EVALUATOR.shouldTransition(data)
								&& GroundedTransitions.FALL.EVALUATOR.shouldTransition(data)
								&& data.getInputs().DUCK.isHeld()
				),
				new ActionTransitionDefinition("qua_mario:ground_pound",
						data -> GroundedTransitions.FALL.EVALUATOR.shouldTransition(data) && data.getInputs().DUCK.isHeld()
				),
				AquaticActionDefinition.AquaticTransitions.FALL,
				GroundedTransitions.FALL
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public boolean interceptAttack(
			MarioData data, @Nullable MarioClientSideData clientData, @Nullable MarioTravelData travelData,
			@Nullable Entity entityTarget, @Nullable BlockPos blockTarget
	) {
		return false;
	}
}

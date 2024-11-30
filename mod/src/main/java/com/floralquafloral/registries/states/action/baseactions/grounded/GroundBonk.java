package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.registries.states.action.baseactions.airborne.Sideflip;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public class GroundBonk extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "bonk_ground");
	}
	@Override public @Nullable String getAnimationName() {
		return "bonk-ground";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	public static final CharaStat BONK_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat BONK_DRAG_MIN = new CharaStat(0.02, RUNNING, DRAG);
	public static final CharaStat BONK_REDIRECTION = new CharaStat(4.5, RUNNING, REDIRECTION);

	@Override
	public void groundedTravel(MarioTravelData data) {
		applyDrag(data,
				BONK_DRAG,
				BONK_DRAG_MIN,
				0,
				0,
				BONK_REDIRECTION
		);
		if(MathHelper.approximatelyEquals(data.getMario().getVelocity().horizontalLengthSquared(), 0.0)) data.getTimers().actionTimer++;
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioAuthoritativeData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:basic",
						data -> data.getTimers().actionTimer > 0
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				GroundedTransitions.ENTER_WATER,
				new ActionTransitionDefinition("qua_mario:bonk_air",
						GroundedTransitions.FALL.EVALUATOR,
						GroundedTransitions.FALL.EXECUTOR_TRAVELLERS,
						GroundedTransitions.FALL.EXECUTOR_CLIENTS
				)
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

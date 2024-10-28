package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class DuckSlide extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_slide");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}

	public static final CharaStat SLIDE_THRESHOLD = new CharaStat(0.25, DUCKING, THRESHOLD);
	public static final CharaStat SLIDE_BOOST = new CharaStat(-0.15);

	public static final CharaStat SLIDE_DRAG = new CharaStat(0.04333, DUCKING, DRAG);
	public static final CharaStat SLIDE_DRAG_MIN = new CharaStat(0.01, DUCKING, DRAG);
	public static final CharaStat SLIDE_REDIRECTION = new CharaStat(4.0, DUCKING, REDIRECTION);

	@Override
	public void groundedSelfTick(MarioClientData data) {
		applyDrag(data,
				SLIDE_DRAG,
				SLIDE_DRAG_MIN,
				Input.getForwardInput(),
				Input.getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override public void otherClientsTick(MarioPlayerData data) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.SLIP;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				DuckWaddle.UNDUCK,
				new ActionTransitionDefinition( // Run out of speed
						"qua_mario:duck_waddle",
						(data) -> MathHelper.approximatelyEquals(Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()), 0.0)
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
		final ActionTransitionDefinition.TransitionEvaluator SLIDE_EVALUATOR = (data) -> {
			double threshold = SLIDE_THRESHOLD.get(data);
			return
					Input.DUCK.isHeld()
							&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold
							&& !data.getAction().ID.equals(getID());
		};

		return List.of(
				new ActionTransitionInjection(
						"qua_mario:duck_waddle",
						ActionTransitionInjection.ActionCategory.GROUNDED,
						new ActionTransitionDefinition(
								"qua_mario:duck_slide",
								SLIDE_EVALUATOR,
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_CLIENT,
								GroundedTransitions.DUCK_WADDLE.EXECUTOR_SERVER
						)
				),
				new ActionTransitionInjection(
						"qua_mario:duck_waddle",
						ActionTransitionInjection.ActionCategory.AIRBORNE,
						new ActionTransitionDefinition(
								"qua_mario:duck_slide",
								SLIDE_EVALUATOR,
								null,
								null
						)
				)
		);
	}
}

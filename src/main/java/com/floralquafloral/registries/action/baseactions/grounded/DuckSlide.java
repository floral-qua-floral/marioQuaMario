package com.floralquafloral.registries.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.action.GroundedActionDefinition;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import java.util.List;

import static com.floralquafloral.CharaStat.*;

public class DuckSlide extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_slide");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}

	@Override
	public void groundedSelfTick(MarioClientData data) {
		applyDrag(data,
				DUCK_SLIDE_DRAG.get(data),
				DUCK_SLIDE_DRAG_MIN.get(data),
				Input.getForwardInput(),
				Input.getStrafeInput(),
				DUCK_SLIDE_REDIRECTION.get(data)
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
		return List.of(
				new ActionTransitionInjection(
						"qua_mario:duck_waddle",
						new ActionTransitionDefinition(
								"qua_mario:duck_slide",
								(data) -> {
									double threshold = DUCK_SLIDE_THRESHOLD.get(data);
									return
											Input.DUCK.isHeld()
											&& !data.getAction().ID.equals(getID())
											&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold;
								}
						)
				)
		);
	}
}

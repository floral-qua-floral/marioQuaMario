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

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class Skid extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "skid");
	}
	@Override public @Nullable String getAnimationName() {
		return "skid";
	}

	public static final CharaStat SKID_THRESHOLD = new CharaStat(0.285, RUNNING, THRESHOLD);

	public static final CharaStat SKID_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat SKID_DRAG_MIN = new CharaStat(0.02, RUNNING, DRAG);
	public static final CharaStat SKID_REDIRECTION = new CharaStat(4.5, RUNNING, REDIRECTION);

	public static final ActionTransitionDefinition SKID_TRANSITION = new ActionTransitionDefinition(
			"qua_mario:skid",
			data -> Input.getForwardInput() < -0.65 && data.getForwardVel() > SKID_THRESHOLD.get(data)
	);

	@Override
	public void groundedTravel(MarioClientData data) {
		applyDrag(data,
				SKID_DRAG,
				SKID_DRAG_MIN,
				-Input.getForwardInput(),
				Input.getStrafeInput(),
				SKID_REDIRECTION
		);
		if(MathHelper.approximatelyEquals(data.getForwardVel(), 0.0)) data.actionTimer++;
	}

	@Override public void clientTick(MarioPlayerData data, boolean isSelf) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.SKIDDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				GroundedTransitions.FALL,
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:basic",
						data -> (data.actionTimer > 0 || Input.getForwardInput() >= 0 || data.getForwardVel() < -0.05)
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				// Sideflip!!!
				GroundedTransitions.JUMP
		);
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

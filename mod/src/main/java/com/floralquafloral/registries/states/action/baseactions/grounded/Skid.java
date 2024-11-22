package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.Sideflip;
import com.floralquafloral.definitions.actions.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public class Skid extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "skid");
	}
	@Override public @Nullable String getAnimationName() {
		return "skid";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	public static final CharaStat SKID_THRESHOLD = new CharaStat(0.285, RUNNING, THRESHOLD);

	public static final CharaStat SKID_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat SKID_DRAG_MIN = new CharaStat(0.02, RUNNING, DRAG);
	public static final CharaStat SKID_REDIRECTION = new CharaStat(4.5, RUNNING, REDIRECTION);

	public static final ActionTransitionDefinition SKID_TRANSITION = new ActionTransitionDefinition(
			"qua_mario:skid",
			data -> data.getInputs().getForwardInput() < -0.65 && data.getForwardVel() > SKID_THRESHOLD.get(data)
	);

	@Override
	public void groundedTravel(MarioTravelData data) {
		applyDrag(data,
				SKID_DRAG,
				SKID_DRAG_MIN,
				-data.getInputs().getForwardInput(),
				data.getInputs().getStrafeInput(),
				SKID_REDIRECTION
		);
		if(MathHelper.approximatelyEquals(data.getForwardVel(), 0.0)) data.getTimers().actionTimer++;
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioAuthoritativeData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.SKIDDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:basic",
						data -> (data.getTimers().actionTimer > 0 || data.getInputs().getForwardInput() >= 0 || data.getForwardVel() < -0.05)
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:sideflip",
						data -> data.getForwardVel() < Sideflip.SIDEFLIP_THRESHOLD.get(data) && data.getInputs().JUMP.isPressed(),
						data -> {
							GroundedTransitions.performJump(data, Sideflip.SIDEFLIP_VEL, null);
							data.setForwardStrafeVel(Sideflip.SIDEFLIP_BACKWARDS_SPEED.get(data), 0);
							data.getMario().setYaw(data.getMario().getYaw() + 180);
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(MarioClientSideData.VoiceLine.SIDEFLIP, seed);
						}
				),
				GroundedTransitions.JUMP
		);
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				GroundedTransitions.ENTER_WATER,
				GroundedTransitions.FALL
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}

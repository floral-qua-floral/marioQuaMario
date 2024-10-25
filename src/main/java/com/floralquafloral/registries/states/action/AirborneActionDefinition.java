package com.floralquafloral.registries.states.action;

import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.stats.CharaStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AirborneActionDefinition implements ActionDefinition {
	public static boolean jumpCapped;

	public abstract static class AerialTransitions {
		public static final ActionTransitionDefinition BASIC_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				(data) -> data.getMario().isOnGround()
		);
		public static final ActionTransitionDefinition DOUBLE_JUMPABLE_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				BASIC_LANDING.EVALUATOR,
				(data, isSelf, seed) -> {
					if(isSelf && data instanceof MarioClientData clientData)
						clientData.jumpLandingTime = 5;
				},
				(data, seed) -> {

				}
		);
		public static final ActionTransitionDefinition TRIPLE_JUMPABLE_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				BASIC_LANDING.EVALUATOR,
				(data, isSelf, seed) -> {
					if(isSelf && data instanceof MarioClientData clientData)
						clientData.doubleJumpLandingTime = 5;
				},
				(data, seed) -> {

				}
		);
	}

	private final @NotNull CharaStat GRAVITY = getGravity();
	private final @NotNull CharaStat JUMP_GRAVITY = getJumpGravity();
	private final @Nullable CharaStat JUMP_CAP = getJumpCap();
	//TODO: Make Grounded states use CharaStats as well!

	protected abstract @NotNull CharaStat getGravity();
	protected abstract @NotNull CharaStat getJumpGravity();
	protected abstract @Nullable CharaStat getJumpCap();

	@Override public final void selfTick(MarioClientData data) {
		double yVel = data.getYVel();
		boolean aboveJumpCap = JUMP_CAP != null && yVel > JUMP_CAP.getValue(data);

		CharaStat useGravity = aboveJumpCap ? JUMP_GRAVITY : GRAVITY;
		yVel += useGravity.getValue(data);
		if(aboveJumpCap && !Input.JUMP.isHeld() && !jumpCapped) {
			yVel = JUMP_CAP.getValue(data);
			jumpCapped = true;
		}

		data.setYVel(yVel);
		aerialSelfTick();
	}

	public abstract void aerialSelfTick();
}

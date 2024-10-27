package com.floralquafloral.registries.states.action;

import com.floralquafloral.VoiceLine;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.baseactions.airborne.Jump;
import com.floralquafloral.registries.states.action.baseactions.grounded.PRun;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.util.ClientSoundPlayer;
import com.floralquafloral.util.JumpSoundPlayer;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import static com.floralquafloral.MarioQuaMario.LOGGER;

public abstract class GroundedActionDefinition implements ActionDefinition {
	public static final CharaStat ZERO = new CharaStat(0.0);

	public abstract static class GroundedTransitions {
		public static void performJump(MarioData data, CharaStat velocityStat, @Nullable CharaStat addendStat,
									   long seed, boolean playSound) {
			if(data.getMario().isMainPlayer() || !data.getMario().getWorld().isClient) {
				double jumpVel = velocityStat.get(data);
				if(addendStat != null)
					jumpVel += Math.max(0.0, data.getForwardVel() / PRun.P_SPEED.get(data)) * addendStat.get(data);

				data.setYVel(jumpVel);
			}

			if(data.getMario().getWorld().isClient && playSound) JumpSoundPlayer.playJumpSfx(data, seed);
		}

		public static final ActionTransitionDefinition FALL = new ActionTransitionDefinition(
				"qua_mario:fall",
				(data) -> !data.getMario().isOnGround()
		);

		public static final ActionTransitionDefinition JUMP = new ActionTransitionDefinition(
				"qua_mario:jump",
				(data) -> Input.JUMP.isPressed(),
				(data, isSelf, seed) -> performJump(data, Jump.JUMP_VEL, Jump.JUMP_ADDEND, seed, true),
				(data, seed) -> performJump(data, Jump.JUMP_VEL, Jump.JUMP_ADDEND, seed, false)
		);

		public static final ActionTransitionDefinition DUCK_WADDLE = new ActionTransitionDefinition(
				"qua_mario:duck_waddle",
				(data) -> Input.DUCK.isHeld(),
				(data, isSelf, seed) -> {
					// Play duck voiceline
					ClientSoundPlayer.playSound(MarioSFX.DUCK, data, 0.5F, 1.0F, seed);
					VoiceLine.DUCK.play(data, seed);
				},
				null
		);
	}

	@Override public final void selfTick(MarioClientData data) {
		data.setYVel(data.getYVel() - 0.01);
		AirborneActionDefinition.jumpCapped = false;
		this.groundedSelfTick(data);
	}

	public abstract void groundedSelfTick(MarioClientData data);

	public void groundAccel(
			MarioClientData data,
			CharaStat forwardAccel, CharaStat forwardTarget, CharaStat strafeAccel, CharaStat strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectDelta
	) {
		double slipFactor = getSlipFactor(data);
		data.approachAngleAndAccel(
				forwardAccel.get(data) * slipFactor, forwardTarget.get(data) * Input.getForwardInput(),
				strafeAccel.get(data) * slipFactor, strafeTarget.get(data) * Input.getStrafeInput(),
				forwardAngleContribution, strafeAngleContribution, redirectDelta.get(data) * slipFactor
		);
	}

	public void applyDrag(
			MarioClientData data,
			CharaStat drag, CharaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution,
			CharaStat redirection
	) {
		double dragValue = drag.get(data);
		boolean dragInverted = dragValue < 0;
		double slipFactor = getSlipFactor(data);
		double dragMinValue = dragMin.get(data) * slipFactor;
		if(!dragInverted) dragValue *= slipFactor;


		Vector2d deltaVelocities = new Vector2d(
				-dragValue * data.getForwardVel(),
				-dragValue * data.getStrafeVel()
		);
		double dragVelocitySquared = deltaVelocities.lengthSquared();
		if(dragVelocitySquared != 0 && dragVelocitySquared < dragMinValue * dragMinValue)
			deltaVelocities.normalize(dragMinValue);

		if(dragInverted) {
			data.setForwardStrafeVel(data.getForwardVel() + deltaVelocities.x, data.getStrafeVel() + deltaVelocities.y);
		}
		else {
			data.approachAngleAndAccel(
					deltaVelocities.x, 0,
					deltaVelocities.y, 0,
					forwardAngleContribution,
					strafeAngleContribution,
					redirection.get(data) * slipFactor
			);
		}
	}

	public static double getSlipFactor(MarioData data) {
		return Math.pow(0.6 / getFloorSlipperiness(data), 3);
	}
	private static float getFloorSlipperiness(MarioData data) {
		if(data.getMario().isOnGround()) {
			BlockPos blockPos = data.getMario().getVelocityAffectingPos();
			return data.getMario().getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}
}

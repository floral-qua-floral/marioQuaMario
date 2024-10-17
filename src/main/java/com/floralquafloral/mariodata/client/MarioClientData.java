package com.floralquafloral.mariodata.client;

import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.action.ParsedAction;
import com.floralquafloral.registries.action.TransitionPhase;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MarioClientData extends MarioPlayerData {
	private static MarioClientData instance;
	public static MarioClientData getInstance() {
		return instance;
	}

	public final ClientPlayerEntity MARIO_CLIENT;

	public MarioClientData(ClientPlayerEntity mario) {
		super(mario);
		this.MARIO_CLIENT = mario;
		MarioClientData.instance = this;
	}

	public int actionTimer = 0;
	@Override public void setAction(ParsedAction action) {
		if(action != getAction()) actionTimer = 0;
		super.setActionTransitionless(action);
	}

	public int jumpLandingTime = 0;
	public int doubleJumpLandingTime = 0;

	@Override public void setVelocities(double forward, double strafe, @Nullable Double vertical) {
		if(this.velocities != null) {
			this.velocities.setForward(forward);
			this.velocities.setStrafe(strafe);
			if(vertical != null) this.velocities.setVertical(vertical);
		}
		else super.setVelocities(forward, strafe, vertical);
	}

	private ClientVelocityContainer velocities;
	@Override @NotNull public MarioVelocityContainer getVelocities() {
		if(this.velocities == null) this.velocities = new ClientVelocities();
		return this.velocities;
	}
	@Override public void clearCachedVelocities() {
		this.velocities = null;
	}

	private interface ClientVelocityContainer extends MarioVelocityContainer {
		void setForward(double forward);
		void setStrafe(double strafe);
		void setVertical(double vertical);
	}

	private class ClientVelocities implements ClientVelocityContainer {
		private double forward, strafe, vertical;

		public ClientVelocities() {
			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(MARIO.getYaw());
			double negativeSineYaw = -Math.sin(yawRad);
			double cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			Vec3d currentVel = MARIO.getVelocity();
			this.forward = currentVel.x * negativeSineYaw + currentVel.z * cosineYaw;
			this.strafe = currentVel.x * cosineYaw + currentVel.z * -negativeSineYaw;
			this.vertical = currentVel.y;
		}
		@Override public double getForward() {
			return this.forward;
		}
		@Override public double getStrafe() {
			return this.strafe;
		}
		@Override public double getVertical() {
			return this.vertical;
		}
		@Override public void setForward(double forward) {
			this.forward = forward;
		}
		@Override public void setStrafe(double strafe) {
			this.strafe = strafe;
		}
		@Override public void setVertical(double vertical) {
			this.vertical = vertical;
		}
	}

	@Override public void tick() {
		Input.update((ClientPlayerEntity) MARIO);
	}

	public boolean travel(Vec3d movementInput) {
		tick();
		Input.updateDirections(movementInput.z, movementInput.x);

		getAction().attemptTransitions(this, TransitionPhase.PRE_TICK);
		getAction().selfTick(this);
		getAction().attemptTransitions(this, TransitionPhase.POST_TICK);

		MarioVelocityContainer cachedVelocities = getVelocities();
		this.clearCachedVelocities();
		setVelocities(cachedVelocities);

		MARIO.move(MovementType.PLAYER, MARIO.getVelocity());
		getAction().attemptTransitions(this, TransitionPhase.POST_MOVE);

		MARIO.updateLimbs(false);
		return true;
	}

}

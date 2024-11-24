package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;

import java.util.List;

public class Fall extends AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "fall");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return BumpingRule.FALLING;
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "stomp");
	}

	@Override protected @NotNull CharaStat getGravity() {
		return AerialStats.GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return null;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}

	@Override public void airborneTravel(MarioTravelData data) {
		airborneAccel(data);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND
		);
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				AerialTransitions.BASIC_LANDING
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	public static boolean attemptKick(
			MarioData data, @Nullable MarioClientSideData clientData, @Nullable MarioTravelData travelData,
			@Nullable Entity entityTarget, @Nullable BlockPos blockTarget
	) {
//		if(travelData != null && travelData.getYVel() < 0) {
//			travelData.setYVel(0.4);
//			return true;
//		}
		return false;
	}

	@Override public boolean interceptAttack(
			MarioData data, @Nullable MarioClientSideData clientData, @Nullable MarioTravelData travelData,
			@Nullable Entity entityTarget, @Nullable BlockPos blockTarget
	) {
		return attemptKick(data, clientData, travelData, entityTarget, blockTarget);
	}
}

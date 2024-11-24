package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.ActionDefinition;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.minecraft.entity.Entity;

import java.util.List;

public class ActionDebug implements ActionDefinition {
	@Override @NotNull public Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "debug");
	}
	@Override @Nullable public String getAnimationName() {
		return null;
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return null;
	}

	@Override
	public void travelHook(MarioTravelData data) {
		data.getTimers().actionTimer++;
		data.setForwardStrafeVel(data.getInputs().getForwardInput() * 0.5, data.getInputs().getStrafeInput() * 0.5);
		data.setYVel(data.getInputs().JUMP.isHeld() ? 0.4 : (data.getInputs().DUCK.isHeld() ? -0.4 : (0.03 * Math.sin((double) data.getTimers().actionTimer++ / 16))));
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition(
						"qua_mario:debug_alt",
						(data) -> { //evaluator
							return data.getMario().isSprinting();
						},
						data -> { // executor for the server and Mario's own client

						},
						(data, isSelf, seed) -> { // executor for all clients (Mario and anyone nearby)
							data.voice(MarioClientSideData.VoiceLine.FIREBALL, seed);
						}
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
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

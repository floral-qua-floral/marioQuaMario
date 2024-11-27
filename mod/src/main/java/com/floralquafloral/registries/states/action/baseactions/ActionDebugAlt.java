package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.ActionDefinition;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionDebugAlt implements ActionDefinition {
	@Override @NotNull public Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "debug_alt");
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
		data.setStrafeVel(data.getInputs().getStrafeInput() * 0.5);

		double pitchRadians = Math.toRadians(data.getMario().getPitch());
		data.setForwardVel(data.getInputs().getForwardInput() * Math.cos(pitchRadians));
		data.setYVel(data.getInputs().getForwardInput() * -Math.sin(pitchRadians));
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
						"qua_mario:debug",
						(data) -> { //evaluator
							return !data.getMario().isSprinting();
						},
						data -> {

						},
						(data, isSelf, seed) -> data.playSoundEvent(SoundEvents.BLOCK_BEACON_POWER_SELECT, seed)
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

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

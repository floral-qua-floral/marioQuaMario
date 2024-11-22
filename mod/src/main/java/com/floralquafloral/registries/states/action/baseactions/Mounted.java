package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.ActionDefinition;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Mounted implements ActionDefinition {
	@Override @NotNull public Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "mounted");
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
		data.getTimers().jumpCapped = false;
	}

	@Override
	public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override
	public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:backflip",
						data -> !data.getMario().hasVehicle() && ((MarioPlayerData) data).attemptDismount
								&& MarioQuaMario.CONFIG.canBackflipFromVehicles(),
						data -> {
							((MarioPlayerData) data).attemptDismount = false;
							assert DuckWaddle.BACKFLIP.EXECUTOR_TRAVELLERS != null;
							DuckWaddle.BACKFLIP.EXECUTOR_TRAVELLERS.execute(data);
						},
						DuckWaddle.BACKFLIP.EXECUTOR_CLIENTS
				),
				new ActionTransitionDefinition("qua_mario:jump",
						data -> !data.getMario().hasVehicle() && ((MarioPlayerData) data).attemptDismount,
						data -> {
							((MarioPlayerData) data).attemptDismount = false;
							assert GroundedActionDefinition.GroundedTransitions.JUMP.EXECUTOR_TRAVELLERS != null;
							GroundedActionDefinition.GroundedTransitions.JUMP.EXECUTOR_TRAVELLERS.execute(data);
						},
						GroundedActionDefinition.GroundedTransitions.JUMP.EXECUTOR_CLIENTS
				),
				new ActionTransitionDefinition("qua_mario:fall",
						data -> !data.getMario().hasVehicle() && !((MarioPlayerData) data).attemptDismount
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(new ActionTransitionDefinition("qua_mario:mounted",
				(data) -> (!((MarioPlayerData) data).attemptDismount) && data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
				data -> ((MarioPlayerData) data).attemptDismount = true,
				null
		));
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}

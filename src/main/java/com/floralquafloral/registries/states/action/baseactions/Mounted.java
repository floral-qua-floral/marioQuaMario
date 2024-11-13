package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.ActionDefinition;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
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
	public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override
	public void serverTick(MarioServerData data) {

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
	public List<ActionTransitionDefinition> getPreTickTransitions() {
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
				),
				new ActionTransitionDefinition("qua_mario:mounted",
						(data) -> (!((MarioPlayerData) data).attemptDismount) && data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						data -> ((MarioPlayerData) data).attemptDismount = true,
						null
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
		return List.of();
	}
}

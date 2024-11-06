package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.ActionDefinition;
import net.minecraft.sound.SoundCategory;
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

	@Override
	public void travelHook(MarioClientData data) {
		data.actionTimer++;
		data.setStrafeVel(Input.getStrafeInput() * 0.5);

		double pitchRadians = Math.toRadians(data.getMario().getPitch());
		data.setForwardVel(Input.getForwardInput() * Math.cos(pitchRadians));
		data.setYVel(Input.getForwardInput() * -Math.sin(pitchRadians));
	}

	@Override
	public void clientTick(MarioPlayerData data, boolean isSelf) {}

	@Override
	public void serverTick(MarioPlayerData data) {

	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition(
						"qua_mario:debug",
						(data) -> { //evaluator
							return !data.getMario().isSprinting();
						},
						(data, isSelf, seed) -> { //executor for clients
							MarioQuaMario.LOGGER.info("DebugAlt action transition's evaluator for clients (isSelf: {})", isSelf);
							if(isSelf) data.getMario().playSoundToPlayer(
									SoundEvents.BLOCK_BEACON_POWER_SELECT,
									SoundCategory.PLAYERS,
									1.0F,
									1.0F
							);
						},
						(data, seed) -> { //executor for the server
							MarioQuaMario.LOGGER.info("DebugAlt action transition's evaluator for server");
						}
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

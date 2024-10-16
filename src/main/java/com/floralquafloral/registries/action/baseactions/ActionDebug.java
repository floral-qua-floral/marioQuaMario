package com.floralquafloral.registries.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.action.ActionDefinition;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ActionDebug implements ActionDefinition {
	@Override @NotNull public Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "debug");
	}
	@Override @Nullable public String getAnimationName() {
		return null;
	}

	@Override
	public void selfTick(MarioClientData data) {
		data.actionTimer++;
		double yVel = Input.JUMP.isHeld() ? 0.4 : (Input.DUCK.isHeld() ? -0.4 : (0.03 * Math.sin((double) data.actionTimer / 16)));
		data.setVelocities(Input.getForwardInput() * 0.5, Input.getStrafeInput() * 0.5, yVel);
	}

	@Override
	public void otherClientsTick(MarioPlayerData data) {}

	@Override
	public void serverTick(MarioPlayerData data) {

	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition(
						"qua_mario:debug_alt",
						(data) -> { //evaluator
							return data.MARIO.isSprinting();
						},
						(data, isSelf) -> { //executor for self
							MarioQuaMario.LOGGER.info("Debug action transition's evaluator for clients (isSelf: {})", isSelf);
						},
						(data) -> { //executor for the server
							MarioQuaMario.LOGGER.info("Debug action transitions evaluator for server");
							MarioQuaMario.LOGGER.info("Playing sound effect @ {}", data.MARIO.getBlockPos());
							data.MARIO.getWorld().playSound(
									null,
									data.MARIO.getBlockPos(),
									SoundEvents.BLOCK_ANVIL_FALL,
									SoundCategory.PLAYERS
							);
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

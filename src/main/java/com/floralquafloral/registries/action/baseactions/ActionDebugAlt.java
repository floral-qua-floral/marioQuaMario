package com.floralquafloral.registries.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.action.ActionDefinition;
import net.minecraft.client.network.ClientPlayerEntity;
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
	public void selfTick(MarioClientData data) {
		data.actionTimer++;
		double yVel = Input.JUMP.isHeld() ? 0.65 : (Input.DUCK.isHeld() ? -0.65 : (0.01 * Math.sin((double) data.actionTimer / 16)));
		data.setVelocities(Input.getForwardInput() * 0.8, Input.getStrafeInput() * 0.5, yVel);
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
						"qua_mario:debug",
						(data) -> { //evaluator
							return !data.MARIO.isSprinting();
						},
						(data, isSelf) -> { //executor for clients
							MarioQuaMario.LOGGER.info("DebugAlt action transition's evaluator for clients (isSelf: {})", isSelf);
							if(isSelf) data.MARIO.playSoundToPlayer(
									SoundEvents.BLOCK_BEACON_POWER_SELECT,
									SoundCategory.PLAYERS,
									1.0F,
									1.0F
							);
						},
						(data) -> { //executor for the server
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

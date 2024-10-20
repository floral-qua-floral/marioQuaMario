package com.floralquafloral.registries.states.action.baseactions;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.states.action.ActionDefinition;
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
		data.setForwardStrafeVel(Input.getForwardInput() * 0.5, Input.getStrafeInput() * 0.5);
		data.setYVel(Input.JUMP.isHeld() ? 0.4 : (Input.DUCK.isHeld() ? -0.4 : (0.03 * Math.sin((double) data.actionTimer / 16))));
	}

	@Override
	public void otherClientsTick(MarioPlayerData data) {}

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
						"qua_mario:debug_alt",
						(data) -> { //evaluator
							return data.getMario().isSprinting();
						},
						(data, isSelf, seed) -> { //executor for self
							MarioQuaMario.LOGGER.info("Debug action transition's evaluator for clients (isSelf: {})", isSelf);
						},
						(data, seed) -> { //executor for the server
							MarioQuaMario.LOGGER.info("Debug action transitions evaluator for server");
							MarioQuaMario.LOGGER.info("Playing sound effect @ {}", data.getMario().getBlockPos());
							data.getMario().getWorld().playSound(
									null,
									data.getMario().getBlockPos(),
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

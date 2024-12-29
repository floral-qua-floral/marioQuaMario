package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.GenericActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.*;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Debug implements GenericActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("debug");
	}

	@Override public @Nullable String getAnimationName() {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data) {
		data.getTimers().actionTimer++;
		data.setForwardStrafeVel(data.getInputs().getForwardInput() * 0.5, data.getInputs().getStrafeInput() * 0.5);
		data.setYVel(data.getInputs().JUMP.isHeld() ? 0.4 : (data.getInputs().DUCK.isHeld() ? -0.4 : (0.03 * Math.sin((double) data.getTimers().actionTimer++ / 16))));
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("debug_sprint"),
						data -> data.getMario().isSprinting(), EvaluatorEnvironment.COMMON,
						null,
						(data, isSelf, seed) -> data.playSound(SoundEvents.ENTITY_VEX_CHARGE, seed)
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions() {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of(
				new AttackInterceptionDefinition() {
					@Override public @Nullable Identifier getActionTarget() {
						return null;
					}
					@Override public Hand getHandToSwing() {
						return Hand.OFF_HAND;
					}
					@Override public boolean shouldTriggerAttackCooldown() {
						return true;
					}

					@Override public boolean shouldInterceptAttack(
							IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable EntityHitResult entityHitResult
					) {
						return true;
					}
					@Override public @NotNull MiningHandling shouldInterceptMining(
							IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
							BlockHitResult blockHitResult, int miningTicks
					) {
						return (weapon.isEmpty() && miningTicks < 20) ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}

					@Override public void executeTravellers(
							IMarioTravelData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
					@Override public void executeClients(
							IMarioClientData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget,
							long seed
					) {
						data.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), seed);
					}

					@Override public void strikeEntity(
							IMarioData data, ItemStack weapon, float attackCooldownProgress,
							ServerWorld world, @NotNull Entity target
					) {

					}
				}
		);
	}
}

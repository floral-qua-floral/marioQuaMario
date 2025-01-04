package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSprint extends Debug {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("debug_sprint");
	}

	@Override public void travelHook(IMarioTravelData data) {
		data.setStrafeVel(data.getInputs().getStrafeInput() * 0.5);

		double pitchRadians = Math.toRadians(data.getMario().getPitch());
		data.setForwardVel(data.getInputs().getForwardInput() * Math.cos(pitchRadians));
		data.setYVel(data.getInputs().getForwardInput() * -Math.sin(pitchRadians));
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("debug"),
						data -> !data.getMario().isSprinting(), EvaluatorEnvironment.SERVER_ONLY,
						null,
						(data, isSelf, seed) -> data.playSound(MarioContentSFX.DUCK, seed)
				)
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of(
				new AttackInterceptionDefinition() {
					@Override public @Nullable Identifier getActionTarget() {
						return null;
					}
					@Override public Hand getHandToSwing() {
						return Hand.MAIN_HAND;
					}
					@Override public boolean shouldTriggerAttackCooldown() {
						return true;
					}

					@Override public boolean shouldInterceptAttack(
							IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
					) {
						return weapon.isEmpty();
					}

					@Override public @NotNull MiningHandling shouldSuppressMining(
							IMarioReadableMotionData data, ItemStack weapon,
							@NotNull BlockHitResult blockHitResult, int miningTicks
					) {
						return miningTicks < 20 ? MiningHandling.HOLD : MiningHandling.MINE;
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
						data.playSound(MarioContentSFX.YOSHI, seed);
					}

					@Override public void executeServer(
							IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
							ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
				}
		);
	}
}

package com.fqf.mario_qua_mario.definitions.states;

import com.fqf.mario_qua_mario.mariodata.*;
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

public interface AttackInterceptingStateDefinition extends MarioStateDefinition {
	@NotNull List<AttackInterceptionDefinition> getAttackInterceptions();

	enum MiningHandling {
		MINE,
		HOLD,
		INTERCEPT
	}

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldInterceptAttack(
				IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
		);

		@NotNull MiningHandling shouldSuppressMining(
				IMarioReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		);

		void executeTravellers(
				IMarioTravelData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		);
		void executeClients(
				IMarioClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed
		);
		void executeServer(
				IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		);
	}

	abstract class PreventAttack implements AttackInterceptionDefinition {
		@Override public @Nullable Identifier getActionTarget() {
			return null;
		}
		@Override public Hand getHandToSwing() {
			return null;
		}
		@Override public boolean shouldTriggerAttackCooldown() {
			return false;
		}

		@Override public @NotNull MiningHandling shouldSuppressMining(
				IMarioReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		) {
			return this.shouldInterceptAttack(data, weapon, 1F, null, blockHitResult)
					? MiningHandling.INTERCEPT
					: MiningHandling.MINE;
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

		}
		@Override public void executeServer(
				IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
	}
}

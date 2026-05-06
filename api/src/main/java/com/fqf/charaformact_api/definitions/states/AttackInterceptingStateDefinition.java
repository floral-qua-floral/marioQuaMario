package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
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

public interface AttackInterceptingStateDefinition extends CfaStateDefinition {
	@NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper);

	enum MiningHandling {
		MINE, // Player mines the block without executing the interception
		HOLD, // Attack interception does not trigger, nor does the player start mining
		INTERCEPT // Attack interception occurs immediately and mining is prevented
	}

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldInterceptAttack(
				CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
		);

		@NotNull MiningHandling shouldSuppressMining(
				CfaReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		);

		void executeTravellers(
				CfaTravelData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		);
		void executeClients(
				CfaClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed
		);
		void executeServer(
				CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
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
				CfaReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		) {
			return this.shouldInterceptAttack(data, weapon, 1F, null, blockHitResult)
					? MiningHandling.INTERCEPT
					: MiningHandling.MINE;
		}

		@Override public void executeTravellers(
				CfaTravelData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
		@Override public void executeClients(
				CfaClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget,
				long seed
		) {

		}
		@Override public void executeServer(
				CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
	}
}

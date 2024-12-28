package com.fqf.mario_qua_mario.definitions.states;

import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
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

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldInterceptAttack(
				IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable EntityHitResult entityHitResult
		);

		@NotNull MiningHandling shouldInterceptMining(
				IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				BlockHitResult blockHitResult, int miningTicks
		);

		void executeTravellers(
				IMarioTravelData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget,
				@Nullable Entity entityTarget
		);

		void executeClients(
				IMarioClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget,
				@Nullable Entity entityTarget,
				long seed
		);

		void strikeEntity(
				IMarioData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @NotNull Entity target
		);
	}

	enum MiningHandling {
		MINE,
		HOLD,
		INTERCEPT
	}
}

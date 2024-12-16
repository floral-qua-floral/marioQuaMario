package com.fqf.mario_qua_mario.definitions;

import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AttackInterceptingStateDefinition extends MarioStateDefinition {
	@NotNull List<AttackInterceptionDefinition> getUnarmedAttackInterceptions();

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldIntercept(
				IMarioReadableMotionData data, float attackCooldownProgress,
				@Nullable BlockHitResult blockHitResult,
				@Nullable EntityHitResult entityHitResult
		);

		void executeTravellers(
				IMarioTravelData data, float attackCooldownProgress,
				@Nullable BlockHitResult blockHitResult,
				@Nullable EntityHitResult entityHitResult
		);

		void executeClients(
				IMarioClientData data, float attackCooldownProgress,
				@Nullable BlockHitResult blockHitResult,
				@Nullable EntityHitResult entityHitResult
		);

		void strikeEntity(
				IMarioData data, float attackCooldownProgress,
				ServerWorld world, @NotNull Entity target
		);
	}
}

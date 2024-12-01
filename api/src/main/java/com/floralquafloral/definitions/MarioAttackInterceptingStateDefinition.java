package com.floralquafloral.definitions;

import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MarioAttackInterceptingStateDefinition extends MarioStateDefinition {
	List<AttackInterceptionDefinition> getUnarmedAttackInterceptions();

	interface AttackInterceptionDefinition {
		@Nullable Identifier getActionTarget();
		@Nullable Hand getHandToSwing();
		boolean shouldTriggerAttackCooldown();

		boolean shouldIntercept(
				MarioTravelData data, float attackCooldownProgress,
				@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
		);

		void executeTravellers(
				MarioTravelData data,
				@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
		);

		void executeClients(
				MarioClientSideData data, boolean isSelf, long seed,
				@Nullable Entity targetEntity,  @Nullable BlockPos targetBlock
		);

		void strikeEntity(
				MarioData data, float attackCooldownProgress,
				ServerWorld world, @NotNull Entity target
		);


	}
}

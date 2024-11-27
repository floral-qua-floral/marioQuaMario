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

//	class AttackInterceptionDefinition {
//		@FunctionalInterface public interface InterceptionEvaluator {
//			boolean shouldIntercept(
//					MarioTravelData data,
//					@Nullable Entity targetEntity, @Nullable BlockPos targetBlock,
//					float attackCooldownProgress
//			);
//		}
//		@FunctionalInterface public interface InterceptionExecutorTravelling {
//			void execute(MarioTravelData data, @Nullable Entity targetEntity, @Nullable BlockPos targetBlock);
//		}
//		@FunctionalInterface public interface InterceptionExecutorClients {
//			void execute(
//					MarioClientSideData data, boolean isSelf, long seed,
//					@Nullable Entity targetEntity,  @Nullable BlockPos targetBlock
//			);
//		}
//
//		@FunctionalInterface public interface DamageCalculator {
//			float calculateDamage(MarioData data, @NotNull Entity targetEntity);
//		}
//
//		public final @Nullable Identifier TARGET_IDENTIFIER;
//		public final InterceptionEvaluator EVALUATOR;
//
//		public final @Nullable InterceptionExecutorTravelling EXECUTOR_TRAVELLERS;
//		public final @Nullable InterceptionExecutorClients EXECUTOR_CLIENTS;
//
//		public final @Nullable String DAMAGE_TYPE;
//		public final @Nullable DamageCalculator DAMAGE_CALCULATOR;
//
//		public final @Nullable Hand SWING_HAND;
//		public final boolean TRIGGER_COOLDOWN;
//
//		public AttackInterceptionDefinition(
//				@Nullable String targetActionID,
//				InterceptionEvaluator evaluator,
//				@Nullable InterceptionExecutorTravelling executeTravel,
//				@Nullable InterceptionExecutorClients executeClients,
//
//				@Nullable String damageType,
//				@Nullable DamageCalculator damageCalculator,
//
//				@Nullable Hand swingHand,
//				boolean triggerAttackCooldown
//		) {
//			this.TARGET_IDENTIFIER = targetActionID == null ? null : Identifier.of(targetActionID);
//			this.EVALUATOR = evaluator;
//
//			this.EXECUTOR_TRAVELLERS = executeTravel;
//			this.EXECUTOR_CLIENTS = executeClients;
//
//			this.DAMAGE_TYPE = damageType;
//			this.DAMAGE_CALCULATOR = damageCalculator;
//
//			this.SWING_HAND = swingHand;
//			this.TRIGGER_COOLDOWN = triggerAttackCooldown;
//		}
//	}

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

		default void damageEntity(
				MarioData data, float attackCooldownProgress,
				ServerWorld world, Entity target,
				RegistryKey<DamageType> damageType, float amount
		) {
			DamageSource source = new DamageSource(
					world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(damageType),
					data.getMario()
			);

			float progressFactor = 0.2F + attackCooldownProgress * attackCooldownProgress * 0.8F;

			target.damage(source, progressFactor * amount);
		}
	}
}

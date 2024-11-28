package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Kick extends Fall {
	public static final AttackInterceptionDefinition KICK = new AttackInterceptionDefinition() {
		@Override public Identifier getActionTarget() {
			return Identifier.of(MarioQuaMario.MOD_ID, "kick");
		}
		@Override public @Nullable Hand getHandToSwing() {
			return null;
		}
		@Override public boolean shouldTriggerAttackCooldown() {
			return true;
		}

		@Override public boolean shouldIntercept(
				MarioTravelData data, float attackCooldownProgress,
				@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
		) {
			return data.getYVel() < 0;
		}

		@Override public void executeTravellers(
				MarioTravelData data,
				@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
		) {
			data.setYVel(0.675);
			if(targetBlock != null) data.getMario().getWorld().breakBlock(targetBlock, true);
		}

		@Override
		public void executeClients(
				MarioClientSideData data, boolean isSelf, long seed,
				@Nullable Entity targetEntity, @Nullable BlockPos targetBlock
		) {
			data.voice(MarioClientSideData.VoiceLine.WALL_JUMP, seed);
		}

		private static final RegistryKey<DamageType> KICK_DAMAGE_TYPE =
				RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MarioQuaMario.MOD_ID, "kick"));

		@Override public void strikeEntity(
				MarioData data, float attackCooldownProgress,
				ServerWorld world, @NotNull Entity target
		) {
			damageEntity(data, 1F, world, target, KICK_DAMAGE_TYPE, 5);
		}
	};

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "kick");
	}
	@Override public @Nullable String getAnimationName() {
		return "kick";
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				AerialTransitions.DOUBLE_JUMPABLE_LANDING
		);
	}

	@Override
	public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}

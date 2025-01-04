package com.fqf.mario_qua_mario.powerups;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class Fire implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("fire");
	}

	@Override public @Nullable Identifier getReversionTarget() {
		return MarioQuaMarioContent.makeID("super");
	}
	@Override public int getValue() {
		return 2;
	}

	@Override public @Nullable SoundEvent getAcquisitionSound() {
		return null;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public float getVoicePitch() {
		return 1;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		return helper.standard("mario_qua_mario", "fire");
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

	// NOTE! THESE VALUES CAN ONLY BE SAFELY USED FOR THE MAIN CLIENT (i.e. in evaluators)!!!!!
	private static long noMainFireballsUntil;
	private static long noSecondaryFireballsUntil;

	private abstract static class FireballDefinition implements AttackInterceptionDefinition {
		private final Hand HAND;
		private FireballDefinition(Hand hand) {
			this.HAND = hand;
		}

		@Override public @Nullable Identifier getActionTarget() {
			return null;
		}
		@Override public Hand getHandToSwing() {
			return this.HAND;
		}
		@Override public boolean shouldTriggerAttackCooldown() {
			return this.HAND == Hand.MAIN_HAND;
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
			data.playSound(MarioContentSFX.FIREBALL, seed);
			data.voice(IMarioClientData.VoiceLine.FIREBALL, seed);
			if(data.getMario().isMainPlayer()) {
				long time = data.getMario().getWorld().getTime();
				if(this.HAND == Hand.MAIN_HAND) {
					noMainFireballsUntil = time + 12;
					noSecondaryFireballsUntil = time + 3;
				}
				else {
					noMainFireballsUntil = time + 12;
					noSecondaryFireballsUntil = time + 12;
				}
			}
		}

		@Override public void executeServer(
				IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of(
				new FireballDefinition(Hand.MAIN_HAND) {
					private boolean allowedCommon(IMarioReadableMotionData data, ItemStack weapon) {
						return weapon.isEmpty();
					}

					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isEmpty() && data.getMario().getWorld().getTime() > noMainFireballsUntil
								&& attackCooldownProgress >= 1 && (entityHitResult == null || !entityHitResult.getEntity().isFireImmune());
					}

					@Override
					public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
						return miningTicks < 3 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}
				},
				new FireballDefinition(Hand.OFF_HAND) {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						long time = data.getMario().getWorld().getTime();
						return time > noSecondaryFireballsUntil
								&& (time < noMainFireballsUntil || !weapon.isEmpty()) // Only after throwing a first fireball, or any time if holding an item
								&& data.getMario().getOffHandStack().isEmpty()
								&& attackCooldownProgress < 1;
					}

					@Override
					public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
						return miningTicks < 3 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}
				},
				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						long time = data.getMario().getWorld().getTime();
						return attackCooldownProgress < 1 && weapon.isEmpty()
								&& (time < noSecondaryFireballsUntil || time > noMainFireballsUntil);
					}
				}
		);
	}
}

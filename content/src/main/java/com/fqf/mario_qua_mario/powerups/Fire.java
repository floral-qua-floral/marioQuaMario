package com.fqf.mario_qua_mario.powerups;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.entity.custom.MarioFireballProjectileEntity;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
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
	public static final Identifier ID = MarioQuaMarioContent.makeID("fire");
	@Override public @NotNull Identifier getID() {
	    return ID;
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
	@Override public float getAnimationWidthFactor() {
		return 1;
	}
	@Override public float getAnimationHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public float getVoicePitch() {
		return 1;
	}
	@Override public float getJumpPitch() {
		return 1F;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public @NotNull PowerHeart getPowerHeart(PowerHeartHelper helper) {
		return helper.auto();
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new FireFlowerData();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

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
			data.voice(Voicelines.FIREBALL, seed);
			if(data.getMario().isMainPlayer()) {
				long time = data.getMario().getWorld().getTime();
				if(this.HAND == Hand.MAIN_HAND) {
					data.getVars(FireFlowerData.class).noMainFireballsUntil = time + 12;
					data.getVars(FireFlowerData.class).noSecondaryFireballsUntil = time + 3;
				}
				else {
					data.getVars(FireFlowerData.class).noMainFireballsUntil = time + 12;
					data.getVars(FireFlowerData.class).noSecondaryFireballsUntil = time + 12;
				}
			}
		}

		@Override public void executeServer(
				IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {
			ServerPlayerEntity mario = data.getMario();
			MarioFireballProjectileEntity tomahawk = new MarioFireballProjectileEntity(world, mario);
//			tomahawk.setVelocity(mario, mario.getPitch(), mario.getYaw(), 0, FIREBALL_SPEED, 0);
			world.spawnEntity(tomahawk);
		}
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of(
				new FireballDefinition(Hand.MAIN_HAND) {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isEmpty() && data.getMario().getWorld().getTime() > data.getVars(FireFlowerData.class).noMainFireballsUntil
								&& attackCooldownProgress >= 1 && (entityHitResult == null || !entityHitResult.getEntity().isFireImmune());
					}

					@Override
					public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
						return miningTicks <= 3 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}
				},
				new FireballDefinition(Hand.OFF_HAND) {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						MarioQuaMarioContent.LOGGER.info("Attempting offhand fireball toss?");
						long time = data.getMario().getWorld().getTime();
						return time > data.getVars(FireFlowerData.class).noSecondaryFireballsUntil
								// Only after throwing a first fireball, or any time if holding an item
								&& (time < data.getVars(FireFlowerData.class).noMainFireballsUntil || !weapon.isEmpty())
								&& data.getMario().getOffHandStack().isEmpty()
								&& attackCooldownProgress < 1;
					}

					@Override
					public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
						return miningTicks <= 3 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}
				},
				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						long time = data.getMario().getWorld().getTime();
						return attackCooldownProgress < 1 && weapon.isEmpty()
								&& (time < data.getVars(FireFlowerData.class).noSecondaryFireballsUntil || time > data.getVars(FireFlowerData.class).noMainFireballsUntil);
					}
				}
		);
	}

	private static class FireFlowerData {
		private long noMainFireballsUntil;
		private long noSecondaryFireballsUntil;
	}
}

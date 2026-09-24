package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.entity.custom.AbstractMarioProjectileEntity;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
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

public abstract class AbstractProjectileThrowingForm<Projectile extends AbstractMarioProjectileEntity> implements FormDefinition {
	private static class ProjectileCooldowns {
		private long noMainHandUntil;
		private long noOffhandUntil;
	}

	@Override public @Nullable Identifier defineReversionTarget() {
		return Super.ID;
	}
	@Override public int defineValue() {
		return 2;
	}

	@Override public @Nullable SoundEvent defineReversionSound() {
		return MarioSFX.REVERT;
	}
	@Override public @Nullable SoundEvent defineAcquisitionSound() {
		return MarioSFX.EMPOWER;
	}

	@Override public @NotNull FormDefinition.FormHeart defineFormHeart(FormHeartHelper helper) {
		return helper.auto();
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ProjectileCooldowns();
	}

	protected abstract boolean canDirectHitEntity(@NotNull EntityHitResult result);
	protected abstract SoundEvent getThrowSound();
	protected int getTicksBetweenDifferentHandThrows() {
		return 3;
	}
	protected abstract int getTotalCooldown();

	protected abstract @NotNull Projectile instantiateProjectile(ServerWorld world, ServerPlayerEntity player);

	private abstract class ProjectileThrowingInterception implements AttackInterceptionDefinition {
		private final Hand HAND;
		private ProjectileThrowingInterception(Hand hand) {
			this.HAND = hand;
		}

		@Override public Hand defineHandToSwing() {
			return this.HAND;
		}
		@Override public boolean triggersAttackCooldown() {
			return this.HAND == Hand.MAIN_HAND;
		}

		@Override public boolean shouldInterceptAttack(
				CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
		) {
			return (entityHitResult == null || AbstractProjectileThrowingForm.this.canDirectHitEntity(entityHitResult))
					&& this.canThrowProjectile(data, weapon, attackCooldownProgress);
		}

		@Override public @NotNull MiningHandling shouldSuppressMining(
				CfaReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		) {
			return miningTicks <= 3 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
		}

		private void triggerCooldowns(CfaData data) {
			long time = data.getPlayer().getWorld().getTime();
			ProjectileCooldowns cooldowns = data.retrieveStateData(ProjectileCooldowns.class);
			int totalCooldown = AbstractProjectileThrowingForm.this.getTotalCooldown();
			if(this.HAND == Hand.MAIN_HAND) {
				cooldowns.noMainHandUntil = time + totalCooldown;
				cooldowns.noOffhandUntil = time + AbstractProjectileThrowingForm.this.getTicksBetweenDifferentHandThrows();
			}
			else {
				cooldowns.noMainHandUntil = time + totalCooldown;
				cooldowns.noOffhandUntil = time + totalCooldown;
			}
		}

		protected abstract boolean canThrowProjectile(
				CfaData data, ItemStack weapon, float attackCooldownProgress
		);

		@Override public void executeClients(
				CfaClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget,
				long seed
		) {
			data.playSound(AbstractProjectileThrowingForm.this.getThrowSound(), seed);
			data.voice(Voicelines.FIREBALL, seed);
			if(data.getPlayer().isMainPlayer()) {
				this.triggerCooldowns(data);
			}
		}

		@Override public void executeServer(
				CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {
			ServerPlayerEntity player = data.getPlayer();

			if(!this.canThrowProjectile(data, weapon, attackCooldownProgress)) return;
			this.triggerCooldowns(data);

			Projectile projectile = AbstractProjectileThrowingForm.this.instantiateProjectile(player.getServerWorld(), player);
			if(entityTarget != null) {
				// Directly apply the effects the projectile would have on collision, without actually spawning one.
				// This ensures that these power-ups will never cause an attack that would have succeeded for a
				// non-powered-up player to fail instead, which would be really annoying.
				projectile.hitEntity(entityTarget, player, player, entityTarget);
			}
			else if(blockTarget == null || !projectile.attemptBlockInteraction(blockTarget, world)) {
				world.spawnEntity(projectile);
			}
		}
	}

	@Override
	public void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {
		builder.add(
				new ProjectileThrowingInterception(Hand.MAIN_HAND) {
					@Override
					protected boolean canThrowProjectile(CfaData data, ItemStack weapon, float attackCooldownProgress) {
						return weapon.isEmpty()
								&& data.getPlayer().getWorld().getTime() > data.retrieveStateData(ProjectileCooldowns.class).noMainHandUntil
								&& attackCooldownProgress >= 1;
					}
				},

				new ProjectileThrowingInterception(Hand.OFF_HAND) {
					@Override
					protected boolean canThrowProjectile(CfaData data, ItemStack weapon, float attackCooldownProgress) {
						long time = data.getPlayer().getWorld().getTime();
						ProjectileCooldowns cooldowns = data.retrieveStateData(ProjectileCooldowns.class);
						return time > cooldowns.noOffhandUntil
								// Only after throwing a first fireball, or any time if holding an item
								&& (time < cooldowns.noMainHandUntil || !weapon.isEmpty())
								&& data.getPlayer().getOffHandStack().isEmpty()
								&& attackCooldownProgress < 1;
					}
				},

				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						long time = data.getPlayer().getWorld().getTime();
						ProjectileCooldowns cooldowns = data.retrieveStateData(ProjectileCooldowns.class);
						return attackCooldownProgress < 1 && weapon.isEmpty()
								&& (entityHitResult == null || canDirectHitEntity(entityHitResult))
								&& (time < cooldowns.noOffhandUntil || time > cooldowns.noMainHandUntil);
					}
				}
		);
	}
}

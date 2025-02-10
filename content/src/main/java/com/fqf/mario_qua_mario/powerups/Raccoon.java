package com.fqf.mario_qua_mario.powerups;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.Easing;
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

public class Raccoon implements PowerUpDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("raccoon");
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
		return helper.standard("mario_qua_mario", "fire");
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public @Nullable Object setupCustomMarioVars() {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}

	private abstract static class TailAttack implements AttackInterceptionDefinition {
		private final Identifier ACTION_TARGET;

		private TailAttack(Identifier actionTarget) {
			this.ACTION_TARGET = actionTarget;
		}

		@Override
		public @Nullable Identifier getActionTarget() {
			return this.ACTION_TARGET;
		}

		@Override
		public @Nullable Hand getHandToSwing() {
			return null;
		}

		@Override
		public boolean shouldTriggerAttackCooldown() {
			return true;
		}

		@Override
		public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
			return weapon.isEmpty() && attackCooldownProgress >= 1 && this.testSwing(data);
		}

		protected abstract boolean testSwing(IMarioReadableMotionData data);

		@Override
		public @NotNull MiningHandling shouldSuppressMining(IMarioReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
			return miningTicks <= 3 ? MiningHandling.HOLD : MiningHandling.MINE;
		}

		@Override
		public void executeTravellers(IMarioTravelData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

		}

		@Override
		public void executeClients(IMarioClientData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed) {
			data.playSound(MarioContentSFX.TAIL_WHIP, seed);
		}

		@Override
		public void executeServer(IMarioAuthoritativeData data, ItemStack weapon, float attackCooldownProgress, ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

		}
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of(
				new TailAttack(MarioQuaMarioContent.makeID("tail_whip_grounded")) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return data.getMario().isOnGround();
					}
				},
				new TailAttack(MarioQuaMarioContent.makeID("tail_whip_grounded")) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return data.getMario().isOnGround();
					}
				},
				new TailAttack(MarioQuaMarioContent.makeID("tail_whip_grounded")) {
					@Override protected boolean testSwing(IMarioReadableMotionData data) {
						return !data.getMario().isOnGround();
					}
				},
				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isEmpty();
					}
				}
		);
	}

	private static final LimbAnimation ARM_ANIMATION = new LimbAnimation(false, (data, arrangement, progress) -> {

	});
	private static final LimbAnimation LEG_ANIMATION = new LimbAnimation(false, (data, arrangement, progress) -> {

	});
	public static PlayermodelAnimation makeTailSwingAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler(10, false, Easing.LINEAR),

				new EntireBodyAnimation(0, (data, arrangement, progress) ->
						arrangement.yaw = progress * 360),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				ARM_ANIMATION, ARM_ANIMATION,
				LEG_ANIMATION, LEG_ANIMATION,
				new LimbAnimation(false, (data, arrangement, progress) -> {

				})
		);
	}
}

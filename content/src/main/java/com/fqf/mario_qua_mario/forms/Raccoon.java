package com.fqf.mario_qua_mario.forms;

import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.form.TailSpinFall;
import com.fqf.mario_qua_mario.actions.form.TailSpinGround;
import com.fqf.mario_qua_mario.actions.form.TailStall;
import com.fqf.mario_qua_mario.util.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Raccoon implements FormDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("raccoon");

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
		return MarioSFX.TAIL_EMPOWER;
	}

	@Override public float defineWidthFactor() {
		return 1;
	}
	@Override public float defineHeightFactor() {
		return 1;
	}
	@Override public float defineAnimationHorizontalScale() {
		return 1;
	}
	@Override public float defineAnimationVerticalScale() {
		return 1;
	}

	@Override public int defineBapStrengthModifier() {
		return 0;
	}

	@Override public float defineVoicePitch() {
		return 1;
	}
	@Override public float defineJumpPitch() {
		return 1F;
	}

	@Override public void accumulatePowers(ImmutableSet.Builder<String> builder) {
		builder.add(
				Powers.SMB3_IDLE,
				Powers.TAIL_ATTACK,
				Powers.TAIL_STALL,
				Powers.TAIL_FLY,
				Powers.CAN_HIT_PROJECTILES,
				Powers.TAPETUM_LUCIDUM
		);
	}

	@Override public @NotNull FormDefinition.FormHeart defineFormHeart(FormHeartHelper helper) {
		return helper.auto();
	}

	public static class RaccoonVars {
		public int flightTicks;
		public @Nullable Double stallStartVel;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new RaccoonVars();
	}
	private void tick(CfaData data) {
		RaccoonVars vars = data.retrieveStateData(RaccoonVars.class);
		if(data.getActionCategory() != ActionCategory.AIRBORNE && data.getActionCategory() != ActionCategory.WALLBOUND) {
			vars.stallStartVel = null;
			vars.flightTicks = 75;
		}
		else {
			if(vars.stallStartVel != null)
				vars.stallStartVel = Math.max(vars.stallStartVel + TailStall.FALL_ACCEL.get(data), TailStall.FALL_SPEED.get(data));
			vars.flightTicks--;
		}
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		tick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		tick(data);
	}

	public static boolean canBeReflected(Entity reflectTarget) {
		return ((ReflectableEntity) reflectTarget).cfa$canReflect();
	}

	private static final double REFLECTION_SPEED_FACTOR = 1.5;
	private static final double MINIMUM_POST_REFLECTION_SPEED = 2.2;
	public static boolean tryReflect(Entity reflectTarget, PlayerEntity mario, boolean isForward) {
		if(canBeReflected(reflectTarget)) {
			Vec3d originalVelocity = reflectTarget.getVelocity();
			double newSpeed = Math.max(originalVelocity.length() * REFLECTION_SPEED_FACTOR, MINIMUM_POST_REFLECTION_SPEED);
			Vec3d aimVector;
			if(isForward) aimVector = // Aim in Mario's look direction
					Vec3d.fromPolar(mario.getPitch(), mario.getYaw());
			else { // Aim outwards from Mario's center
				Vec3d marioPos = mario.getPos().add(0, mario.getHeight() * 0.25, 0);
				Vec3d targetPos = reflectTarget.getPos().add(0, reflectTarget.getHeight(), 0);
				aimVector = targetPos.subtract(marioPos).normalize();
			}
			Vec3d newVelocity;
			if(reflectTarget instanceof PersistentProjectileEntity persistentProjectileTarget && ((PersistentReflectable) persistentProjectileTarget).mqm$isInGround()) {
				// Reflect a trident, arrow, etc that's lodged in a block
				PersistentReflectable reflectable = ((PersistentReflectable) persistentProjectileTarget);

				Vec3d inversionFactor;
				Direction direction = reflectable.mqm$getGroundStickFace();
				Vec3d identity = new Vec3d(1, 1, 1);
				if(MathHelper.sign(aimVector.getComponentAlongAxis(direction.getAxis())) != direction.getDirection().offset())
					inversionFactor = identity.withAxis(direction.getAxis(), -0.4);
				else
					inversionFactor = identity;

				newVelocity = aimVector.multiply(inversionFactor).multiply(newSpeed);
				reflectable.mqm$dislodge();
				persistentProjectileTarget.setCritical(true);
				PersistentProjectileEntity.PickupPermission pickup = persistentProjectileTarget.pickupType;
				persistentProjectileTarget.setOwner(mario);
				persistentProjectileTarget.pickupType = pickup;
			}
			else if(reflectTarget instanceof ProjectileEntity projectileTarget) {
				// Reflect a projectile mid-flight
//				newVelocity = aimVector.add(originalVelocity.negate().normalize()).normalize().multiply(newSpeed);
				newVelocity = aimVector.multiply(newSpeed);
				if(reflectTarget instanceof PersistentProjectileEntity persistentProjectileTarget) {
					persistentProjectileTarget.setCritical(true);
					PersistentProjectileEntity.PickupPermission pickup = persistentProjectileTarget.pickupType;
					projectileTarget.setOwner(mario);
					persistentProjectileTarget.pickupType = pickup;
				}
				else {
					projectileTarget.setOwner(mario);
				}
			}
			else {
				// Reflect a non-projectile entity
				newVelocity = aimVector.multiply(newSpeed);
				// this is wonky but i really don't wanna do the math god almighty
				reflectTarget.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, reflectTarget.getPos().add(aimVector));

				// Is there any other logic I'd want to do here...?? I could check Ownable to transfer ownership of things
				// like Vexes on reflect, which is a hilarious idea, but it doesn't make enough sense for me.
			}
			reflectTarget.playSound(MarioSFX.KICK, 0.4F, 1);
			reflectTarget.setVelocity(newVelocity);
			reflectTarget.velocityModified = true;
		}
		return false;
	}

	public static final float TAIL_STRIKE_DAMAGE = 5.75F;

	private abstract static class TailAttack implements AttackInterceptionDefinition {
		private final Identifier ACTION_TARGET;
		private final CameraAnimationSet CAMERA_ANIMATIONS;

		private TailAttack(Identifier actionTarget, CameraAnimationSet cameraAnimations) {
			this.ACTION_TARGET = actionTarget;
			this.CAMERA_ANIMATIONS = cameraAnimations;
		}

		@Override
		public @Nullable Identifier defineActionTarget() {
			return this.ACTION_TARGET;
		}

		@Override
		public @Nullable Hand defineHandToSwing() {
			return null;
		}

		@Override
		public boolean triggersAttackCooldown() {
			return true;
		}

		@Override
		public boolean shouldInterceptAttack(CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
			return weapon.isEmpty() && attackCooldownProgress >= 1 && this.testSwing(data);
		}

		protected abstract boolean testSwing(CfaReadableMotionData data);

		@Override
		public @NotNull MiningHandling shouldSuppressMining(CfaReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
			return miningTicks <= 3 ? MiningHandling.HOLD : MiningHandling.MINE;
		}

		@Override
		public void executeTravellers(CfaTravelData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

		}

		@Override
		public void executeClients(CfaClientData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed) {
			data.forceBodyAlignment(false);
			data.playSound(MarioSFX.TAIL_WHIP, seed);
//			if(entityTarget != null) data.playSound(MarioSFX.KICK, seed);
			if(this.ACTION_TARGET == null) {
				data.voice(Voicelines.TAIL_WHIP, seed);
				data.playAnimation(TAIL_WHIP_ANIMATION, TAIL_WHIP_ANIMATION_DURATION);
				data.playCameraAnimation(this.CAMERA_ANIMATIONS);
			}
			else data.voice(Voicelines.TAIL_SPIN, seed);
		}

		@Override
		public void executeServer(CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress, ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {
			ServerPlayerEntity mario = data.getPlayer();
			mario.spawnSweepAttackParticles();
			if(entityTarget != null) {
				DamageSource damageSource = mario.getDamageSources().playerAttack(mario);
				if(!tryReflect(entityTarget, mario, true)) {
					// Only damage entities that aren't reflected!
					entityTarget.damage(damageSource, TAIL_STRIKE_DAMAGE);
					mario.onAttacking(entityTarget);
				}
				Box targetBoundingBox = entityTarget.getBoundingBox();
				List<Entity> sweepTargets = mario.getWorld().getNonSpectatingEntities(Entity.class, targetBoundingBox.expand(1.0, 0.25, 1.0));
				double additionalRange = 3 + targetBoundingBox.getLengthX();
				for(Entity sweepTarget : sweepTargets) {
					if(
							sweepTarget != mario
							&& sweepTarget != entityTarget
							&& !mario.isTeammate(sweepTarget)
							&& !(sweepTarget instanceof Tameable tameable && mario.equals(tameable.getOwner()))
							&& (sweepTarget.canHit() || canBeReflected(sweepTarget))
							&& mario.canInteractWithEntity(sweepTarget, additionalRange)) {

						if(
								!tryReflect(sweepTarget, mario, true)
								&& sweepTarget instanceof LivingEntity livingTarget
								&& sweepTarget.damage(damageSource, TAIL_STRIKE_DAMAGE)
						) {
							livingTarget.takeKnockback(
									0.4F, MathHelper.sin(mario.getYaw() * (float) (Math.PI / 180.0)), -MathHelper.cos(mario.getYaw() * (float) (Math.PI / 180.0))
							);
							mario.onAttacking(sweepTarget);
						}
					}
				}
			}
			else if(this.ACTION_TARGET == null) {
				// Didn't hit an entity and didn't hit a block, so do an airblast-style hitbox reflect
				double radius = mario.getEntityInteractionRange() * 0.5;
				Vec3d center = mario.getEyePos().add(Vec3d.fromPolar(mario.getPitch(), mario.getYaw()).multiply(radius));
				Box airblastHitbox =  Box.of(center, radius, radius, radius);

				// If Mario's on the ground, ensure that the reflection hitbox down extends to the floor. This way Mario
				// can easily scoop up a whole bunch of arrows and stuff off the floor & fling 'em forward, which seems
				// like it'd be fun
				if(mario.isOnGround())
					airblastHitbox = airblastHitbox.withMinY(Math.min(airblastHitbox.minY, mario.getY()));

				MarioQuaMario.LOGGER.info("Airblast!\n\tCenter: {}\n\tWidth: {}\n\tmin Y: {}", center, airblastHitbox.getLengthX(), airblastHitbox.minY);

				List<Entity> reflectTargets = mario.getWorld().getEntitiesByClass(Entity.class, airblastHitbox,
						entity -> !entity.isSpectator() && !entity.equals(mario));
				for(Entity reflectTarget : reflectTargets) {
					MarioQuaMario.LOGGER.info("Trying to reflect {}", reflectTarget);
					tryReflect(reflectTarget, mario, true);
				}
			}
		}
	}

	@Override
	public void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {
		CameraAnimationSet tailWhipCameraAnimation = makeTailWhipCameraAnimationSet();
		builder.add(
				new TailAttack(TailSpinGround.ID, null) {
					@Override protected boolean testSwing(CfaReadableMotionData data) {
						return data.getPlayer().isOnGround() && data.getPlayer().isInSneakingPose();
					}
				},
				new TailAttack(TailSpinFall.ID, null) {
					@Override protected boolean testSwing(CfaReadableMotionData data) {
						return !data.getPlayer().isOnGround() && data.getPlayer().isInSneakingPose();
					}
				},
				new TailAttack(null, tailWhipCameraAnimation) {
					@Override protected boolean testSwing(CfaReadableMotionData data) {
						return true;
					}
				},
				new PreventAttack() {
					@Override
					public boolean shouldInterceptAttack(CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isEmpty() && blockHitResult == null;
					}
				}
		);
	}

	private static CameraAnimation makeTailWhipCameraAnimation(int addend, Easing easing) {
		return new CameraAnimation(
				new CameraProgressHandler(1, (data, ticksPassed) ->
						Math.min(ticksPassed / (TAIL_WHIP_ANIMATION_DURATION + addend + 0.5F), 1)
				),
				(data, arrangement, progress) ->
						arrangement.yaw += easing.ease(progress % 1) * -360
		);
	}
	private static @NotNull CameraAnimationSet makeTailWhipCameraAnimationSet() {
		return new CameraAnimationSet(
				MarioQuaMario.CONFIG::getTailWhipCameraAnim,
				makeTailWhipCameraAnimation(2, Easing.mix(Easing.SINE_IN_OUT, Easing.QUART_IN_OUT)),
				makeTailWhipCameraAnimation(-3, Easing.EXPO_IN_OUT),
				null
		);
	}

	private static final int TAIL_WHIP_ANIMATION_DURATION = 7;

	private static float calculateProgress(float animationTime) {
		return Easing.SINE_IN_OUT.ease(Math.min(animationTime / TAIL_WHIP_ANIMATION_DURATION, 1));
	}
	public static final AnimationDefinition TAIL_WHIP_ANIMATION = AnimationDefinition.of(
			AnimationFlag.NO_SWING_LIMBS,
			(arrangement, data, animationTime, helper) -> arrangement.yaw = calculateProgress(animationTime) * 360,
			(posture, data, animationTime, helper) -> {
				float progress = calculateProgress(animationTime);
				float factor = progress < 0.5 ? progress * 2 : progress * -2 + 2;

				posture.HEAD.addPos(0, factor * 4.3F, factor * -1);

				posture.TORSO.addPos(0, factor * 4.3F, factor * -1);
				posture.TORSO.pitch += factor * 42;

				helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
					arrangement.addPos(0, factor * 4, factor * 4);
					arrangement.pitch -= MathHelper.lerp(factor, 8, 90);
				});

				helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
					arrangement.addPos(0, factor * 2, factor * 6);
					arrangement.pitch -= factor * 39;
				});

				if(posture.TAIL != null) {
					posture.TAIL.setAngles(
							helper.interpolateKeyframes(progress * 2, posture.TAIL.pitch, -posture.TORSO.pitch - MathHelper.clamp(data.getPlayer().getPitch() - 30, -80, 75), posture.TAIL.pitch),
							helper.interpolateKeyframes(progress * 2, 0, 85, 0),
							0
					);
				}
			}
	);

}

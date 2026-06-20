package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Posture;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Debug implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("debug");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	public static void tPose(Posture posture) {
		posture.RIGHT_ARM.roll = 90;
		posture.LEFT_ARM.roll = -90;
	}

	@Override
	public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(posture, data, animationTime, helper) -> {
					tPose(posture);
				}
		);
	}

	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}

	@Override public @NotNull GenericActionType getGenericActionType() {
		return GenericActionType.UNSPECIFIED;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		data.getPlayer().setHealth(20);
	}
	@Override public boolean travelHook(CfaTravelData data) {
		ActionTimerVars.get(data).actionTimer++;
		data.setForwardStrafeVel(data.getInputs().getForwardInput() * 0.5, data.getInputs().getStrafeInput() * 0.5);
		data.setYVel(data.getInputs().JUMP.isHeld() ? 0.4 : (data.getInputs().DUCK.isHeld() ? -0.4 : (0.03 * Math.sin(ActionTimerVars.get(data).actionTimer++ / 16.0))));
		return true;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, CastableHelper helper) {
		builder.add(
				new TransitionDefinition(
						DebugSprint.ID,
						data -> data.getPlayer().isSprinting(), EvaluatorEnvironment.COMMON,
						null,
						(data, isSelf, seed) -> data.playSound(MarioSFX.FIREBALL, seed)
				),
				new TransitionDefinition(
						LavaBoost.ID,
						data -> false,
						EvaluatorEnvironment.SERVER_ONLY,
						data -> {
							Vec3d lavaBoostEjectionPos = LavaBoost.findLavaBoostEjectionSpot(data);
							if(lavaBoostEjectionPos == null)
								MarioQuaMario.LOGGER.error("Triggered Lava Boost transition, but then couldn't find" +
										" the ejection pos?! Player is at {}", data.getPlayer().getPos());
							else
								data.goTo(lavaBoostEjectionPos);

							data.setYVel(LavaBoost.BOOST_VEL.get(data));
							data.setForwardStrafeVel(0, 0);
						},
						(data, isSelf, seed) -> {
							data.voice(Voicelines.BURNT, seed);
						}
				)
		);
	}

	@Override
	public void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {
		builder.add(
				new AttackInterceptionDefinition() {
					@Override
					public @Nullable Identifier defineActionTarget() {
						return null;
					}

					@Override
					public @Nullable Hand defineHandToSwing() {
						return null;
					}

					@Override
					public boolean triggersAttackCooldown() {
						return false;
					}

					@Override
					public boolean shouldInterceptAttack(CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress, @Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult) {
						return weapon.isOf(Items.WOODEN_AXE);
					}

					@Override
					public @NotNull MiningHandling shouldSuppressMining(CfaReadableMotionData data, ItemStack weapon, @NotNull BlockHitResult blockHitResult, int miningTicks) {
						return MiningHandling.INTERCEPT;
					}

					@Override
					public void executeTravellers(CfaTravelData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {



//						data.getPlayer().setYaw(data.getPlayer().getYaw() + 90);
//						data.getPlayer().bodyYaw = 20;
//						data.getPlayer().prevHeadYaw = -30;
//						data.getPlayer().prevBodyYaw = 20;
//						data.getPlayer().prevYaw = 0;
					}

					@Override
					public void executeClients(CfaClientData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed) {
//						data.forceBodyAlignment(true);
//						data.instantVisualRotate(90, true);
					}

					@Override
					public void executeServer(CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress, ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

					}
				},
				new AttackInterceptionDefinition() {
					@Override public @Nullable Identifier defineActionTarget() {
						return null;
					}
					@Override public Hand defineHandToSwing() {
						return Hand.OFF_HAND;
					}
					@Override public boolean triggersAttackCooldown() {
						return true;
					}

					@Override public boolean shouldInterceptAttack(
							CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
					) {
						return weapon.isEmpty();
					}

					@Override public @NotNull MiningHandling shouldSuppressMining(
							CfaReadableMotionData data, ItemStack weapon,
							@NotNull BlockHitResult blockHitResult, int miningTicks
					) {
						return miningTicks < 20 ? MiningHandling.INTERCEPT : MiningHandling.MINE;
					}

					@Override public void executeTravellers(
							CfaTravelData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
					@Override public void executeClients(
							CfaClientData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget,
							long seed
					) {
						data.playSound(MarioSFX.HEAVY, seed);
					}

					@Override public void executeServer(
							CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
							ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
				}
		);
	}
}

package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
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

import java.util.List;
import java.util.Set;

public class Debug implements GenericActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("debug");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static final PlayermodelAnimation T_POSE = new PlayermodelAnimation(
		null,
		new ProgressHandler(null, (data, prevAnimationID) -> true, (data, ticksPassed) -> ticksPassed / 25F),
		null,

		null,
		null,

		new LimbAnimation(false, (data, arrangement, progress) -> arrangement.roll += 90),
		new LimbAnimation(false, (data, arrangement, progress) -> arrangement.roll -= 90),

		new LimbAnimation(false, null),
		new LimbAnimation(false, null),

		new LimbAnimation(false, null)
	);
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return T_POSE;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}
	@Override public @NotNull GenericActionType getGenericActionType() {
		return GenericActionType.UNSPECIFIED;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

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

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
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
				),
				DebugSideTurn.SIDE_TURN
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions() {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions() {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {

		return List.of(
				new AttackInterceptionDefinition() {
					@Override
					public @Nullable Identifier getActionTarget() {
						return null;
					}

					@Override
					public @Nullable Hand getHandToSwing() {
						return null;
					}

					@Override
					public boolean shouldTriggerAttackCooldown() {
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

						data.getPlayer().setYaw(data.getPlayer().getYaw() + 90);
					}

					@Override
					public void executeClients(CfaClientData data, ItemStack weapon, float attackCooldownProgress, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed) {
						data.forceBodyAlignment(true);
						data.instantVisualRotate(90, true);
						data.playAnimation(DebugSideTurn.ANIMATION, -1);
					}

					@Override
					public void executeServer(CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress, ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget) {

					}
				},
				new AttackInterceptionDefinition() {
					@Override public @Nullable Identifier getActionTarget() {
						return null;
					}
					@Override public Hand getHandToSwing() {
						return Hand.OFF_HAND;
					}
					@Override public boolean shouldTriggerAttackCooldown() {
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

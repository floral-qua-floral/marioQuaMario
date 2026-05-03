package com.fqf.mario_qua_mario_content.actions.generic;

import com.fqf.charapoweract_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charapoweract_api.cpadata.*;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSprint extends Debug {
	public static final Identifier ID = MarioQuaMarioContent.makeID("debug_test");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public boolean travelHook(ICPATravelData data) {
		data.setStrafeVel(data.getInputs().getStrafeInput() * 0.5);

		double pitchRadians = Math.toRadians(data.getPlayer().getPitch());
		data.setForwardVel(data.getInputs().getForwardInput() * Math.cos(pitchRadians));
		data.setYVel(data.getInputs().getForwardInput() * -Math.sin(pitchRadians));
		return true;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions() {
		return List.of(
				new TransitionDefinition(
						Debug.ID,
						data -> !data.getPlayer().isSprinting(), EvaluatorEnvironment.SERVER_ONLY,
						null,
						(data, isSelf, seed) -> data.playSound(MarioContentSFX.DUCK, seed)
				)
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of(
				new AttackInterceptionDefinition() {
					@Override public @Nullable Identifier getActionTarget() {
						return null;
					}
					@Override public Hand getHandToSwing() {
						return Hand.MAIN_HAND;
					}
					@Override public boolean shouldTriggerAttackCooldown() {
						return true;
					}

					@Override public boolean shouldInterceptAttack(
							ICPAReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
					) {
						return weapon.isEmpty();
					}

					@Override public @NotNull MiningHandling shouldSuppressMining(
							ICPAReadableMotionData data, ItemStack weapon,
							@NotNull BlockHitResult blockHitResult, int miningTicks
					) {
						return miningTicks < 20 ? MiningHandling.HOLD : MiningHandling.MINE;
					}

					@Override public void executeTravellers(
							ICPATravelData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
					@Override public void executeClients(
							ICPAClientData data, ItemStack weapon, float attackCooldownProgress,
							@Nullable BlockPos blockTarget, @Nullable Entity entityTarget,
							long seed
					) {
						data.playSound(MarioContentSFX.YOSHI, seed);
					}

					@Override public void executeServer(
							ICPAAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
							ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
					) {

					}
				}
		);
	}
}

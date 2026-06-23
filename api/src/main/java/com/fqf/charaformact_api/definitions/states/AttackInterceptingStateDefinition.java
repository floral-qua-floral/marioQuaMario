package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.google.common.collect.ImmutableList;
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

public interface AttackInterceptingStateDefinition extends CfaStateDefinition {
	default void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {

	}

	interface AttackInterceptionDefinition {
		/**
		 * If null, performing the Attack Interception will have no effect whatsoever on the player's Action. Otherwise,
		 * the player will be put in the Action defined here. Standard Action->Action transitions will not occur.
		 */
		default @Nullable Identifier defineActionTarget() {
			return null;
		}

		@Nullable Hand defineHandToSwing();
		boolean triggersAttackCooldown();

		/**
		 * This method is only ever called on the main client. As such, a compromised client could request the Attack
		 * Interception without meeting the conditions defined here. However, they cannot perform Attack Interceptions
		 * belonging to a Form or an Action that they are not in. Please keep this in mind in your design.
		 * @param data The main client's CfaData.
		 * @param weapon The item currently held in the player's Weapon Stack, typically the Main Hand.
		 * @param attackCooldownProgress A value from 0 to 1 representing how full the Attack Cooldown bar is.
		 * @param entityHitResult The entity being targeted, if any.
		 * @param blockHitResult The block being targeted, if any. Only one of entityHitResult or blockHitResult may be
		 *                       non-null at a time; you'll never receive both. That said, it is possible for both to be
		 *                       null if the player is completely whiffing an attack.
		 * @return True to start the Attack Interception logic, false to perform a vanilla attack. Even if you return
		 * true, if the player is targeting a block, they'll still have to go through shouldSuppressMining.
		 */
		boolean shouldInterceptAttack(
				CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
		);

		/**
		 * This method is also only ever called on the main client.
		 * @param data The main client's CfaData.
		 * @param weapon The player's Weapon Stack.
		 * @param blockHitResult The current target of the crosshair. This provides its position and also the face it's
		 *                       being mined on.
		 * @param miningTicks The number of ticks for which the player has been attempting to mine.
		 * @return See MiningHandling enum's documentation.
		 */
		@NotNull MiningHandling shouldSuppressMining(
				CfaReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		);

		/**
		 * <code>executeTravellers</code>: Runs on the client that is performing the transition, and on the server.
		 * <code>executeClients</code>: Runs on all clients, including the one performing the transition.
		 * <code>executeServer</code>: Runs on the server side only.
		 */
		default void executeTravellers(
				CfaTravelData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
		default void executeClients(
				CfaClientData data, ItemStack weapon, float attackCooldownProgress,
				@Nullable BlockPos blockTarget, @Nullable Entity entityTarget, long seed
		) {

		}
		default void executeServer(
				CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}

		/**
		 * Use these to decide what happens when the player presses/holds Attack while targeting a block.
		 * <ul>
		 *     <li><code>MINE</code>: Player mines the block without executing the interception. As soon as they've
		 *     started mining, they will NO LONGER call shouldSuppressMining at all until they've let go of the button and
		 *     pressed it again. Attack Interceptions cannot be used to interrupt mining partway through.</li>
		 *     <li><code>INTERCEPT</code>: Performs the Attack Interception without mining. If they keep holding the Attack
		 *     button, they'll continue calling shouldSuppressMining every tick. As long as you keep returning INTERCEPT,
		 *     they'll do nothing at all. Start returning MINE instead after some number of ticks to allow them to perform
		 *     the interception, then start mining if they hold the button.</li>
		 *     <li><code>HOLD</code>: Does not perform the Attack Interception and also does not mine the block. If the
		 *     player keeps holding the Attack button, they'll continue calling shouldSuppressMining every tick. If they
		 *     let go of the Attack button, they'll immediately perform the Attack Interception. You can prevent this by
		 *     returning MINE after a certain number of ticks, to create an either-or behavior - if the player presses and
		 *     releases Attack quickly, they'll perform the Interception, but if they hold the button down, they'll start
		 *     mining and never perform the Interception. If you want to use this, it's recommended that you return HOLD
		 *     for the first 3 or so ticks, then start returning MINE. This handling is especially recommended for Attack
		 *     Interceptions that may be disruptive, so that the player can choose to mine without triggering them.</li>
		 * </ul>
		 */
		enum MiningHandling {
			MINE, // Player mines the block without executing the interception
			INTERCEPT, // Attack interception occurs immediately and mining is prevented
			HOLD // Attack interception does not trigger, nor does the player start mining
		}
	}

	/**
	 * This class is provided for your convenience. Feel free to extend it in order to make Attack Interceptions which
	 * can conditionally prevent attacks and/or mining.
	 */
	abstract class PreventAttack implements AttackInterceptionDefinition {
		@Override public @Nullable Identifier defineActionTarget() {
			return null;
		}
		@Override public Hand defineHandToSwing() {
			return null;
		}
		@Override public boolean triggersAttackCooldown() {
			return false;
		}

		@Override public @NotNull MiningHandling shouldSuppressMining(
				CfaReadableMotionData data, ItemStack weapon,
				@NotNull BlockHitResult blockHitResult, int miningTicks
		) {
			return this.shouldInterceptAttack(data, weapon, 1F, null, blockHitResult)
					? MiningHandling.INTERCEPT
					: MiningHandling.MINE;
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

		}
		@Override public void executeServer(
				CfaAuthoritativeData data, ItemStack weapon, float attackCooldownProgress,
				ServerWorld world, @Nullable BlockPos blockTarget, @Nullable Entity entityTarget
		) {

		}
	}
}

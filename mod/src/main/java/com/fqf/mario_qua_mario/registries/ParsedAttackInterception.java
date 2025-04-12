package com.fqf.mario_qua_mario.registries;

import com.fqf.mario_qua_mario_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.packets.MarioAttackInterceptionPackets;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParsedAttackInterception {
	public static ParsedAttackInterception getInterception(MarioAttackInterceptionPackets.AttackInterceptionPayload payload) {
		return payload.isFromAction() ? ParsedActionHelper.get(payload.interceptionSource()).INTERCEPTIONS.get(payload.interceptionIndex())
				: RegistryManager.POWER_UPS.getOrThrow(payload.interceptionSource()).INTERCEPTIONS.get(payload.interceptionIndex());
	}
	public static float getAttackCooldownProgress(PlayerEntity mario) {
		return mario.getAttackCooldownProgress(0.5F);
	}

	private final AttackInterceptingStateDefinition.AttackInterceptionDefinition DEFINITION;
	public final boolean IS_FROM_ACTION;

	public final @Nullable AbstractParsedAction ACTION_TARGET;
	public final @Nullable Hand HAND_TO_SWING;
	public final boolean TRIGGERS_ATTACK_COOLDOWN;

	public ParsedAttackInterception(AttackInterceptingStateDefinition.AttackInterceptionDefinition definition, boolean isFromAction) {
		this.DEFINITION = definition;
		this.IS_FROM_ACTION = isFromAction;

		Identifier actionTargetID = definition.getActionTarget();
		this.ACTION_TARGET = actionTargetID == null ? null : RegistryManager.ACTIONS.get(actionTargetID);
		this.HAND_TO_SWING = definition.getHandToSwing();
		this.TRIGGERS_ATTACK_COOLDOWN = definition.shouldTriggerAttackCooldown();
	}

	public boolean shouldInterceptAttack(
			IMarioReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
			@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
	) {
		return this.DEFINITION.shouldInterceptAttack(data, weapon, attackCooldownProgress, entityHitResult, blockHitResult);
	}
	public @NotNull AttackInterceptingStateDefinition.MiningHandling shouldSuppressMining(
			IMarioReadableMotionData data, ItemStack weapon, BlockHitResult blockHitResult, int miningTicks
	) {
		return this.DEFINITION.shouldSuppressMining(data, weapon, blockHitResult, miningTicks);
	}

	public void execute(
			MarioPlayerData data,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		ItemStack weapon = data.getMario().getWeaponStack();
		float cooldownProgress = getAttackCooldownProgress(data.getMario());

		if(data instanceof IMarioClientData clientData)
			this.DEFINITION.executeClients(clientData, weapon, cooldownProgress, targetBlock, targetEntity, seed);
		if(data instanceof MarioMoveableData moveableData)
			this.DEFINITION.executeTravellers(moveableData, weapon, cooldownProgress, targetBlock, targetEntity);
		if(data instanceof IMarioAuthoritativeData authoritativeData)
			this.DEFINITION.executeServer(authoritativeData, weapon, cooldownProgress, authoritativeData.getMario().getServerWorld(), targetBlock, targetEntity);

		if(this.ACTION_TARGET != null) data.setActionTransitionless(this.ACTION_TARGET);
		if(data.isClient() && this.HAND_TO_SWING != null) data.getMario().swingHand(this.HAND_TO_SWING, false);
		if(this.TRIGGERS_ATTACK_COOLDOWN) data.getMario().resetLastAttackedTicks();
	}
}

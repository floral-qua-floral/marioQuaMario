package com.fqf.charaformact.registries;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.definitions.states.AttackInterceptingStateDefinition;
import com.fqf.charaformact.cfadata.*;
import com.fqf.charaformact.packets.AttackInterceptionPackets;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
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
	public static ParsedAttackInterception getInterception(AttackInterceptionPackets.AttackInterceptionPayload payload) {
		return payload.isFromAction() ? ParsedActionHelper.get(payload.interceptionSource()).INTERCEPTIONS.get(payload.interceptionIndex())
				: RegistryManager.FORMS.getOrThrow(payload.interceptionSource()).INTERCEPTIONS.get(payload.interceptionIndex());
	}
	public static float getAttackCooldownProgress(PlayerEntity player) {
		return player.getAttackCooldownProgress(0.5F);
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
			CfaReadableMotionData data, ItemStack weapon, float attackCooldownProgress,
			@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult
	) {
		return this.DEFINITION.shouldInterceptAttack(data, weapon, attackCooldownProgress, entityHitResult, blockHitResult);
	}
	public @NotNull AttackInterceptingStateDefinition.MiningHandling shouldSuppressMining(
			CfaReadableMotionData data, ItemStack weapon, BlockHitResult blockHitResult, int miningTicks
	) {
		return this.DEFINITION.shouldSuppressMining(data, weapon, blockHitResult, miningTicks);
	}

	public void execute(
			CfaPlayerData data,
			@Nullable Entity targetEntity, @Nullable BlockPos targetBlock, long seed
	) {
		ItemStack weapon = data.getPlayer().getWeaponStack();
		float cooldownProgress = getAttackCooldownProgress(data.getPlayer());

		if(data instanceof CfaClientData clientData)
			this.DEFINITION.executeClients(clientData, weapon, cooldownProgress, targetBlock, targetEntity, seed);
		if(data instanceof CfaMoveableData moveableData)
			this.DEFINITION.executeTravellers(moveableData, weapon, cooldownProgress, targetBlock, targetEntity);
		if(data instanceof CfaAuthoritativeData authoritativeData)
			this.DEFINITION.executeServer(authoritativeData, weapon, cooldownProgress, authoritativeData.getPlayer().getServerWorld(), targetBlock, targetEntity);

		if(this.ACTION_TARGET != null) data.setActionTransitionless(this.ACTION_TARGET);
		if(data.isClient() && this.HAND_TO_SWING != null) data.getPlayer().swingHand(this.HAND_TO_SWING, false);
		if(this.TRIGGERS_ATTACK_COOLDOWN) data.getPlayer().resetLastAttackedTicks();
	}
}

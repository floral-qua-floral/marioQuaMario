package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.cpadata.CPAMainClientData;
import com.fqf.charapoweract.packets.CPAClientPacketHelper;
import com.fqf.charapoweract_api.definitions.states.AttackInterceptingStateDefinition.MiningHandling;
import com.fqf.charapoweract.registries.ParsedAttackInterception;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow @Nullable public ClientPlayerEntity player;
	@Shadow @Nullable public HitResult crosshairTarget;
	@Shadow @Final public GameOptions options;

	@Shadow protected abstract void handleBlockBreaking(boolean breaking);
	@Shadow protected abstract boolean doAttack();

	@Inject(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/HitResult;getType()Lnet/minecraft/util/hit/HitResult$Type;"), cancellable = true)
	private void doAttackInterception(CallbackInfoReturnable<Boolean> cir) {
		assert this.player != null && this.crosshairTarget != null;

		if(this.heldInterception != null || !this.player.cpa$getCPAData().isEnabled()) return;

		if(this.crosshairTarget.getType() == HitResult.Type.BLOCK) {
			if(this.attemptMiningAttackInterceptions(this.player.cpa$getCPAData()))
				cir.setReturnValue(false);
		}
		else {
			if(this.attemptAttackInterceptions(this.player.cpa$getCPAData()))
				cir.setReturnValue(false);
		}
	}

	@Unique
	private boolean attemptAttackInterceptions(CPAMainClientData data) {
		assert this.player != null && this.crosshairTarget != null;
		float attackCooldownProgress = ParsedAttackInterception.getAttackCooldownProgress(this.player);
		ItemStack weapon = this.player.getWeaponStack();
		EntityHitResult entityTarget = this.crosshairTarget.getType() == HitResult.Type.ENTITY ? (EntityHitResult) this.crosshairTarget : null;

		for (ParsedAttackInterception interception : data.getAction().INTERCEPTIONS)
			if(this.shouldInterceptAttack(data, attackCooldownProgress, weapon, entityTarget, interception))
				return true;

		for (ParsedAttackInterception interception : data.getPowerForm().INTERCEPTIONS)
			if(this.shouldInterceptAttack(data, attackCooldownProgress, weapon, entityTarget, interception))
				return true;

		return false;
	}

	@Unique
	private boolean shouldInterceptAttack(
			CPAMainClientData data,
			float attackCooldownProgress, ItemStack weapon, @Nullable EntityHitResult entityHitResult,
			ParsedAttackInterception interception
	) {
		if(interception.shouldInterceptAttack(data, weapon, attackCooldownProgress, entityHitResult, null)) {
			this.executeAndNetworkInterception(data, attackCooldownProgress, weapon, entityHitResult, null, interception);
			this.miningTicks = 1; // Prevents double-interception when punching air & then moving crosshair to block
			this.executeHeldInterception = false; // Prevents double-interception from HOLD mining interceptions that didn't end properly
			return true;
		}
		return false;
	}

	@Unique
	private boolean attemptMiningAttackInterceptions(CPAMainClientData data) {
		assert this.player != null && this.crosshairTarget != null;
		float attackCooldownProgress = ParsedAttackInterception.getAttackCooldownProgress(this.player);
		ItemStack weapon = this.player.getMainHandStack();

		for (ParsedAttackInterception interception : data.getAction().INTERCEPTIONS) {
			MiningHandling handling = this.shouldInterceptMiningAttack(
					data, attackCooldownProgress, weapon, (BlockHitResult) this.crosshairTarget, interception);
			if(handling != null)
				return handling != MiningHandling.MINE;
		}

		for (ParsedAttackInterception interception : data.getPowerForm().INTERCEPTIONS) {
			MiningHandling handling = this.shouldInterceptMiningAttack(
					data, attackCooldownProgress, weapon, (BlockHitResult) this.crosshairTarget, interception);
			if(handling != null)
				return handling != MiningHandling.MINE;
		}

		return false;
	}

	@Unique
	private MiningHandling shouldInterceptMiningAttack(
			CPAMainClientData data,
			float attackCooldownProgress, ItemStack weapon, @NotNull BlockHitResult blockHitResult,
			ParsedAttackInterception interception
	) {
		if(interception.shouldInterceptAttack(data, weapon, attackCooldownProgress, null, blockHitResult)) {
			MiningHandling handling = interception.shouldSuppressMining(data, weapon, blockHitResult, 0);
			switch(handling) { // switch-case fallthrough is my dear friend and I am happy to get to use it here!!! <3
				case INTERCEPT:
					this.heldInterception = interception;
					this.executeHeldInterception = false;
					this.miningTicks = 0;
				case MINE:
					this.executeAndNetworkInterception(data, attackCooldownProgress, weapon, null, blockHitResult, interception);
					break;
				case HOLD:
					this.heldInterception = interception;
					this.executeHeldInterception = true;
					this.miningTicks = 0;
			}
			return handling;
		}

		return null;
	}

	@Unique
	private void executeAndNetworkInterception(
			CPAMainClientData data,
			float attackCooldownProgress, ItemStack weapon,
			@Nullable EntityHitResult entityHitResult, @Nullable BlockHitResult blockHitResult,
			ParsedAttackInterception interception
	) {
		assert this.player != null;
		long seed = this.player.getRandom().nextLong();
		Entity entityTarget = entityHitResult == null ? null : entityHitResult.getEntity();
		BlockPos blockTarget = blockHitResult == null ? null : blockHitResult.getBlockPos();
		interception.execute(data, entityTarget, blockTarget, seed);
		CPAClientPacketHelper.attackInterceptionC2S(data, interception, entityTarget, blockTarget, seed);
	}

	@Unique private @Nullable ParsedAttackInterception heldInterception;
	@Unique private boolean executeHeldInterception = false;
	@Unique private int miningTicks;

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"))
	private void resetMiningTicksAndTriggerHeldInterception(boolean breaking, CallbackInfo ci) {
		assert this.player != null;
		if(!this.player.cpa$getCPAData().isEnabled()) return;
		if(!breaking && !this.options.attackKey.isPressed()) {
			this.miningTicks = 0;
			if(this.heldInterception != null) {
				if(this.executeHeldInterception) {
					BlockHitResult blockHitResult;

					assert this.crosshairTarget != null;
					if(this.crosshairTarget.getType() == HitResult.Type.BLOCK) blockHitResult = (BlockHitResult) this.crosshairTarget;
					else blockHitResult = null;

					this.executeAndNetworkInterception(
							this.player.cpa$getCPAData(),
							ParsedAttackInterception.getAttackCooldownProgress(this.player), this.player.getWeaponStack(),
							null, blockHitResult,
							this.heldInterception
					);
				}

				this.heldInterception = null;
				this.executeHeldInterception = false;
			}
		}
		else this.miningTicks++;
	}

	@Inject(method = "handleBlockBreaking", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;updateBlockBreakingProgress(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)Z"), cancellable = true)
	private void doMiningInterception(boolean breaking, CallbackInfo ci) {
		assert this.player != null && this.crosshairTarget != null;
		if (breaking && this.heldInterception != null) {
//			if (this.attemptMiningInterceptions(this.player.cpa$getCPAData(), false)) {
			float attackCooldownProgress = ParsedAttackInterception.getAttackCooldownProgress(this.player);
			ItemStack weapon = this.player.getWeaponStack();
			BlockHitResult blockHitResult = (BlockHitResult) this.crosshairTarget;
			if (this.shouldSuppressMining(this.player.cpa$getCPAData(), weapon, blockHitResult, this.heldInterception)) {
				this.handleBlockBreaking(false);
				ci.cancel();
			}
			else {
				if(this.crosshairTarget.getType() == HitResult.Type.BLOCK) this.doAttack(); // Reset mining progress
				this.heldInterception = null;
				this.executeHeldInterception = false;
			}
		}
	}

	@Unique
	private boolean shouldSuppressMining(
			CPAMainClientData data, ItemStack weapon,
			BlockHitResult blockHitResult, ParsedAttackInterception interception
	) {
		return switch(interception.shouldSuppressMining(data, weapon, blockHitResult, this.miningTicks)) {
			case MINE -> false;
			case HOLD, INTERCEPT -> true;
		};
	}
}

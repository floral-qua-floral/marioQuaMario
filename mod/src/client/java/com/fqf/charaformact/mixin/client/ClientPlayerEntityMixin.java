package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.CfaMainClientData;
import com.fqf.charaformact.cfadata.injections.AdvCfaMainClientDataHolder;
import com.fqf.charaformact.util.CfaPositionSettable;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements AdvCfaMainClientDataHolder, CfaPositionSettable {
	@Shadow @Final protected MinecraftClient client;

	@Shadow public Input input;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError("Mixin constructor?!");
	}

	@Inject(method = "canSprint", at = @At("HEAD"), cancellable = true)
	private void preventSprinting(CallbackInfoReturnable<Boolean> cir) {
		CfaMainClientData data = this.cfa$getCfaData();
		if(data.doCustomTravel(false)) {
			if(
					data.getAction().FLAGS.contains(ActionFlag.PROHIBIT_SPRINT)
					|| (
							data.getAction().FLAGS.contains(ActionFlag.RESTRICT_SPRINT)
							&& !this.isSprinting()
					)
			)
				cir.setReturnValue(false);
		}
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V"))
	private void tickMovementClinger(CallbackInfo ci) {
		this.cfa$getCfaData().tickInputs();
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
	private boolean allowSprintingInPartialWater(ClientPlayerEntity instance, Operation<Boolean> original) {
		return !instance.cfa$getCfaData().isEnabled() && original.call(instance);
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private boolean moveFastWithItem(ClientPlayerEntity instance, Operation<Boolean> original) {
		return (!this.cfa$getCfaData().doCustomTravel(false) || instance.isOnGround()) && original.call(instance);
	}

	@Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
	private void preventSlowDown(CallbackInfoReturnable<Boolean> cir) {
		if(this.cfa$getCfaData().doCustomTravel(false)) cir.setReturnValue(false);
	}

	@Inject(method = "isInSneakingPose", at = @At("HEAD"), cancellable = true)
	private void preventSneakPose(CallbackInfoReturnable<Boolean> cir) {
		if(!this.cfa$getCfaData().isEnabled()) return;
		cir.setReturnValue(this.cfa$getCfaData().getAction().FLAGS.contains(ActionFlag.CROUCHING));
	}

	@Override
	public void cfa$setPos(Vec3d pos) {
		// will this fix it please\
		this.move(MovementType.SELF, pos.subtract(this.getPos()));
//		this.lastX = pos.x;
//		this.lastBaseY = pos.y;
//		this.lastZ = pos.z;
	}

	@Inject(method = "sendMovementPackets", at = @At("RETURN"))
	private void sendHeldTransitionPackets(CallbackInfo ci) {
		List<CustomPayload> heldPackets = this.cfa$getCfaData().HELD_TRANSITION_PACKETS;
		for(CustomPayload heldTransitionPacket : heldPackets) {
			ClientPlayNetworking.send(heldTransitionPacket);
		}
		heldPackets.clear();
	}

	@WrapOperation(
			method = "tickMovement",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z",
					opcode = Opcodes.GETFIELD
			)
	)
	private boolean preventFlying(PlayerAbilities instance, Operation<Boolean> original) {
		boolean originalValue = original.call(instance);
		assert this.client.interactionManager != null;
		if(!originalValue || instance.flying || this.client.interactionManager.isFlyingLocked()) return originalValue;
		CfaMainClientData data = this.cfa$getCfaData();
		return !data.isEnabled() || !data.getAction().FLAGS.contains(ActionFlag.PROHIBIT_FLIGHT);
	}

	@WrapOperation(
			method = "tickMovement",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSwimming()Z"
			)
	)
	private boolean ignoreSwimmingStatusForMovement(ClientPlayerEntity instance, Operation<Boolean> original) {
		return original.call(instance) && !this.cfa$getCfaData().isEnabled();
	}

	@WrapOperation(
			method = "tickMovement",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/input/Input;hasForwardMovement()Z"
			),
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"
					),
					to = @At(
							value = "FIELD",
							opcode = Opcodes.GETFIELD,
							target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z"
					)
			)
	)
	private boolean sometimesAllowAnyDirectionalInput(Input instance, Operation<Boolean> original) {
		if(this.enabledAndHasFlag(ActionFlag.OMNI_DIRECTIONAL_SPRINT))
			return !MathHelper.approximatelyEquals(instance.movementForward, 0)
					|| !MathHelper.approximatelyEquals(instance.movementSideways, 0);
		else return original.call(instance);
	}

	@WrapMethod(method = "isWalking")
	private boolean sometimesAllowAnyDirectionalWalking(Operation<Boolean> original) {
		if(this.enabledAndHasFlag(ActionFlag.OMNI_DIRECTIONAL_SPRINT)) {
			if(this.isSubmergedInWater)
				return !MathHelper.approximatelyEquals(this.input.movementForward, 0)
						|| !MathHelper.approximatelyEquals(this.input.movementSideways, 0);
			else
				return Vector2d.lengthSquared(this.input.movementForward, this.input.movementSideways) >= 0.8 * 0.8;
		}
		else return original.call();
	}

	@WrapOperation(
			method = "tickMovement",
			at = @At(
					value = "FIELD",
					opcode = Opcodes.GETFIELD,
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;collidedSoftly:Z"
			),
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lnet/minecraft/client/network/ClientPlayerEntity;isSprinting()Z"
					),
					to = @At(
							value = "FIELD",
							opcode = Opcodes.GETFIELD,
							target = "Lnet/minecraft/entity/player/PlayerAbilities;allowFlying:Z"
					)
			)
	)
	private boolean allowSprintAgainstCollisions(ClientPlayerEntity instance, Operation<Boolean> original) {
		return original.call(instance) || this.enabledAndHasFlag(ActionFlag.ALLOW_SPRINT_AGAINST_WALL);
	}

	@Unique
	private boolean enabledAndHasFlag(ActionFlag flag) {
		CfaMainClientData data = this.cfa$getCfaData();
		return data.isEnabled() && data.getAction().FLAGS.contains(flag);
	}
}

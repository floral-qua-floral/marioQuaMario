package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(MarioDataManager.getMarioData(this) instanceof MarioClientData marioClientData
				&& marioClientData.useMarioPhysics() && marioClientData.travel(movementInput))
					ci.cancel();
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaJumpingSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(MarioDataManager.getMarioData(this).useMarioPhysics())
			cir.setReturnValue(false);
	}

	@Inject(at = @At("TAIL"), method = "getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;", cancellable = true)
	private void getBaseDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		MarioData data = MarioDataManager.getMarioData(this);
		if(data.isEnabled()) {
			// Returns the standing hitbox if being used by Mario on the client side while he can't sneak
			if(data.getSneakProhibited() && pose == EntityPose.CROUCHING) {
				cir.setReturnValue(data.getMario().getBaseDimensions(EntityPose.STANDING));
				return;
			}

			cir.setReturnValue(getModifiedDimensions(pose, cir, data));
		}
	}

	@Unique
	private static @NotNull EntityDimensions getModifiedDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir, MarioData data) {
		ParsedPowerUp powerUp = data.getPowerUp();
		ParsedCharacter character = data.getCharacter();

		float widthFactor = powerUp.WIDTH_FACTOR;
		float heightFactor = powerUp.HEIGHT_FACTOR;
		if(pose == EntityPose.CROUCHING) heightFactor *= 0.6F;

		EntityDimensions resultDimensions = cir.getReturnValue();

		return new EntityDimensions(
				resultDimensions.width() * widthFactor,
				resultDimensions.height() * heightFactor,
				resultDimensions.eyeHeight() * heightFactor,
				resultDimensions.attachments(), resultDimensions.fixed()
		);
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	public void slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		MarioData data = MarioDataManager.getMarioData(this);
		if(data.useMarioPhysics() && data.getAction().SNEAK_LEGALITY.slipOffLedges()) {
			cir.setReturnValue(false);
		}
	}
}

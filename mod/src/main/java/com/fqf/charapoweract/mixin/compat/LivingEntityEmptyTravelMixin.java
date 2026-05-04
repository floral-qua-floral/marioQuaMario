package com.fqf.charapoweract.mixin.compat;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LivingEntity.class)
public class LivingEntityEmptyTravelMixin {
//	@WrapMethod(method = "travel")
//	private void skipTravelIfPlayingAsCharacter(Vec3d movementInput, Operation<Void> original) {
//		Entity thisAsEntity = (Entity) (Object) this;
//		if(thisAsEntity instanceof PlayerEntity thisAsPlayer && thisAsPlayer.cpa$getCPAData().doCustomTravel()
//				&& !thisAsEntity.getWeaponStack().isOf(Items.FIREWORK_STAR)
//				&& !thisAsEntity.getWeaponStack().isOf(Items.FIRE_CHARGE)
//		) {
//			CharaPowerAct.LOGGER.info("Doing empty LivingEntity travel for {}", this);
//		}
//		else {
//			original.call(movementInput);
//		}
//	}

	// As can be seen from the commented out code above, I tried very hard to figure out how to make this method return
	// early for the player in a way that Sable's mixin would then hook onto so it could still trigger.
	// I tried this with an Inject and a WrapOperation, and tried both with the mixin priority set to 950 and 1050.
	// Nothing worked so instead I'm just doing this silly nonsense to skip straight to the pre-existing return.
	@WrapOperation(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;isLogicalSideForUpdatingMovement()Z"))
	private boolean gaslightAboutLogicalSideForSable(LivingEntity instance, Operation<Boolean> original, @Local(argsOnly = true) Vec3d movementInput) {
		return movementInput != null && original.call(instance);
	}
	@WrapWithCondition(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;updateLimbs(Z)V"))
	private boolean preventRedundantUpdateLimbsForSable(LivingEntity instance, boolean flutter, @Local(argsOnly = true) Vec3d movementInput) {
		return movementInput != null;
	}
}

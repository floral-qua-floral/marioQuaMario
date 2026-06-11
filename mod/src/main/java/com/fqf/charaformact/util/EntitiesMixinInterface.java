package com.fqf.charaformact.util;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.entity.Entity;

public interface EntitiesMixinInterface {
	IllegalStateException EXCEPTION = new IllegalStateException("Method wasn't overridden!");

	default boolean cfa$doLivingEntityTravel() {
		return true;
	}

	default boolean cfa$shouldStepOnBlock() {
		return true;
	}

	default float cfa$calculateNextStepSoundDistance(Operation<Float> original) {
		return original.call();
	}

	default void cfa$afterMounting(Entity mount) {

	}

	default boolean cfa$isInSneakingPose(boolean vanillaResult) {
		return vanillaResult;
	}

	default boolean cfa$canSetSwimming() {
		return true;
	}

	default float cfa$modifyBodyRotationForTurnHead(float bodyRotation) {
		return bodyRotation;
	}

	default void cfa$wrapPushAwayFrom(Entity entity, Operation<Void> original) {
		original.call(entity);
	}

	default void cfa$afterChangeLookDirection() {

	}
}

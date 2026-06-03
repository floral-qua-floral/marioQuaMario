package com.fqf.charaformact.mixin;

import com.fqf.charaformact.util.DelayedRemovable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityChangeListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Entity.class)
public class EntityDelayedRemovalMixin implements DelayedRemovable {
	@Shadow private EntityChangeListener changeListener;
	@Unique private boolean doingDelayedRemoval = false;
	@Unique private @Nullable Entity.RemovalReason storedRemovalReason = null;

	@Override
	public void cfa$startDelayedRemoval() {
		this.doingDelayedRemoval = true;
	}

	@WrapOperation(method = "setRemoved", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityChangeListener;remove(Lnet/minecraft/entity/Entity$RemovalReason;)V"))
	private void delayRemoval(EntityChangeListener instance, Entity.RemovalReason removalReason, Operation<Void> original) {
		if(this.doingDelayedRemoval) {
			this.storedRemovalReason = removalReason;
		}
		else original.call(instance, removalReason);
	}

	@Override
	public void cfa$finishDelayedRemoval() {
		this.doingDelayedRemoval = false;
		if(this.storedRemovalReason != null) this.changeListener.remove(this.storedRemovalReason);
	}
}

package com.floralquafloral.mixin;

import com.floralquafloral.StompableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityStompabilityMixin implements StompableEntity {
	@Shadow public abstract EntityType<?> getType();

	@Override public StompResult qua_mario$stomp(Identifier stompType, DamageSource damageSource, float amount) {
//		if(getType().isIn())

		return StompResult.NORMAL;
	}
}

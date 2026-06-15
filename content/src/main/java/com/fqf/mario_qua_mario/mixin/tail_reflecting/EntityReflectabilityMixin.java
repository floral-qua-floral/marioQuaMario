package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.ReflectableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityReflectabilityMixin implements ReflectableEntity {
	@Shadow public abstract EntityType<?> getType();

	@Override
	public boolean cfa$canReflect() {
		return this.getType().isIn(MQMTags.TAIL_ATTACK_REFLECTABLE);
	}
}

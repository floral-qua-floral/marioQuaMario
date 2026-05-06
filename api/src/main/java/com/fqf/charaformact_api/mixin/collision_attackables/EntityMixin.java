package com.fqf.charaformact_api.mixin.collision_attackables;

import com.fqf.charaformact_api.interfaces.CollisionAttackable;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements CollisionAttackable {

}

package com.fqf.mario_qua_mario_api.mixin;

import com.fqf.mario_qua_mario_api.interfaces.Stompable;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class EntityMixin implements Stompable {

}

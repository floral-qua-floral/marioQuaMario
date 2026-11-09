package com.fqf.mario_qua_mario.mixin.freezing.sizing;

import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public class BasicEncasementSizingMixin implements IceEncasementEntity.EncasementSizeProvider {

}

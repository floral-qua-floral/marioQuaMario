package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.mariodata.injections.MarioDataHolder;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMarioDataMixin implements MarioDataHolder {
}

package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioDataHolder;
import com.fqf.mario_qua_mario.mariodata.injections.IMarioDataHolder;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMarioDataMixin implements AdvMarioDataHolder, IMarioDataHolder {
	@Override public IMarioData mqm$getIMarioData() {
		return this.mqm$getMarioData();
	}
}

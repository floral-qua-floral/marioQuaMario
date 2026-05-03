package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract_api.mariodata.IMarioData;
import com.fqf.charapoweract.mariodata.injections.AdvMarioDataHolder;
import com.fqf.charapoweract_api.mariodata.injections.IMarioDataHolder;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMarioDataMixin implements AdvMarioDataHolder, IMarioDataHolder {
	@Override public IMarioData mqm$getIMarioData() {
		return this.mqm$getMarioData();
	}
}

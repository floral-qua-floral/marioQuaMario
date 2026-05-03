package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract_api.cpadata.ICPAData;
import com.fqf.charapoweract.mariodata.injections.AdvMarioDataHolder;
import com.fqf.charapoweract_api.cpadata.injections.ICPADataHolder;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMarioDataMixin implements AdvMarioDataHolder, ICPADataHolder {
	@Override public ICPAData cpa$getICPAData() {
		return this.mqm$getMarioData();
	}
}

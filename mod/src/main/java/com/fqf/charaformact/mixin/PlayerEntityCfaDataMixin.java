package com.fqf.charaformact.mixin;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact.cfadata.injections.AdvCfaDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaDataHolder;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityCfaDataMixin implements AdvCfaDataHolder, CfaDataHolder {

}

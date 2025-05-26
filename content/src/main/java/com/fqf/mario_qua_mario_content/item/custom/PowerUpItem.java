package com.fqf.mario_qua_mario_content.item.custom;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class PowerUpItem extends Item {
	private final Identifier POWER_UP_FORM_ID;
	private final int REPLACEMENT_VALUE_THRESHOLD;

	public PowerUpItem(Settings settings, Identifier powerUpForm, int replaceThreshold) {
		super(settings);
		this.POWER_UP_FORM_ID = powerUpForm;
		this.REPLACEMENT_VALUE_THRESHOLD = replaceThreshold;
	}

	@Override
	public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
		if(user instanceof ServerPlayerEntity serverPlayer) {
			IMarioAuthoritativeData data = serverPlayer.mqm$getIMarioAuthoritativeData();
			if(data.isEnabled() && this.REPLACEMENT_VALUE_THRESHOLD >= data.getPowerUpValue() && !data.getPowerUpID().equals(this.POWER_UP_FORM_ID))
				data.empowerTo(this.POWER_UP_FORM_ID);
		}
		return super.finishUsing(stack, world, user);
	}
}

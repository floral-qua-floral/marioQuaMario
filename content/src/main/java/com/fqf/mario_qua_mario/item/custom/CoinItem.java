package com.fqf.mario_qua_mario.item.custom;

import com.fqf.mario_qua_mario.item.ModItems;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.stat.Stats;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class CoinItem extends Item {
	public CoinItem(Settings settings) {
		super(settings);
	}

	public static final List<Pair<Item, Integer>> COIN_REWARDS = List.of(
			new Pair<>(ModItems.SUPER_MUSHROOM, 3),
			new Pair<>(ModItems.FIRE_FLOWER, 1),
			new Pair<>(ModItems.SUPER_LEAF, 1)
	);

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if(!user.isInCreativeMode() && stack.getCount() < 8) return TypedActionResult.fail(stack);

		user.playSound(MarioContentSFX.COIN, 1.0F, 1.0F);

		if(!world.isClient()) {
			Pair<Item, Integer> reward = COIN_REWARDS.get(user.getRandom().nextInt(COIN_REWARDS.size()));
			user.giveItemStack(new ItemStack(reward.getLeft(), reward.getRight()));
		}

		user.incrementStat(Stats.USED.getOrCreateStat(this));
		stack.decrementUnlessCreative(8, user);
		return TypedActionResult.success(stack, true);
	}
}

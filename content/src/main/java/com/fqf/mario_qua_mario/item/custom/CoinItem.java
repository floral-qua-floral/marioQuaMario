package com.fqf.mario_qua_mario.item.custom;

import com.fqf.mario_qua_mario.item.MQMItems;
import com.fqf.mario_qua_mario.item.MQMLootTables;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.server.world.ServerWorld;
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
			new Pair<>(MQMItems.SUPER_MUSHROOM, 3),
			new Pair<>(MQMItems.FIRE_FLOWER, 1),
			new Pair<>(MQMItems.SUPER_LEAF, 1)
	);

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ItemStack stack = user.getStackInHand(hand);
		if(!user.isInCreativeMode() && stack.getCount() < 8) return TypedActionResult.fail(stack);

		user.playSound(MarioSFX.COIN_USE, 1.0F, 1.0F);

		if(!world.isClient()) {
			LootContextParameterSet lootContextParameterSet = new LootContextParameterSet.Builder((ServerWorld) world)
					.add(LootContextParameters.ORIGIN, user.getPos())
					.add(LootContextParameters.THIS_ENTITY, user)
					.luck(user.getLuck())
					.build(LootContextTypes.ADVANCEMENT_REWARD);
			LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(MQMLootTables.POWER_UP_REDEMPTION);
			List<ItemStack> list = lootTable.generateLoot(lootContextParameterSet);

			for(ItemStack itemStack : list) {
				if(!user.giveItemStack(itemStack))
					user.dropItem(itemStack, false, true);
			}

			user.incrementStat(Stats.USED.getOrCreateStat(this));
			stack.decrementUnlessCreative(8, user);
			return TypedActionResult.success(stack, true);
		}

		return TypedActionResult.fail(stack);
	}


}

package com.fqf.mario_qua_mario_content.item;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.item.custom.CoinItem;
import com.fqf.mario_qua_mario_content.item.custom.PowerUpItem;
import com.fqf.mario_qua_mario_content.powerups.Fire;
import com.fqf.mario_qua_mario_content.powerups.Raccoon;
import com.fqf.mario_qua_mario_content.powerups.Super;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import java.util.List;

public class ModItems {
	public static final Item FIRE_FLOWER = registerPowerItem("fire_flower", Fire.ID, 5, 0.3F);
	public static final Item SUPER_LEAF = registerPowerItem("super_leaf", Raccoon.ID, 4, 0.4F);
	public static final Item SUPER_MUSHROOM = registerItem("super_mushroom", new PowerUpItem(new Item.Settings().maxCount(4).food(
			new FoodComponent.Builder().nutrition(6).saturationModifier(0.7F).alwaysEdible().snack().build()
	), Super.ID, 1));

	public static final Item COIN = registerItem("coin", new CoinItem((new Item.Settings()).rarity(Rarity.UNCOMMON)
			.component(DataComponentTypes.LORE, new LoreComponent(List.of(
					styled(Text.translatable("item.mario_qua_mario.coin.lore1", Text.keybind("key.use").formatted(Formatting.BOLD))),
					styled(Text.translatable("item.mario_qua_mario.coin.lore2"))
			)))
	));
	private static MutableText styled(MutableText input) {
		return input.setStyle(Style.EMPTY.withColor(Formatting.GRAY));
	}

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, MarioQuaMarioContent.makeResID(name), item);
	}

	private static Item registerPowerItem(String name, Identifier powerID, int nutrition, float saturation) {
		return registerItem(name, new PowerUpItem(new Item.Settings().maxCount(1).food(new FoodComponent.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build()), powerID, 2));
	}

	public static void registerModItems() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(fabricItemGroupEntries ->
				fabricItemGroupEntries.add(COIN));
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(fabricItemGroupEntries -> {
			fabricItemGroupEntries.add(SUPER_MUSHROOM);
			fabricItemGroupEntries.add(FIRE_FLOWER);
			fabricItemGroupEntries.add(SUPER_LEAF);
		});
	}
}

package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;

public class MarioQuaMarioContentDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {

	}

	private static class MarioModelGenerator extends FabricModelProvider {
		public MarioModelGenerator(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {

		}

		@Override
		public void generateItemModels(ItemModelGenerator itemModelGenerator) {
			itemModelGenerator.register(ModItems.COIN, Models.GENERATED);
		}
	}
}

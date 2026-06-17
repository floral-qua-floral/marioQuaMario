package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.item.ModItems;
import com.fqf.mario_qua_mario.util.LightningStrikableInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerInventory.class)
public class PlayerInventoryStruckByLightningMixin implements LightningStrikableInventory {
	@Shadow @Final public PlayerEntity player;

	@Override
	public void mqm$strike() {
		if(this.player instanceof ServerPlayerEntity serverPlayer) {
			int totalConverted = 0;

			totalConverted += convert((PlayerInventory) (Object) this);
			totalConverted += convert(serverPlayer.playerScreenHandler.getCraftingInput());

			ItemStack oldCursorStack = serverPlayer.currentScreenHandler.getCursorStack();
			if(oldCursorStack.isOf(ModItems.SUPER_MUSHROOM)) {
				totalConverted += oldCursorStack.getCount();
				serverPlayer.currentScreenHandler.setCursorStack(equivalentNumberOfMiniMushrooms(oldCursorStack));
			}

			if(totalConverted > 0) {
				MarioQuaMario.LOGGER.info("Converted {} Super Mushrooms in {}'s inventory!", totalConverted, serverPlayer.getName().getString());

				serverPlayer.currentScreenHandler.sendContentUpdates();
				serverPlayer.playerScreenHandler.onContentChanged(serverPlayer.getInventory());
			}
		}
	}

	@Unique
	private static int convert(Inventory inventory) {
		int totalConverted = 0;

		for (int slotIndex = 0; slotIndex < inventory.size(); slotIndex++) {
			ItemStack oldStack = inventory.getStack(slotIndex);
			if(oldStack.isOf(ModItems.SUPER_MUSHROOM)) {
				inventory.setStack(slotIndex, equivalentNumberOfMiniMushrooms(oldStack));
				totalConverted += oldStack.getCount();
			}
		}

		return totalConverted;
	}

	@Unique
	private static ItemStack equivalentNumberOfMiniMushrooms(ItemStack old) {
		return new ItemStack(ModItems.MINI_MUSHROOM, old.getCount());
	}
}

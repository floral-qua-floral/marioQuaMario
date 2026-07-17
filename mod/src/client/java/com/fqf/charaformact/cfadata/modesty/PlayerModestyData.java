package com.fqf.charaformact.cfadata.modesty;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import com.fqf.charaformact.util.DebugHudUtil;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;
import org.apache.commons.lang3.mutable.MutableByte;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.fqf.charaformact_api.util.CfaTags.EquipmentCoveringTags.*;

public class PlayerModestyData {
	private static final EquipmentSlot[] PLAYER_ARMOR_SLOTS =
			{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private final AbstractClientPlayerEntity PLAYER;
	private final EnumMap<EquipmentCoverSpot, MutableByte> MODESTY_MAP;
	private final EnumMap<EquipmentSlot, RenderedArmorInfo> LAST_RENDERED_ARMOR;

	public PlayerModestyData(CfaClientDataImpl data) {
		this.PLAYER = data.getPlayer();
		this.MODESTY_MAP = new EnumMap<>(EquipmentCoverSpot.class);
		for(EquipmentCoverSpot spot : EquipmentCoverSpot.values()) {
			this.MODESTY_MAP.put(spot, new MutableByte());
		}

		this.LAST_RENDERED_ARMOR = new EnumMap<>(EquipmentSlot.class);
		for(EquipmentSlot slot : PLAYER_ARMOR_SLOTS) {
			this.LAST_RENDERED_ARMOR.put(slot, new RenderedArmorInfo(ItemStack.EMPTY, null));
		}
	}

	public void updateRenderedArmor() {
		for(EquipmentSlot slot : PLAYER_ARMOR_SLOTS) {
			ItemStack equipped = this.PLAYER.getEquippedStack(slot);
			RenderedArmorInfo prevRenderedArmor = this.LAST_RENDERED_ARMOR.get(slot);
			if(!prevRenderedArmor.STACK.equals(equipped)) {
				// Armor has been swapped!
				CharaFormAct.LOGGER.info("Item in armor slot {} has changed from {}->{}", slot, prevRenderedArmor.STACK.getItem(), equipped.getItem());
				RenderedArmorInfo renderingNow = new RenderedArmorInfo(equipped, slot);
				this.LAST_RENDERED_ARMOR.put(slot, renderingNow);

				BiConsumer<RenderedArmorInfo, Consumer<MutableByte>> updater = (info, consumer) ->
						info.COVER_SPOTS.forEach(spot -> consumer.accept(this.MODESTY_MAP.get(spot)));

				if(!renderingNow.COVER_SPOTS.equals(prevRenderedArmor.COVER_SPOTS)) {
					updater.accept(prevRenderedArmor, MutableByte::decrement);
					updater.accept(renderingNow, MutableByte::increment);
				}
			}
		}
	}

	public boolean isSpotCovered(EquipmentCoverSpot spot) {
		return this.MODESTY_MAP.get(spot).byteValue() > 0;
	}

	public boolean renderDebugHud(DebugHudUtil.Pair pair) {
		boolean success = false;
		for (Map.Entry<EquipmentCoverSpot, MutableByte> entry : this.MODESTY_MAP.entrySet()) {
			byte count = entry.getValue().byteValue();
			if (count > 0) {
				DebugHudUtil.renderDebugText(pair, entry.getKey(), ":", count);
				success = true;
			}
		}
		return success;
	}

	public static boolean doesArmorSlotItemCoverSpot(ItemStack item, EquipmentSlot slot, EquipmentCoverSpot spot) {
		return switch(spot) {
			case HEADGEAR -> !item.isIn(IS_NOT_HEADGEAR);
			case SCALP -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.UP);
				}
				yield !item.isIn(DOES_NOT_COVER_SCALP);
			}
			case FACE -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.SOUTH);
				}
				yield item.isIn(COVERS_FACE_FROM_HEAD_SLOT);
			}
			case EARS -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.EAST)
							|| blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.WEST);
				}
				yield !item.isIn(DOES_NOT_COVER_EARS);
			}
			case UPPER_CHEST -> !item.isIn(DOES_NOT_COVER_CHEST);
			case BELLY -> !item.isIn(DOES_NOT_COVER_BELLY);
			case BACK -> !item.isIn(DOES_NOT_COVER_BACK);
			case SHOULDERS -> !item.isIn(DOES_NOT_COVER_SHOULDERS);
			case HANDS -> item.isIn(COVERS_HANDS_FROM_CHEST_SLOT);
			case BUTT -> !item.isIn(DOES_NOT_COVER_BUTT);
			case TOES -> slot == EquipmentSlot.FEET ? !item.isIn(DOES_NOT_COVER_TOES) : item.isIn(COVERS_TOES_FROM_LEGS_SLOT);
		};
	}

	// TODO: Use events or mixins to make this function with mods like Accessories, Trinkets, maybe even The Aether if
	//  i'm gonna be nuts :3
	public static boolean doesModdedSlotItemCoverSpot(ItemStack item, EquipmentCoverSpot spot) {
		return !item.isIn(switch(spot) {
			case HEADGEAR -> IS_NOT_HEADGEAR;
			case SCALP -> DOES_NOT_COVER_SCALP;
			case FACE -> NEVER_COVERS_FACE;
			case EARS -> DOES_NOT_COVER_EARS;
			case UPPER_CHEST -> DOES_NOT_COVER_CHEST;
			case BELLY -> DOES_NOT_COVER_BELLY;
			case BACK -> DOES_NOT_COVER_BACK;
			case SHOULDERS -> DOES_NOT_COVER_SHOULDERS;
			case HANDS -> NEVER_COVERS_HANDS;
			case BUTT -> DOES_NOT_COVER_BUTT;
			case TOES -> DOES_NOT_COVER_TOES;
		});
	}
}

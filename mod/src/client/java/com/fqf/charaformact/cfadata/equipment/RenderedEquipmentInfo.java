package com.fqf.charaformact.cfadata.equipment;

import com.fqf.charaformact.registries.ImmutableCollectionHelper;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import com.fqf.charaformact_api.interfaces.AppearanceCoveringEquipment;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.EmptyBlockView;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;

import static com.fqf.charaformact_api.util.CfaTags.EquipmentCoveringTags.*;

public final class RenderedEquipmentInfo {
	public static final RenderedEquipmentInfo EMPTY = new RenderedEquipmentInfo();

	public final ItemStack STACK;
	public final Set<EquipmentCoverSpot> COVER_SPOTS;

	public RenderedEquipmentInfo() {
		this.STACK = ItemStack.EMPTY;
		this.COVER_SPOTS = Set.of();
	}
	public RenderedEquipmentInfo(ItemStack stack, VisibleEquipmentSlot slot) {
		this.STACK = stack;
		if(stack.getItem() instanceof AppearanceCoveringEquipment coveringStack) {
			ImmutableSet<EquipmentCoverSpot> immutable = ImmutableCollectionHelper.accumulateSet(builder ->
					coveringStack.accumulateCoveringSpots(this.STACK, builder));
			// Copy the contents of the immutable set into an EnumSet since EnumSets are very efficient to read. But
			// doing so means we lose the immutability of the original set, since EnumSets are mutable. So we just
			// store an unmodifiable view of the EnumSet.
			// Surely only a buffoon would write code like this???
			this.COVER_SPOTS = Collections.unmodifiableSet(EnumSet.copyOf(immutable));
		}
		else {
			EnumSet<EquipmentCoverSpot> mutableCoverSpots = EnumSet.noneOf(EquipmentCoverSpot.class);

			Predicate<EquipmentCoverSpot> isCoveringSpotPredicate = slot.IS_VANILLA
					? spot -> doesArmorSlotItemCoverSpot(stack, slot, spot)
					: spot -> doesModdedSlotItemCoverSpot(stack, spot);

			for(EquipmentCoverSpot potentialCoveringSpot : slot.POTENTIAL_COVERING_SPOTS)
				if(isCoveringSpotPredicate.test(potentialCoveringSpot))
					mutableCoverSpots.add(potentialCoveringSpot);

			this.COVER_SPOTS = Collections.unmodifiableSet(mutableCoverSpots);
		}
	}

	private static boolean doesArmorSlotItemCoverSpot(ItemStack item, VisibleEquipmentSlot slot, EquipmentCoverSpot spot) {
		return switch(spot) {
			case HEADGEAR -> !item.isIn(IS_NOT_HEADGEAR);
			case SCALP -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.UP);
				}
				yield !item.isIn(DOES_NOT_COVER_SCALP);
			}
			case EYES -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.SOUTH);
				}
				yield item.isIn(COVERS_EYES_FROM_HEAD_SLOT);
			}
			case NOSE -> {
				if(item.getItem() instanceof BlockItem blockItem) {
					yield blockItem.getBlock().getDefaultState().isSideSolidFullSquare(EmptyBlockView.INSTANCE, BlockPos.ORIGIN, Direction.SOUTH);
				}
				yield item.isIn(COVERS_NOSE_FROM_HEAD_SLOT);
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
			case TOES -> slot == VisibleEquipmentSlot.VANILLA_BOOTS
					? !item.isIn(DOES_NOT_COVER_TOES)
					: item.isIn(COVERS_TOES_FROM_LEGS_SLOT);
		};
	}

	private static boolean doesModdedSlotItemCoverSpot(ItemStack item, EquipmentCoverSpot spot) {
		if(spot == EquipmentCoverSpot.NOSE) // Special case: Nose is never covered except by explicitly nose-covering items
			return item.isIn(COVERS_NOSE_FROM_HEAD_SLOT);

		return !item.isIn(switch(spot) {
			case HEADGEAR -> IS_NOT_HEADGEAR;
			case SCALP -> DOES_NOT_COVER_SCALP;
			case EYES -> NEVER_COVERS_EYES;
			case NOSE -> throw new AssertionError("This error should not be reachable!");
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

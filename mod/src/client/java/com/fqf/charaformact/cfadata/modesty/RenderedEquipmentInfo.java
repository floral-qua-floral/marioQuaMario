package com.fqf.charaformact.cfadata.modesty;

import com.fqf.charaformact.registries.ImmutableCollectionHelper;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import com.fqf.charaformact_api.interfaces.AppearanceCoveringEquipment;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class RenderedEquipmentInfo {
	public static final RenderedEquipmentInfo EMPTY = new RenderedEquipmentInfo(ItemStack.EMPTY);

	public static final Event<RenderEquipment> UPDATE_EQUIPMENT_RENDERING = EventFactory.createArrayBacked(RenderEquipment.class, callbacks -> player -> {
		for(RenderEquipment callback : callbacks) {
			callback.onUpdateEquipment(player);
		}
	});

	@FunctionalInterface
	public interface RenderEquipment {
		void onUpdateEquipment(AbstractClientPlayerEntity player);
	}

	public final ItemStack STACK;
	public final Set<EquipmentCoverSpot> COVER_SPOTS;
	private final boolean IS_HARDCODED;

	protected RenderedEquipmentInfo(ItemStack stack) {
		this.STACK = stack.copy();
		if(this.STACK.getItem() instanceof AppearanceCoveringEquipment covering) { // Immutable if spots are hardcoded!
			ImmutableSet<EquipmentCoverSpot> immutable = ImmutableCollectionHelper.accumulateSet(builder ->
					covering.accumulateCoveringSpots(this.STACK, builder));
			this.COVER_SPOTS = Collections.unmodifiableSet(EnumSet.copyOf(immutable));
			this.IS_HARDCODED = true;
		}
		else if(stack.isEmpty()) {
			this.COVER_SPOTS = EnumSet.noneOf(EquipmentCoverSpot.class);
			this.IS_HARDCODED = true;
		}
		else {
			this.COVER_SPOTS = EnumSet.noneOf(EquipmentCoverSpot.class);
			this.IS_HARDCODED = false;
		}
	}

	protected boolean canCoverSpot(ItemStack stack, EquipmentCoverSpot spot) {
		return PlayerModestyData.doesModdedSlotItemCoverSpot(stack, spot);
	}

	protected final void tryCover(EquipmentCoverSpot... spots) {
		if(this.IS_HARDCODED) return;
		for(EquipmentCoverSpot spot : spots)
			if(this.canCoverSpot(this.STACK, spot))
				this.COVER_SPOTS.add(spot);
	}
}

package com.fqf.charaformact.cfadata.equipment;

import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory.*;
import static com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot.*;

public enum VisibleEquipmentSlot {
	// Head
	VANILLA_HELMET(true, ARMOR_OUTER, HEADGEAR, SCALP, EARS, EYES, NOSE),
	HAT(false, ARMOR_INNER, HEADGEAR, SCALP),
	FACE(false, SPECIAL, HEADGEAR, EYES, NOSE),
	EARS_SLOT(false, ARMOR_INNER, HEADGEAR, EARS, SCALP),

	// Torso
	VANILLA_CHESTPLATE(true, ARMOR_OUTER, UPPER_CHEST, BELLY, BACK, SHOULDERS, HANDS),
	BACK_SLOT(false, SPECIAL, BACK),
	BELT(false, ARMOR_INNER, BELLY),

	// Arms
	GLOVES(false, SPECIAL, HANDS),

	// Legs
	VANILLA_LEGGINGS(true, ARMOR_INNER, BELLY, BUTT, TOES),
	VANILLA_BOOTS(true, ARMOR_OUTER, TOES),

	// Other
	UNRECOGNIZED(false, UNKNOWN);

	public final boolean IS_VANILLA;
	public final EquipmentFeatureCategory TRANSFORMATION_CATEGORY;
	public final Set<EquipmentCoverSpot> POTENTIAL_COVERING_SPOTS;

	VisibleEquipmentSlot(boolean isVanilla, EquipmentFeatureCategory transformationCategory, EquipmentCoverSpot... coveringSpots) {
		this.IS_VANILLA = isVanilla;
		this.TRANSFORMATION_CATEGORY = transformationCategory;
		this.POTENTIAL_COVERING_SPOTS = Collections.unmodifiableSet(coveringSpots.length == 0
				? EnumSet.noneOf(EquipmentCoverSpot.class)
				: EnumSet.copyOf(List.of(coveringSpots))
		);
	}
}

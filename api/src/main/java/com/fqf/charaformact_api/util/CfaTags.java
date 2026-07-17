package com.fqf.charaformact_api.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static net.minecraft.registry.RegistryKeys.ENTITY_TYPE;
import static net.minecraft.registry.RegistryKeys.DAMAGE_TYPE;
import static net.minecraft.registry.RegistryKeys.BLOCK;
import static net.minecraft.registry.RegistryKeys.ITEM;

public interface CfaTags {
	TagKey<EntityType<?>> NOT_HIT_BY_COLLISION_ATTACKS = getTag(ENTITY_TYPE, "not_hurt_by_collision_attacks");
	TagKey<EntityType<?>> HARMS_COLLISION_ATTACKERS = getTag(ENTITY_TYPE, "harms_collision_attackers");

	TagKey<DamageType> COLLISION_ATTACKS = getTag(DAMAGE_TYPE, "collision_attacks");

	TagKey<Block> UNBAPPABLE = getTag(BLOCK, "unbappable");
	TagKey<Block> NOT_POWERED_WHEN_BAPPED = getTag(BLOCK, "not_powered_when_bapped");
	TagKey<Block> USES_DOUBLE_HARDNESS_WHEN_BAPPED = getTag(BLOCK, "uses_double_hardness_when_bapped");
	TagKey<Block> USES_HALF_HARDNESS_WHEN_BAPPED = getTag(BLOCK, "uses_half_hardness_when_bapped");
	TagKey<Block> DESTROYED_BY_INDIRECT_BAP = getTag(BLOCK, "destroyed_by_indirect_bap");

	interface EquipmentCoveringTags {
		TagKey<Item> IS_NOT_HEADGEAR = getTag(ITEM, "covering/head/is_not_headgear");
		TagKey<Item> DOES_NOT_COVER_SCALP = getTag(ITEM, "covering/head/does_not_cover_scalp");
		TagKey<Item> COVERS_FACE_FROM_HEAD_SLOT = getTag(ITEM, "covering/head/covers_face_from_head_slot");
		TagKey<Item> NEVER_COVERS_FACE = getTag(ITEM, "covering/head/never_covers_face");
		TagKey<Item> DOES_NOT_COVER_EARS = getTag(ITEM, "covering/head/does_not_cover_ears");

		TagKey<Item> DOES_NOT_COVER_CHEST = getTag(ITEM, "covering/torso/does_not_cover_chest");
		TagKey<Item> DOES_NOT_COVER_BELLY = getTag(ITEM, "covering/torso/does_not_cover_belly");
		TagKey<Item> DOES_NOT_COVER_BACK = getTag(ITEM, "covering/torso/does_not_cover_back");

		TagKey<Item> DOES_NOT_COVER_SHOULDERS = getTag(ITEM, "covering/arms/does_not_cover_shoulders");
		TagKey<Item> COVERS_HANDS_FROM_CHEST_SLOT = getTag(ITEM, "covering/arms/covers_hands_from_chest_slot");
		TagKey<Item> NEVER_COVERS_HANDS = getTag(ITEM, "covering/arms/never_covers_hands");

		TagKey<Item> DOES_NOT_COVER_BUTT = getTag(ITEM, "covering/legs/does_not_cover_butt");

		TagKey<Item> COVERS_TOES_FROM_LEGS_SLOT = getTag(ITEM, "covering/legs/covers_toes_from_legs_slot");
		TagKey<Item> DOES_NOT_COVER_TOES = getTag(ITEM, "covering/legs/does_not_cover_toes");
	}




	private static <T> TagKey<T> getTag(RegistryKey<Registry<T>> key, String name) {
		return TagKey.of(key, Identifier.of("charaformact", name));
	}
}

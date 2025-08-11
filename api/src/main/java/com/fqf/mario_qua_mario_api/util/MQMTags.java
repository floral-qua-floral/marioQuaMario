package com.fqf.mario_qua_mario_api.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static net.minecraft.registry.RegistryKeys.BLOCK;
import static net.minecraft.registry.RegistryKeys.ENTITY_TYPE;

public interface MQMTags {
	TagKey<EntityType<?>> UNSTOMPABLE = getTag(ENTITY_TYPE, "unstompable");
	TagKey<EntityType<?>> HURTS_TO_STOMP = getTag(ENTITY_TYPE, "hurts_to_stomp");

	TagKey<Block> UNBAPPABLE = getTag(BLOCK, "unbappable");
	TagKey<Block> NOT_POWERED_WHEN_BAPPED = getTag(BLOCK, "not_powered_when_bapped");
	TagKey<Block> USES_DOUBLE_HARDNESS_WHEN_BAPPED = getTag(BLOCK, "uses_double_hardness_when_bapped");
	TagKey<Block> USES_HALF_HARDNESS_WHEN_BAPPED = getTag(BLOCK, "uses_half_hardness_when_bapped");
	TagKey<Block> DESTROYED_BY_INDIRECT_BAP = getTag(BLOCK, "destroyed_by_indirect_bap");

	private static <T> TagKey<T> getTag(RegistryKey<Registry<T>> key, String name) {
		return TagKey.of(key, Identifier.of("mario_qua_mario", name));
	}
}

package com.fqf.mario_qua_mario_content.util;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import static net.minecraft.registry.RegistryKeys.BLOCK;
import static net.minecraft.registry.RegistryKeys.ENTITY_TYPE;
import static net.minecraft.registry.RegistryKeys.DAMAGE_TYPE;

public interface MQMContentTags {
	TagKey<DamageType> FLATTENS_ENTITIES = getTag(DAMAGE_TYPE, "flattens_entities");

	TagKey<EntityType<?>> RISING_STOMPABLE_NONMONSTERS = getTag(ENTITY_TYPE, "rising_stompable_nonmonsters");
	TagKey<EntityType<?>> FIRE_MARIO_PUNCH_TARGETS = getTag(ENTITY_TYPE, "fire_mario_punch_targets");

	TagKey<Block> CLIMBABLE = getTag(BLOCK, "mario_climbable");
	TagKey<Block> SOMETIMES_CLIMBABLE_PANES = getTag(BLOCK, "mario_sometimes_climbable_panes");
	TagKey<Block> UNSLIDEABLE_WALLS = getTag(BLOCK, "mario_unslideable_walls");
	TagKey<Block> DESTROYED_BY_FIREBALL = getTag(BLOCK, "destroyed_by_mario_fireball");

	private static <T> TagKey<T> getTag(RegistryKey<Registry<T>> key, String name) {
		return TagKey.of(key, Identifier.of("mario_qua_mario", name));
	}
}

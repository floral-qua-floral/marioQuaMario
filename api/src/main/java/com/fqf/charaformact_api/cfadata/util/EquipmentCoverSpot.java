package com.fqf.charaformact_api.cfadata.util;

/**
 * All the different parts of the player's Appearance that might be covered up by equipment they're wearing. This is
 * meant to be used in a custom AppearanceModel so you can hide geometry which should be covered up, mainly to prevent
 * clipping.
 */
public enum EquipmentCoverSpot {
	HEADGEAR, // Sweatbands; Things that would replace a hat
	SCALP, // Hats, Helmets; Things that would cover a cat's ears
	EYES, // Goggles, Glasses, Masks; Things that would cover up the eyes and replace a pair of glasses
	NOSE, // Diving Helmets, Frog-Mouth Helms, Masks; Things that would cover a protruding nose or beak
	EARS, // Helmets, Hoods, Earmuffs; Things that would cover the sides of the head, a human's ears

	UPPER_CHEST, // Chestplates, Shirts; Things that would cover a breast
	BELLY, // Belts, Leggings; Things that would wrap around the front of a waist
	BACK, // Elytra, Capes, Backpacks; Things that would cover a pair of wings

	SHOULDERS, // Chestplate Pauldrons
	HANDS, // Gloves; Things that would cover claws

	BUTT, // Leggings, Robes, Skirts; Things that would cover the base of a tail

	TOES // Boots, Shoes; Things that would cover hindclaws
}

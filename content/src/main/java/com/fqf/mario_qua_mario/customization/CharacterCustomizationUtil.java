package com.fqf.mario_qua_mario.customization;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;

import java.util.OptionalInt;
import java.util.UUID;

public class CharacterCustomizationUtil {
	public static final TrackedData<Boolean> ALWAYS_USE_SKIN_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Integer> SKIN_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> CAP_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> SPOTS_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<Integer> VEST_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	public static final TrackedData<OptionalInt> SHIRT_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	public static final TrackedData<Boolean> HAS_PIGTAILS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public static final String PERSISTENT_DATA_KEY = MarioQuaMario.MOD_ID + "_model_customizations";
	public static final String ALWAYS_USE_SKIN_COLOR_KEY = "always_use_custom_skin_tone";
	public static final String SKIN_COLOR_KEY = "skin_tone";
	public static final String CAP_COLOR_KEY = "cap_color";
	public static final String SPOTS_COLOR_KEY = "spots_color";
	public static final String VEST_COLOR_KEY = "vest_color";
	public static final String HAS_SHIRT_KEY = "has_shirt";
	public static final String SHIRT_COLOR_KEY = "shirt_color";
	public static final String HAS_PIGTAILS_KEY = "has_pigtails";
}

package com.fqf.mario_qua_mario.customization;

import net.minecraft.entity.data.TrackedData;

import java.util.UUID;

public interface CustomizablePlayerEntity {
	default <T> void mqm$updateCustomizationData(TrackedData<T> trackedData, T newValue) {
		throw new IllegalStateException("This should have been implemented! >:(");
	}

	default <T> T mqm$getCustomizationData(TrackedData<T> trackedData) {
		throw new IllegalStateException("This also should have been implemented! >:(");
	}

	default void mqm$resetSkinToneOnly(UUID uuid) {
		throw new IllegalStateException("This also ALSO should have been implemented! >:(");
	}

	default void mqm$resetCustomizationData(UUID uuid) {
		throw new IllegalStateException("This also ALSO SUPER-ALSO should have been implemented! >:(");
	}
}

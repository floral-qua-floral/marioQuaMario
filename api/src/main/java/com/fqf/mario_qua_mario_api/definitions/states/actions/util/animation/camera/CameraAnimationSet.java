package com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CameraAnimationSet(
		@NotNull CameraAnimationOptionGetter optionGetter,
		@NotNull CameraAnimation authentic,
		@Nullable CameraAnimation gentle,
		@Nullable CameraAnimation minimal
) {
}

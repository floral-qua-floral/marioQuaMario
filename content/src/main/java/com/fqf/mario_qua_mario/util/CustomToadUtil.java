package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Unique;

import java.util.OptionalInt;

public class CustomToadUtil {
	@Unique public static final TrackedData<Integer> SKIN_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> CAP_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> SPOTS_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> VEST_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<OptionalInt> SHIRT_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	@Unique public static final TrackedData<Boolean> HAS_PIGTAILS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public static void registerCommand() {
		MarioQuaMario.LOGGER.info("Registering Custom Toad command! <3");
	}

	public interface CustomToadEntity {
		default <T> void mqm$updateToadData(TrackedData<T> trackedData, T newValue) {
			throw new IllegalStateException("This should have been implemented! >:(");
		}

		default <T> T mqm$getToadData(TrackedData<T> trackedData) {
			throw new IllegalStateException("This also should have been implemented! >:(");
		}
	}
}

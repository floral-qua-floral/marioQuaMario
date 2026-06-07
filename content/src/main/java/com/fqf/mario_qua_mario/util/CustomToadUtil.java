package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Unique;

import java.util.OptionalInt;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CustomToadUtil {
	@Unique public static final TrackedData<Integer> SKIN_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> CAP_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> SPOTS_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<Integer> VEST_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.INTEGER);
	@Unique public static final TrackedData<OptionalInt> SHIRT_COLOR = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.OPTIONAL_INT);
	@Unique public static final TrackedData<Boolean> HAS_PIGTAILS = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.BOOLEAN);

	public static final String PERSISTENT_DATA_KEY = MarioQuaMario.MOD_ID + "_custom_toad_data";
	public static final String SKIN_COLOR_KEY = "skin_tone";
	public static final String CAP_COLOR_KEY = "cap_color";
	public static final String SPOTS_COLOR_KEY = "spots_color";
	public static final String VEST_COLOR_KEY = "vest_color";
	public static final String HAS_SHIRT_KEY = "has_shirt";
	public static final String SHIRT_COLOR_KEY = "shirt_color";
	public static final String HAS_PIGTAILS_KEY = "has_pigtails";

	public static void registerCommand() {
		MarioQuaMario.LOGGER.info("Registering Custom Toad command! <3");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(literal("customtoad")
					.then(literal("reset")
							.executes(commandContext -> {
								ServerPlayerEntity player = commandContext.getSource().getPlayerOrThrow();
								((CustomToadEntity) player).mqm$resetToadData(player.getGameProfile().getId());
								return 1;
							})
					)
					.then(literal("randomize")
							.executes(commandContext -> {
								get(commandContext).mqm$resetToadData(UUID.randomUUID());
								return 1;
							})
					)
					.then(literal("set")
							.then(makeCustomizationBranch("skin", SKIN_COLOR))
							.then(makeCustomizationBranch("cap", CAP_COLOR))
							.then(makeCustomizationBranch("spots", SPOTS_COLOR))
							.then(makeCustomizationBranch("vest", VEST_COLOR))
							.then(literal("shirt")
									.then(literal("remove")
											.executes(commandContext -> {
												get(commandContext).mqm$updateToadData(SHIRT_COLOR, OptionalInt.empty());
												return 1;
											})
									)
									.then(literal("setColor")
											.then(makeColorAssignmentBranch(commandContext -> {
												get(commandContext).mqm$updateToadData(SHIRT_COLOR, OptionalInt.of(getArgb(commandContext)));
												return 1;
											}))
									)
							)
							.then(literal("pigtails")
									.then(argument("isEnabled", BoolArgumentType.bool())
											.executes(commandContext -> {
												get(commandContext).mqm$updateToadData(HAS_PIGTAILS, BoolArgumentType.getBool(commandContext, "isEnabled"));
												return 1;
											})
									)
							)
					)
			);
		});
	}

	private static CustomToadEntity get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return (CustomToadEntity) context.getSource().getPlayerOrThrow();
	}
	private static ArgumentBuilder<ServerCommandSource, ?> makeCustomizationBranch(String name, TrackedData<Integer> trackedData) {
		return literal(name)
				.then(makeColorAssignmentBranch(commandContext -> {
					((CustomToadEntity) commandContext.getSource().getPlayerOrThrow()).mqm$updateToadData(trackedData, getArgb(commandContext));
					return 1;
				}));
	}
	private static int getArgb(CommandContext<ServerCommandSource> commandContext) {
		return ColorHelper.Argb.getArgb(
				IntegerArgumentType.getInteger(commandContext, "red"),
				IntegerArgumentType.getInteger(commandContext, "green"),
				IntegerArgumentType.getInteger(commandContext, "blue")
		);
	}
	private static ArgumentBuilder<ServerCommandSource, ?> makeColorAssignmentBranch(Command<ServerCommandSource> execution) {
		return argument("red", IntegerArgumentType.integer(0, 255))
				.then(argument("green", IntegerArgumentType.integer(0, 255))
						.then(argument("blue", IntegerArgumentType.integer(0, 255))
								.executes(execution)
						)
				);
	}

	public interface CustomToadEntity {
		default <T> void mqm$updateToadData(TrackedData<T> trackedData, T newValue) {
			throw new IllegalStateException("This should have been implemented! >:(");
		}

		default <T> T mqm$getToadData(TrackedData<T> trackedData) {
			throw new IllegalStateException("This also should have been implemented! >:(");
		}

		default void mqm$resetToadData(UUID uuid) {
			throw new IllegalStateException("This also ALSO should have been implemented! >:(");
		}
	}
}

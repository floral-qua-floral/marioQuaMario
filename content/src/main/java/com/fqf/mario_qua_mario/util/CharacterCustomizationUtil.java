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

import java.util.OptionalInt;
import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

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

	public static void registerCommand() {
		MarioQuaMario.LOGGER.info("Registering Custom Toad command! <3");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {

			LiteralCommandNode<ServerCommandSource> toadBranch = literal("customize_toad")
					.then(literal("reset")
							.executes(commandContext -> {
								ServerPlayerEntity player = commandContext.getSource().getPlayerOrThrow();
								((CustomizablePlayerEntity) player).mqm$resetCustomizationData(player.getGameProfile().getId());
								return 1;
							})
					)
					.then(literal("randomize")
							.executes(commandContext -> {
								get(commandContext).mqm$resetCustomizationData(UUID.randomUUID());
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
												get(commandContext).mqm$updateCustomizationData(SHIRT_COLOR, OptionalInt.empty());
												return 1;
											})
									)
									.then(literal("setColor")
											.then(makeColorAssignmentBranch(commandContext -> {
												get(commandContext).mqm$updateCustomizationData(SHIRT_COLOR, OptionalInt.of(getArgb(commandContext)));
												return 1;
											}))
									)
							)
							.then(literal("pigtails")
									.then(argument("isEnabled", BoolArgumentType.bool())
											.executes(commandContext -> {
												get(commandContext).mqm$updateCustomizationData(HAS_PIGTAILS, BoolArgumentType.getBool(commandContext, "isEnabled"));
												return 1;
											})
									)
							)
					).build();

			LiteralCommandNode<ServerCommandSource> otherBranch = literal("customize_skin")
					.then(literal("reset")
							.executes(commandContext -> {
								get(commandContext).mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, false);
								return 1;
							})
					)
					.then(literal("randomize")
							.executes(commandContext -> {
								CustomizablePlayerEntity player = get(commandContext);
								player.mqm$resetSkinToneOnly(UUID.randomUUID());
								player.mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, true);
								return 1;
							})
					)
					.then(literal("setColor")
							.then(makeColorAssignmentBranch(commandContext -> {
								CustomizablePlayerEntity player = get(commandContext);
								player.mqm$updateCustomizationData(SKIN_COLOR, getArgb(commandContext));
								player.mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, true);
								return 1;
							}))
					).build();

			LiteralCommandNode<ServerCommandSource> root = dispatcher.register(literal("marioquamario")
					.then(toadBranch)
					.then(otherBranch)
			);
			dispatcher.register(literal("mqm").redirect(root));
			dispatcher.register(literal("customtoad").redirect(toadBranch));
		});
	}

	private static CustomizablePlayerEntity get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return (CustomizablePlayerEntity) context.getSource().getPlayerOrThrow();
	}
	private static ArgumentBuilder<ServerCommandSource, ?> makeCustomizationBranch(String name, TrackedData<Integer> trackedData) {
		return literal(name)
				.then(makeColorAssignmentBranch(commandContext -> {
					((CustomizablePlayerEntity) commandContext.getSource().getPlayerOrThrow()).mqm$updateCustomizationData(trackedData, getArgb(commandContext));
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
}

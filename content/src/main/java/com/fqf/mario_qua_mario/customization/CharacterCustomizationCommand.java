package com.fqf.mario_qua_mario.customization;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.ColorHelper;

import java.util.OptionalInt;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static com.fqf.mario_qua_mario.customization.CharacterCustomizationUtil.*;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CharacterCustomizationCommand {
	public static void registerCommand() {


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
							.then(makeAdvancedColorAssignmentBranch(
									"skin",
									DefaultSkinTone.values(), DefaultSkinTone::getName, DefaultSkinTone::getARBG,
									SKIN_COLOR
							))
							.then(makeAdvancedColorAssignmentBranch("cap", CAP_COLOR))
							.then(makeAdvancedColorAssignmentBranch("spots", SPOTS_COLOR))
							.then(makeAdvancedColorAssignmentBranch("vest", VEST_COLOR))
							.then(literal("shirt")
									.then(literal("remove")
											.executes(commandContext -> {
												get(commandContext).mqm$updateCustomizationData(SHIRT_COLOR, OptionalInt.empty());
												return 1;
											})
									)
									.then(makeAdvancedColorAssignmentBranch(
											"setColor",
											DyeColor.values(), DyeColor::getName, DyeColor::getEntityColor,
											(commandContext, argb) -> {
												get(commandContext).mqm$updateCustomizationData(SHIRT_COLOR, OptionalInt.of(argb));
												return 1;
											}
									))
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

			LiteralCommandNode<ServerCommandSource> root = dispatcher.register(literal("marioquamario")
					.then(toadBranch)
					.then(literal("customize_skin")
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
							.then(makeAdvancedColorAssignmentBranch(
									"setColor",
									DefaultSkinTone.values(), DefaultSkinTone::getName, DefaultSkinTone::getARBG,
									(commandContext, argb) -> {
										CustomizablePlayerEntity player = get(commandContext);
										player.mqm$updateCustomizationData(SKIN_COLOR, argb);
										player.mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, true);
										return 1;
									}
							))
					)
			);
			dispatcher.register(literal("mqm").redirect(root));
			dispatcher.register(literal("customtoad").redirect(toadBranch));
		});
	}

	private static CustomizablePlayerEntity get(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		return (CustomizablePlayerEntity) context.getSource().getPlayerOrThrow();
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

	private static ArgumentBuilder<ServerCommandSource, ?> makeAdvancedColorAssignmentBranch(
			String root, TrackedData<Integer> trackedData
	) {
		return makeAdvancedColorAssignmentBranch(
				root,
				DyeColor.values(), DyeColor::getName, DyeColor::getEntityColor,
				trackedData
		);
	}
	private static <T> ArgumentBuilder<ServerCommandSource, ?> makeAdvancedColorAssignmentBranch(
			String root,
			T[] defaultOptions,
			Function<T, String> toStringFunction,
			ToIntFunction<T> toIntFunction,
			TrackedData<Integer> trackedData
	) {
		return makeAdvancedColorAssignmentBranch(root, defaultOptions, toStringFunction, toIntFunction, (context, argb) -> {
			get(context).mqm$updateCustomizationData(trackedData, argb);
			return 1;
		});
	}
	private static <T> ArgumentBuilder<ServerCommandSource, ?> makeAdvancedColorAssignmentBranch(
			String root,
			T[] defaultOptions,
			Function<T, String> toStringFunction,
			ToIntFunction<T> toIntFunction,
			ColorExecution execution
	) {
		LiteralArgumentBuilder<ServerCommandSource> ofDefaultOptionBranch = literal("of");
		for(T defaultOption : defaultOptions) {
			ofDefaultOptionBranch.then(literal(toStringFunction.apply(defaultOption))
					.executes(
							commandContext -> execution.run(commandContext, toIntFunction.applyAsInt(defaultOption))
					)
			);
		}

		return literal(root)
				.then(ofDefaultOptionBranch)
				.then(literal("rgb")
						.then(argument("red", IntegerArgumentType.integer(0, 255))
								.then(argument("green", IntegerArgumentType.integer(0, 255))
										.then(argument("blue", IntegerArgumentType.integer(0, 255))
												.executes(commandContext -> {
													int argb = ColorHelper.Argb.getArgb(
															IntegerArgumentType.getInteger(commandContext, "red"),
															IntegerArgumentType.getInteger(commandContext, "green"),
															IntegerArgumentType.getInteger(commandContext, "blue")
													);
													return execution.run(commandContext, argb);
												})
										)
								)
						)
				)
//				.then(literal("hex")
//						.then(argument("hex", StringArgumentType.word())
//								.executes()
//						)
//				)
				;
	}

	@FunctionalInterface
	private interface ColorExecution {
		int run(CommandContext<ServerCommandSource> context, int argb) throws CommandSyntaxException;
	}
}

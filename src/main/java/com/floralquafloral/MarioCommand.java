package com.floralquafloral;

import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.registries.RegistryManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.tom.cpm.shared.template.args.BoolArg;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarioCommand {
	public static void registerMarioCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("mario")
				.then(literal("setEnabled")
					.then(argument("enabled", BoolArgumentType.bool())
						.executes(context -> setEnabled(context, false))
						.then(argument("target", EntityArgumentType.player())
							.requires(source -> source.hasPermissionLevel(2))
							.executes(context -> setEnabled(context, true))
						)
					)
				)
				.then(literal("setAction")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("action", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
						.executes(context -> setAction(context, false))
						.then(argument("target", EntityArgumentType.player())
							.executes(context -> setAction(context, true))
						)
					)
				)
			)
		);
	}

	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback) {
		context.getSource().sendFeedback(() -> Text.literal(feedback), true);
		return 1;
	}

	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return playerArgumentGiven ? EntityArgumentType.getPlayer(context, "target") : context.getSource().getPlayerOrThrow();
	}

	private static int setEnabled(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return sendFeedback(context, MarioDataPackets.setMarioEnabled(
				getPlayerFromCmd(context, playerArgumentGiven),
				BoolArgumentType.getBool(context, "enabled")
		));
	}

	private static int setAction(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return sendFeedback(context, MarioDataPackets.forceSetMarioAction(
				getPlayerFromCmd(context, playerArgumentGiven),
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value()
		));
	}
}

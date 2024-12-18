package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.RandomSeed;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarioCommand {
	public static void registerMarioCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("mario")
				.then(literal("set")
					.then(literal("enabled")
						.then(argument("enabled", BoolArgumentType.bool())
							.requires(source -> source.hasPermissionLevel(0))
							.executes(context -> setEnabled(context, false))
							.then(argument("target", EntityArgumentType.player())
								.requires(source -> source.hasPermissionLevel(2))
								.executes(context -> setEnabled(context, true))
							)
						)
					)
					.then(literal("action")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("action", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
							.executes(context -> setAction(context, false))
							.then(argument("mario", EntityArgumentType.player())
								.executes(context -> setAction(context, true))
							)
						)
					)
					.then(literal("powerUp")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("power-up", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.POWER_UPS_KEY))
							.executes(context -> setPowerUp(context, false))
							.then(argument("mario", EntityArgumentType.player())
								.executes(context -> setPowerUp(context, true))
							)
						)
					)
					.then(literal("character")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("character", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.CHARACTERS_KEY))
							.executes(context -> setCharacter(context, false))
							.then(argument("mario", EntityArgumentType.player())
								.executes(context -> setCharacter(context, true))
							)
						)
					)
				)
				.then(literal("perform")
					.then(literal("stomp")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("stomp", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.STOMP_TYPES_KEY))
							.then(argument("goomba", EntityArgumentType.entity())
								.executes(context -> executeStomp(context, false))
								.then(argument("mario", EntityArgumentType.player())
									.executes(context -> executeStomp(context, true))
								)
							)
						)
					)
					.then(literal("bump")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> executeBump(context, false, Direction.UP, 4))
							.then(makeBumpDirectionFork(Direction.UP))
							.then(makeBumpDirectionFork(Direction.DOWN))
							.then(makeBumpDirectionFork(Direction.NORTH))
							.then(makeBumpDirectionFork(Direction.SOUTH))
							.then(makeBumpDirectionFork(Direction.EAST))
							.then(makeBumpDirectionFork(Direction.WEST))
						)
					)
					.then(literal("actionTransition")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("from", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
							.then(argument("to", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
								.executes(context -> executeActionTransition(context, false))
								.then(argument("mario", EntityArgumentType.player())
									.executes(context -> executeActionTransition(context, true))
								)
							)
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

	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, String argumentName) throws CommandSyntaxException {
		return playerArgumentGiven ? EntityArgumentType.getPlayer(context, argumentName) : context.getSource().getPlayerOrThrow();
	}
	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return getPlayerFromCmd(context, playerArgumentGiven, "mario");
	}

	private static int setEnabled(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		return sendFeedback(environment, MarioDataPackets.setMarioEnabled(
//				getPlayerFromCmd(environment, playerArgumentGiven),
//				BoolArgumentType.getBool(environment, "enabled")
//		));
		getPlayerFromCmd(context, playerArgumentGiven, "target");

		return 0;
	}

	private static int setAction(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newActionID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value().ID;
		mario.mqm$getMarioData().assignAction(newActionID);

		return sendFeedback(context, "Changed " + mario.getName().getString() + "'s action to " + newActionID + ".");
	}

	private static int setPowerUp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newPowerUpID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "power-up", RegistryManager.POWER_UPS_KEY).value().ID;
		mario.mqm$getMarioData().assignPowerUp(newPowerUpID);

		return sendFeedback(context, "Changed " + mario.getName().getString() + "'s power-up to " + newPowerUpID + ".");
	}

	private static int setCharacter(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newCharacterID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "power-up", RegistryManager.CHARACTERS_KEY).value().ID;
		mario.mqm$getMarioData().assignCharacter(newCharacterID);


		return sendFeedback(context, mario.getName().getString() + " will now play as " + newCharacterID + ".");
	}

	private static int executeStomp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		ServerPlayerEntity stomper = getPlayerFromCmd(environment, playerArgumentGiven);
//		Entity target = EntityArgumentType.getEntity(environment, "goomba");
//		ParsedStomp stompType = RegistryEntryReferenceArgumentType.getRegistryEntry(environment, "stomp", RegistryManager.STOMP_TYPES_KEY).value();
//
//		stomper.teleport((ServerWorld) target.getWorld(), target.getX(), target.getY() + target.getHeight(), target.getZ(), target.getPitch(), target.getYaw());
//		stompType.executeServer((MarioServerData) MarioDataManager.getMarioData(stomper), target, true, RandomSeed.getSeed());
//
//		return sendFeedback(environment, "Made " + stomper.getName().getString() + " perform a stomp of type " + stompType.ID + " on " + target.getName().getString());
		return 0;
	}


	private static int executeBump(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, Direction direction, Integer strength) throws CommandSyntaxException {
//		ServerPlayerEntity bumper = getPlayerFromCmd(environment, playerArgumentGiven);
//		if(strength == null) strength = IntegerArgumentType.getInteger(environment, "strength");
//		BlockPos position = BlockPosArgumentType.getBlockPos(environment, "position");
//
//		MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(bumper);
////		BumpManager.bumpBlockServer(data, bumper.getServerWorld(), position, strength, strength, direction, true, true);
////		BumpManager.bumpResponseCommon(data, data, bumper.getServerWorld(), bumper.getServerWorld().getBlockState(position), position, strength, strength, direction);
//
//		return sendFeedback(environment, "Made " + bumper.getName().getString() + " bump block " + direction + " with a strength " + strength);

		return 0;
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeBumpDirectionFork(Direction direction) {
		return literal(direction.name().toLowerCase(Locale.ROOT))
			.executes(context -> executeBump(context, false, direction, 4))
			.then(argument("strength", IntegerArgumentType.integer())
				.executes(context -> executeBump(context, false, direction, null))
				.then(argument("target", EntityArgumentType.player())
					.executes(context -> executeBump(context, true, direction, null))
				)
			);
	}

	private static int executeActionTransition(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		AbstractParsedAction fromAction =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "from", RegistryManager.ACTIONS_KEY).value();
		AbstractParsedAction toAction =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "to", RegistryManager.ACTIONS_KEY).value();

		long seed = RandomSeed.getSeed();

		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		boolean successful = mario.mqm$getMarioData().setAction(fromAction, toAction, seed, false);

		if(successful) MarioDataPackets.transitionToActionS2C(
				mario,
				true,
				fromAction,
				toAction,
				seed
		);

		return sendFeedback(context, successful ?
				"Successfully made " + mario.getName().getString() + " execute transition \"" + fromAction.ID + "->" + toAction.ID + "\"."
				: "No transition exists from " + fromAction.ID + " to " + toAction.ID + "! :("
		);
	}
}

package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.RandomSeed;

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
				.then(literal("setPowerUp")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("power", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.POWER_UPS_KEY))
						.executes(context -> setPowerUp(context, false))
						.then(argument("target", EntityArgumentType.player())
							.executes(context -> setPowerUp(context, true))
						)
					)
				)
				.then(literal("setCharacter")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("character", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.CHARACTERS_KEY))
						.executes(context -> setCharacter(context, false))
						.then(argument("target", EntityArgumentType.player())
							.executes(context -> setCharacter(context, true))
						)
					)
				)
				.then(literal("executeStomp")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("stomp", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.STOMP_TYPES_KEY))
						.then(argument("goomba", EntityArgumentType.entity())
							.executes(context -> executeStomp(context, false))
							.then(argument("target", EntityArgumentType.player())
								.executes(context -> executeStomp(context, true))
							)
						)
					)
				)
				.then(literal("executeBump")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("position", BlockPosArgumentType.blockPos())
						.executes(context -> executeBump(context, false, Direction.UP, 4))
						.then(makeBumpDirectionFork("up", Direction.UP))
						.then(makeBumpDirectionFork("down", Direction.DOWN))
						.then(makeBumpDirectionFork("north", Direction.NORTH))
						.then(makeBumpDirectionFork("south", Direction.SOUTH))
						.then(makeBumpDirectionFork("east", Direction.EAST))
						.then(makeBumpDirectionFork("west", Direction.WEST))
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
//		return sendFeedback(context, MarioDataPackets.setMarioEnabled(
//				getPlayerFromCmd(context, playerArgumentGiven),
//				BoolArgumentType.getBool(context, "enabled")
//		));
		return 0;
	}

	private static int setAction(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		return sendFeedback(context, MarioDataPackets.forceSetMarioAction(
//				getPlayerFromCmd(context, playerArgumentGiven),
//				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value()
//		));
		return 0;
	}

	private static int setPowerUp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		return sendFeedback(context, MarioDataPackets.setMarioPowerUp(
//				getPlayerFromCmd(context, playerArgumentGiven),
//				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "power", RegistryManager.POWER_UPS_KEY).value()
//		));
		return 0;
	}

	private static int setCharacter(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		return sendFeedback(context, MarioDataPackets.setMarioCharacter(
//				getPlayerFromCmd(context, playerArgumentGiven),
//				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "character", RegistryManager.CHARACTERS_KEY).value()
//		));
		return 0;
	}

	private static int executeStomp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
//		ServerPlayerEntity stomper = getPlayerFromCmd(context, playerArgumentGiven);
//		Entity target = EntityArgumentType.getEntity(context, "goomba");
//		ParsedStomp stompType = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "stomp", RegistryManager.STOMP_TYPES_KEY).value();
//
//		stomper.teleport((ServerWorld) target.getWorld(), target.getX(), target.getY() + target.getHeight(), target.getZ(), target.getPitch(), target.getYaw());
//		stompType.executeServer((MarioServerData) MarioDataManager.getMarioData(stomper), target, true, RandomSeed.getSeed());
//
//		return sendFeedback(context, "Made " + stomper.getName().getString() + " perform a stomp of type " + stompType.ID + " on " + target.getName().getString());
		return 0;
	}


	private static int executeBump(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, Direction direction, Integer strength) throws CommandSyntaxException {
//		ServerPlayerEntity bumper = getPlayerFromCmd(context, playerArgumentGiven);
//		if(strength == null) strength = IntegerArgumentType.getInteger(context, "strength");
//		BlockPos position = BlockPosArgumentType.getBlockPos(context, "position");
//
//		MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(bumper);
////		BumpManager.bumpBlockServer(data, bumper.getServerWorld(), position, strength, strength, direction, true, true);
////		BumpManager.bumpResponseCommon(data, data, bumper.getServerWorld(), bumper.getServerWorld().getBlockState(position), position, strength, strength, direction);
//
//		return sendFeedback(context, "Made " + bumper.getName().getString() + " bump block " + direction + " with a strength " + strength);

		return 0;
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeBumpDirectionFork(String name, Direction direction) {
		return literal(name)
			.executes(context -> executeBump(context, false, direction, 4))
			.then(argument("strength", IntegerArgumentType.integer())
				.executes(context -> executeBump(context, false, direction, null))
				.then(argument("target", EntityArgumentType.player())
					.executes(context -> executeBump(context, true, direction, null))
				)
			);
	}
}

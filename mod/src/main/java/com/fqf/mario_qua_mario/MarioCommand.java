package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.packets.MarioAttackInterceptionPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Locale;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarioCommand {
	public static void registerMarioCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			dispatcher.register(literal("mario")
				.then(literal("disable")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> disable(context, false))
					.then(argument("mario", EntityArgumentType.player())
							.executes(context -> disable(context, true))
					)
				)
				.then(literal("set")
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
					.then(literal("attackInterception")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("mario", EntityArgumentType.player())
							.then(makeInterceptionTypeFork(true, registryAccess))
							.then(makeInterceptionTypeFork(false, registryAccess))
						)
					)
					.then(literal("reversion")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> executeReversion(context, false))
						.then(argument("mario", EntityArgumentType.player())
							.executes(context -> executeReversion(context, true))
						)
					)
				)
			)
		);
	}

	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback, boolean successful) {
		if(successful) context.getSource().sendFeedback(() -> Text.literal(feedback), true);
		else context.getSource().sendError(Text.literal(feedback));
		return successful ? 1 : 0;
	}
	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback) {
		return sendFeedback(context, feedback, true);
	}

	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, String argumentName) throws CommandSyntaxException {
		return playerArgumentGiven ? EntityArgumentType.getPlayer(context, argumentName) : context.getSource().getPlayerOrThrow();
	}
	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return getPlayerFromCmd(context, playerArgumentGiven, "mario");
	}

	private static int disable(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		boolean success = data.isEnabled();
		data.disable();
		String name = mario.getName().getString();
		return sendFeedback(context, success ? "Disabled mod for " + name + "." : name + " is already not enabled!", success);
	}

	private static int setAction(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newActionID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value().ID;

		String name = mario.getName().getString();

		return switch(mario.mqm$getMarioData().assignAction(newActionID)) {
			case SUCCESS -> sendFeedback(context, "Changed " + name + "'s action to " + newActionID + ".");
			case NOT_ENABLED ->
					sendFeedback(context, name + " is not playing as a character, and so cannot be in an action state!", false);
		};
	}

	private static int setPowerUp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newPowerUpID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "power-up", RegistryManager.POWER_UPS_KEY).value().ID;
		MarioServerPlayerData data = mario.mqm$getMarioData();

		String name = mario.getName().getString();
		return switch(mario.mqm$getMarioData().assignPowerUp(newPowerUpID)) {
			case SUCCESS ->
					sendFeedback(context, "Changed " + name + "'s power-up to " + newPowerUpID + ".");
			case NOT_ENABLED ->
					sendFeedback(context, name + " is not playing as a character, and so cannot take on power-up forms!", false);
			case MISSING_PLAYERMODEL ->
					sendFeedback(context, name + "'s character (" + data.getCharacterID()
							+ ") does not have a playermodel for the form " + newPowerUpID + ".", false);
		};

//		return sendFeedback(context, "Changed " + mario.getName().getString() + "'s power-up to " + newPowerUpID + ".");
	}

	private static int setCharacter(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newCharacterID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "character", RegistryManager.CHARACTERS_KEY).value().ID;
		mario.mqm$getMarioData().assignCharacter(newCharacterID);

		return sendFeedback(context, mario.getName().getString() + " will now play as " + newCharacterID + ".");
	}

	private static int executeStomp(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		String name = mario.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot perform stomps.", false);

//		ServerPlayerEntity stomper = getPlayerFromCmd(environment, playerArgumentGiven);
//		Entity target = EntityArgumentType.getEntity(environment, "goomba");
//		ParsedStomp stompType = RegistryEntryReferenceArgumentType.getRegistryEntry(environment, "stomp", RegistryManager.STOMP_TYPES_KEY).value();
//
//		stomper.teleport((ServerWorld) target.getWorld(), target.getX(), target.getY() + target.getHeight(), target.getZ(), target.getPitch(), target.getYaw());
//		stompType.executeServerAndGetTargetAction((MarioServerData) MarioDataManager.getMarioData(stomper), target, true, RandomSeed.getSeed());
//
//		return sendFeedback(environment, "Made " + stomper.getName().getString() + " perform a stomp of type " + stompType.ID + " on " + target.getName().getString());

		return sendFeedback(context, "Command not yet implemented.", false);
	}


	private static int executeBump(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, Direction direction, Integer strength) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		String name = mario.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot bump blocks.", false);

//		if(strength == null) strength = IntegerArgumentType.getInteger(environment, "strength");
//		BlockPos position = BlockPosArgumentType.getBlockPos(environment, "position");
//
//		MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(bumper);
//		BumpManager.bumpBlockServer(data, bumper.getServerWorld(), position, strength, strength, direction, true, true);
//		BumpManager.bumpResponseCommon(data, data, bumper.getServerWorld(), bumper.getServerWorld().getBlockState(position), position, strength, strength, direction);
//
//		return sendFeedback(environment, "Made " + bumper.getName().getString() + " bump block " + direction + " with a strength " + strength);

		return sendFeedback(context, "Command not yet implemented.", false);
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeBumpDirectionFork(Direction direction) {
		return literal(direction.name().toLowerCase(Locale.ROOT))
			.executes(context -> executeBump(context, false, direction, 4))
			.then(argument("strength", IntegerArgumentType.integer())
				.executes(context -> executeBump(context, false, direction, null))
				.then(argument("mario", EntityArgumentType.player())
					.executes(context -> executeBump(context, true, direction, null))
				)
			);
	}

	private static int executeAttackInterception(CommandContext<ServerCommandSource> context, boolean isAction, Boolean isEntity) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, true);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		String name = mario.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot perform attack interceptions.", false);

		Entity targetEntity = null;
		BlockPos targetBlock = null;
		if(isEntity != null) {
			if(isEntity) targetEntity = EntityArgumentType.getEntity(context, "target");
			else targetBlock = BlockPosArgumentType.getValidBlockPos(context, "position");
		}

		int index = IntegerArgumentType.getInteger(context, "index");
		Identifier interceptionSourceID = null;

		try {
			if(isAction) {
				AbstractParsedAction action = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value();
				interceptionSourceID = action.ID;
				MarioAttackInterceptionPackets.handleInterceptionCommandAction(mario, action, index, targetEntity, targetBlock);
			}
			else {
				ParsedPowerUp powerUp = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "powerUp", RegistryManager.POWER_UPS_KEY).value();
				interceptionSourceID = powerUp.ID;
				MarioAttackInterceptionPackets.handleInterceptionCommandPowerUp(mario, powerUp, index, targetEntity, targetBlock);
			}
			return sendFeedback(context, "Made " + name + " perform Attack Interception number " + index + " from " + interceptionSourceID);
		}
		catch(IndexOutOfBoundsException ignored) {
			return sendFeedback(context, interceptionSourceID + " doesn't have an Attack Interception of index " + index, false);
		}
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeInterceptionTypeFork(boolean isAction, CommandRegistryAccess registryAccess) {
		String type = isAction ? "action" : "powerUp";
		return literal(type)
			.then(argument(type, actionOrPowerUpRegistryArgument(isAction, registryAccess))
				.then(argument("index", IntegerArgumentType.integer(0))
					.executes(context -> executeAttackInterception(context, isAction, null))
					.then(literal("entity")
						.then(argument("target", EntityArgumentType.entity())
							.executes(context -> executeAttackInterception(context, isAction, true))
						)
					)
					.then(literal("block")
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> executeAttackInterception(context, isAction, false))
						)
					)
				)
			);
	}
	private static RegistryEntryReferenceArgumentType<?> actionOrPowerUpRegistryArgument(boolean isAction, CommandRegistryAccess registryAccess) {
		if(isAction) return RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY);
		else return RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.POWER_UPS_KEY);
	}

	private static int executeActionTransition(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		String name = mario.getName().getString();

		AbstractParsedAction fromAction =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "from", RegistryManager.ACTIONS_KEY).value();
		AbstractParsedAction toAction =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "to", RegistryManager.ACTIONS_KEY).value();
		return switch(data.forceActionTransition(fromAction.ID, toAction.ID)) {
			case SUCCESS ->
					sendFeedback(context, "Made " + name + " execute transition \"" + fromAction.ID + "->" + toAction.ID + "\".", true);
			case NOT_ENABLED ->
					sendFeedback(context, name + " is not playing as a character, and as such cannot execute action transitions.", false);
			case NO_SUCH_TRANSITION ->
					sendFeedback(context, "No transition exists from " + fromAction.ID + " to " + toAction.ID + "! :(", false);
		};

//		if(!data.isEnabled())
//			return sendFeedback(context, name + " is not playing as a character, and as such cannot execute action transitions.", false);
//
//		AbstractParsedAction stompTypeID =
//				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "from", RegistryManager.ACTIONS_KEY).value();
//		AbstractParsedAction toAction =
//				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "to", RegistryManager.ACTIONS_KEY).value();
//
//		long seed = RandomSeed.getSeed();
//
//		boolean successful = mario.mqm$getMarioData().setAction(stompTypeID, toAction, seed, false, true);
//
//		if(successful) MarioDataPackets.transitionToActionS2C(
//				mario,
//				true,
//				stompTypeID,
//				toAction,
//				seed
//		);
//
//		return sendFeedback(context, successful ?
//				"Successfully made " + name + " execute transition \"" + stompTypeID.ID + "->" + toAction.ID + "\"."
//				: "No transition exists from " + stompTypeID.ID + " to " + toAction.ID + "! :(", successful);
	}

	private static int executeReversion(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity mario = getPlayerFromCmd(context, playerArgumentGiven);
		MarioServerPlayerData data = mario.mqm$getMarioData();
		String name = mario.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot revert forms.", false);

		Identifier formerPowerUp = data.getPowerUpID();
		IMarioAuthoritativeData.ReversionResult result = data.executeReversion();
		Identifier newPowerUp = data.getPowerUpID();

		return sendFeedback(context, switch(result) {
			case SUCCESS -> "Successfully reverted " + name + " from form " + formerPowerUp + " to " + newPowerUp + ".";
			case NO_WEAKER_FORM -> "Unable to execute reversion; " + name + "'s current power-up form (" + formerPowerUp + ") has no reversion target.";
			case MISSING_PLAYERMODEL ->
					"Unable to execute reversion; " + name + "'s current power-up (" + formerPowerUp + ") reverts into form "
					+ data.getPowerUp().REVERSION_TARGET_ID + ", for which their character (" + data.getCharacterID() + ") has no playermodel.";
			case NOT_ENABLED -> "Unable to execute reversion; " + name + " is not playing as a character.";
		}, result == IMarioAuthoritativeData.ReversionResult.SUCCESS);
	}
}

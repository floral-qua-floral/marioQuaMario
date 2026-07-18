package com.fqf.charaformact;

import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact.util.ItemStackArmorReader;
import com.fqf.charaformact_api.interfaces.BapResult;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact.packets.AttackInterceptionPackets;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact_api.util.CfaTags;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Locale;
import java.util.stream.Stream;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CharaFormActCommand {
	public static void registerCharaFormActCommand() {
		ArgumentTypeRegistry.registerArgumentType(CharaFormAct.makeID("direction"), DirectionArgumentType.class,
				ConstantArgumentSerializer.of(DirectionArgumentType::direction));
		ArgumentTypeRegistry.registerArgumentType(CharaFormAct.makeID("equipment_slot"), EquipmentSlotArgumentType.class,
				ConstantArgumentSerializer.of(EquipmentSlotArgumentType::equipmentSlot));

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralCommandNode<ServerCommandSource> literalCommandNode = dispatcher.register(literal("charaformact")
				.then(literal("disable")
					.requires(source -> source.hasPermissionLevel(2))
					.executes(context -> disable(context, false))
					.then(argument("player", EntityArgumentType.player())
						.executes(context -> disable(context, true))
					)
				)
				.then(literal("set")
					.then(literal("action")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("action", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
							.executes(context -> setAction(context, false))
							.then(argument("player", EntityArgumentType.player())
								.executes(context -> setAction(context, true))
							)
						)
					)
					.then(literal("form")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("form", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.FORMS_KEY))
							.executes(context -> setForm(context, false))
							.then(argument("player", EntityArgumentType.player())
								.executes(context -> setForm(context, true))
							)
						)
					)
					.then(literal("character")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("character", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.CHARACTERS_KEY))
							.executes(context -> setCharacter(context, false))
							.then(argument("player", EntityArgumentType.player())
								.executes(context -> setCharacter(context, true))
							)
						)
					)
				)
				.then(literal("perform")
					.then(literal("collisionAttack")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("collisionAttack", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.COLLISION_ATTACKS_KEY))
							.then(argument("attackTarget", EntityArgumentType.entity())
								.executes(context -> executeCollisionAttack(context, false))
								.then(argument("attacker", EntityArgumentType.player())
									.executes(context -> executeCollisionAttack(context, true))
								)
							)
						)
					)
					.then(literal("bap")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("position", BlockPosArgumentType.blockPos())
							.executes(context -> executeBapFromStrength(context, false, Direction.UP, 4))
							.then(makeBapDirectionFork(Direction.UP))
							.then(makeBapDirectionFork(Direction.DOWN))
							.then(makeBapDirectionFork(Direction.NORTH))
							.then(makeBapDirectionFork(Direction.SOUTH))
							.then(makeBapDirectionFork(Direction.EAST))
							.then(makeBapDirectionFork(Direction.WEST))
						)
					)
					.then(literal("actionTransition")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("from", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
							.then(argument("to", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY))
								.executes(context -> executeActionTransition(context, false))
								.then(argument("player", EntityArgumentType.player())
									.executes(context -> executeActionTransition(context, true))
								)
							)
						)
					)
					.then(literal("attackInterception")
						.requires(source -> source.hasPermissionLevel(2))
						.then(argument("player", EntityArgumentType.player())
							.then(makeInterceptionTypeFork(true, registryAccess))
							.then(makeInterceptionTypeFork(false, registryAccess))
						)
					)
					.then(literal("reversion")
						.requires(source -> source.hasPermissionLevel(2))
						.executes(context -> executeReversion(context, false))
						.then(argument("player", EntityArgumentType.player())
							.executes(context -> executeReversion(context, true))
						)
					)
				)
				.then(literal("debug")
					.then(literal("getAppearanceCoveringCounts")
						.executes(CharaFormActCommand::getAppearanceCoveringCounts)
					)
					.then(literal("equipment")
						.then(argument("slot", EquipmentSlotArgumentType.equipmentSlot())
							.then(literal("getArmor")
								.executes(CharaFormActCommand::getEquipmentAttributes)
							)
							.then(literal("getTags")
								.executes(context -> CharaFormActCommand.getEquipmentTags(context, registryAccess.getWrapperOrThrow(RegistryKeys.ITEM).streamTagKeys()))
							)
						)
					)
				)
			);
			dispatcher.register(literal("cfa").redirect(literalCommandNode));
		});
	}

	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback, int result) {
		if(result > 0) context.getSource().sendFeedback(() -> Text.literal(feedback), true);
		else context.getSource().sendError(Text.literal(feedback));
		return result;
	}
	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback, boolean successful) {
		return sendFeedback(context, feedback, successful ? 1 : 0);
	}
	private static int sendFeedback(CommandContext<ServerCommandSource> context, String feedback) {
		return sendFeedback(context, feedback, true);
	}

	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, String argumentName) throws CommandSyntaxException {
		return playerArgumentGiven ? EntityArgumentType.getPlayer(context, argumentName) : context.getSource().getPlayerOrThrow();
	}
	private static ServerPlayerEntity getPlayerFromCmd(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		return getPlayerFromCmd(context, playerArgumentGiven, "player");
	}

	private static int disable(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		CfaServerPlayerData data = player.cfa$getCfaData();
		boolean success = data.isEnabled();
		data.disable();
		String name = player.getName().getString();
		return sendFeedback(context, success ? "Disabled mod for " + name + "." : name + " is already not enabled!", success);
	}

	private static int setAction(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newActionID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "action", RegistryManager.ACTIONS_KEY).value().ID;

		String name = player.getName().getString();

		return switch(player.cfa$getCfaData().assignAction(newActionID)) {
			case SUCCESS -> sendFeedback(context, "Changed " + name + "'s action to " + newActionID + ".");
			case NOT_ENABLED ->
					sendFeedback(context, name + " is not playing as a character, and so cannot be in an action state!", false);
		};
	}

	private static int setForm(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newFormID = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "form", RegistryManager.FORMS_KEY).value().ID;
		CfaServerPlayerData data = player.cfa$getCfaData();

		String name = player.getName().getString();
		return switch(player.cfa$getCfaData().assignForm(newFormID)) {
			case SUCCESS ->
					sendFeedback(context, "Changed " + name + "'s form to " + newFormID + ".");
			case NOT_ENABLED ->
					sendFeedback(context, name + " is not playing as a character, and so cannot take on any form!", false);
			case NO_VALID_APPEARANCE ->
					sendFeedback(context, name + "'s character (" + data.getCharacterID()
							+ ") does not have any appearance for the form " + newFormID + ".", false);
		};
	}

	private static int setCharacter(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		Identifier newCharacterID =
				RegistryEntryReferenceArgumentType.getRegistryEntry(context, "character", RegistryManager.CHARACTERS_KEY).value().ID;
		player.cfa$getCfaData().assignCharacter(newCharacterID);

		return sendFeedback(context, player.getName().getString() + " will now play as " + newCharacterID + ".");
	}

	private static int executeCollisionAttack(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity attacker = getPlayerFromCmd(context, playerArgumentGiven, "attacker");
		CfaServerPlayerData data = attacker.cfa$getCfaData();
		String name = attacker.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot perform collision attacks.", false);

		return sendFeedback(context, "Command not yet implemented.", false);
	}


	private static int executeBapFromStrength(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, Direction direction, Integer strength) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		CfaServerPlayerData data = player.cfa$getCfaData();
		String name = player.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot bap blocks.", false);
		BlockPos position = BlockPosArgumentType.getBlockPos(context, "position");
		String posString = "(" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")";

		if(context.getSource().getWorld().getBlockState(position).isAir())
			return sendFeedback(context, "Block at " + posString + " is air!", false);

		if(strength == null) strength = IntegerArgumentType.getInteger(context, "strength");

		BapResult result = BlockBappingUtil.attemptBap(data, player.getWorld(), position, direction, strength, true);

		if(result == BapResult.FAIL)
			return sendFeedback(context, "Block at " + posString + " is unaffected by " + name + "'s bap.", false);

		return sendFeedback(context, "Made " + name + " bap block at position " + posString + ". Result: " + result, result.ordinal() + 1);
	}
	private static int executeBapFromResult(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven, Direction direction, BapResult result) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		CfaServerPlayerData data = player.cfa$getCfaData();
		String name = player.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot bap blocks.", false);
		BlockPos position = BlockPosArgumentType.getBlockPos(context, "position");
		String posString = "(" + position.getX() + ", " + position.getY() + ", " + position.getZ() + ")";

		if(context.getSource().getWorld().getBlockState(position).isAir())
			return sendFeedback(context, "Block at " + posString + " is air!", false);

		boolean noPower = player.getServerWorld().getBlockState(position).isIn(CfaTags.NOT_POWERED_WHEN_BAPPED);
		result = switch(result) {
			case BUMP -> noPower ? BapResult.BUMP_WITHOUT_POWERING : result;
			case BUMP_EMBRITTLE -> noPower ? BapResult.BUMP_EMBRITTLE_WITHOUT_POWERING : result;
			case BREAK -> noPower ? BapResult.BREAK_WITHOUT_POWERING : result;
			default -> result;
		};

		BlockBappingUtil.networkAndStoreBapInfo(player.getWorld(), position, direction, -1, player, result, true);

		return sendFeedback(context, "Made " + name + " do " + result + " to block at " + posString, true);
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeBapDirectionFork(Direction direction) {
		return literal(direction.name().toLowerCase(Locale.ROOT))
			.executes(context -> executeBapFromStrength(context, false, direction, 4))
			.then(literal("strength")
				.then(argument("strength", IntegerArgumentType.integer())
					.executes(context -> executeBapFromStrength(context, false, direction, null))
					.then(argument("player", EntityArgumentType.player())
						.executes(context -> executeBapFromStrength(context, true, direction, null))
					)
				)
			)
			.then(literal("result")
				.then(makeBapResultFork(direction, "bump", BapResult.BUMP))
				.then(makeBapResultFork(direction, "embrittle", BapResult.BUMP_EMBRITTLE))
				.then(makeBapResultFork(direction, "break", BapResult.BREAK))
			);
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeBapResultFork(Direction direction, String name, BapResult result) {
		return literal(name)
			.executes(context -> executeBapFromResult(context, false, direction, result))
			.then(argument("player", EntityArgumentType.player())
				.executes(context -> executeBapFromResult(context, true, direction, result))
			);
	}

	private static int executeAttackInterception(CommandContext<ServerCommandSource> context, boolean isAction, Boolean isEntity) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, true);
		CfaServerPlayerData data = player.cfa$getCfaData();
		String name = player.getName().getString();

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
				AttackInterceptionPackets.handleInterceptionCommandAction(player, action, index, targetEntity, targetBlock);
			}
			else {
				ParsedForm form = RegistryEntryReferenceArgumentType.getRegistryEntry(context, "form", RegistryManager.FORMS_KEY).value();
				interceptionSourceID = form.ID;
				AttackInterceptionPackets.handleInterceptionCommandForm(player, form, index, targetEntity, targetBlock);
			}
			return sendFeedback(context, "Made " + name + " perform Attack Interception number " + index + " from " + interceptionSourceID);
		}
		catch(IndexOutOfBoundsException ignored) {
			return sendFeedback(context, interceptionSourceID + " doesn't have an Attack Interception of index " + index, false);
		}
	}
	private static LiteralArgumentBuilder<ServerCommandSource> makeInterceptionTypeFork(boolean isAction, CommandRegistryAccess registryAccess) {
		String type = isAction ? "action" : "form";
		return literal(type)
			.then(argument(type, actionOrFormRegistryArgument(isAction, registryAccess))
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
	private static RegistryEntryReferenceArgumentType<?> actionOrFormRegistryArgument(boolean isAction, CommandRegistryAccess registryAccess) {
		if(isAction) return RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.ACTIONS_KEY);
		else return RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryManager.FORMS_KEY);
	}

	private static int executeActionTransition(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		CfaServerPlayerData data = player.cfa$getCfaData();
		String name = player.getName().getString();

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
	}

	private static int executeReversion(CommandContext<ServerCommandSource> context, boolean playerArgumentGiven) throws CommandSyntaxException {
		ServerPlayerEntity player = getPlayerFromCmd(context, playerArgumentGiven);
		CfaServerPlayerData data = player.cfa$getCfaData();
		String name = player.getName().getString();

		if(!data.isEnabled())
			return sendFeedback(context, name + " is not playing as a character, and as such cannot revert forms.", false);

		Identifier previousForm = data.getFormID();
		CfaAuthoritativeData.ReversionResult result = data.executeReversion();
		Identifier newForm = data.getFormID();

		return sendFeedback(context, switch(result) {
			case SUCCESS -> "Successfully reverted " + name + " from form " + previousForm + " to " + newForm + ".";
			case NO_WEAKER_FORM -> "Unable to execute reversion; " + name + "'s current form (" + previousForm + ") has no reversion target.";
			case NO_VALID_APPEARANCE ->
					"Unable to execute reversion; " + name + "'s current form (" + previousForm + ") reverts into "
					+ data.getForm().REVERSION_TARGET_ID + ", for which their character (" + data.getCharacterID()
					+ ") has no appearance.";
			case NOT_ENABLED -> "Unable to execute reversion; " + name + " is not playing as a character.";
		}, result == CfaAuthoritativeData.ReversionResult.SUCCESS);
	}

	private static class DirectionArgumentType extends EnumArgumentType<Direction> {
		protected DirectionArgumentType() {
			super(Direction.CODEC, Direction::values);
		}

		public static EnumArgumentType<Direction> direction() {
			return new DirectionArgumentType();
		}

		public static Direction getDirection(CommandContext<ServerCommandSource> context, String id) {
			return context.getArgument(id, Direction.class);
		}
	}

	private static class EquipmentSlotArgumentType extends EnumArgumentType<EquipmentSlot> {
		protected EquipmentSlotArgumentType() {
			super(EquipmentSlot.CODEC, EquipmentSlot::values);
		}

		public static EnumArgumentType<EquipmentSlot> equipmentSlot() {
			return new EquipmentSlotArgumentType();
		}

		public static EquipmentSlot getEquipmentSlot(CommandContext<ServerCommandSource> context, String id) {
			return context.getArgument(id, EquipmentSlot.class);
		}
	}

	private static int getAppearanceCoveringCounts(CommandContext<ServerCommandSource> context) {
		CharaFormAct.ClientHelper helper = CharaFormAct.getClientHelper();
		if(helper != null) {
			ObjectIntPair<String> covering = helper.getAppearanceCoveringInformation();
			return sendFeedback(context, "Found " + covering.rightInt() + " spots being covered" + covering.left(), covering.rightInt());
		}
		return sendFeedback(context, "Very sorry, this command is only available in Singleplayer :(", false);
	}
	private static int getEquipmentAttributes(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		EquipmentSlot slot = EquipmentSlotArgumentType.getEquipmentSlot(context, "slot");
		ItemStack equipment = player.getEquippedStack(slot);
		FloatFloatPair result = ItemStackArmorReader.getArmorAndToughness(equipment, slot);
		return sendFeedback(context, player.getName().getString() + "'s " + equipment.getItem().toString()
				+ " seems to provide " + result.leftFloat() + " armor and " + result.rightFloat() + " toughness.", (int) result.leftFloat());
	}
	private static int getEquipmentTags(CommandContext<ServerCommandSource> context, Stream<TagKey<Item>> tagKeys) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
		EquipmentSlot slot = EquipmentSlotArgumentType.getEquipmentSlot(context, "slot");
		ItemStack equipment = player.getEquippedStack(slot);

		MutableInt count = new MutableInt();
		StringBuilder output = new StringBuilder();
		tagKeys.filter(equipment::isIn).forEach(key -> {
			if(count.getAndIncrement() == 0) output.append(":\n");
			else output.append('\n');
			output.append("- ").append(key.id());
		});

		return sendFeedback(context, equipment.getItem().toString() + " is in " + count.intValue() + " tags" + output,
				count.intValue());
	}
}

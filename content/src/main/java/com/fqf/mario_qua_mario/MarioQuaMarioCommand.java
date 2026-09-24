package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.customization.CharacterCustomizationCommand;
import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import it.unimi.dsi.fastutil.objects.Object2BooleanFunction;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class MarioQuaMarioCommand {
	private static final SimpleCommandExceptionType FREEZE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.mario_qua_mario.freeze.failed"));
	private static final SimpleCommandExceptionType PERMA_FREEZE_FAILED_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("commands.mario_qua_mario.freeze.snowgrave.failed"));

	public static void registerCommand() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralCommandNode<ServerCommandSource> toadBranch = CharacterCustomizationCommand.getCustomizeToadBranch();

			LiteralCommandNode<ServerCommandSource> root = dispatcher.register(literal("marioquamario")
					.then(literal("freeze")
							.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
							.then(argument("target", EntityArgumentType.entities())
									.executes(context -> executeFreezeCommand(context, false))
									.then(literal("fatal")
											.executes(context -> executeFreezeCommand(context, true))
									)
							)
					)
					.then(toadBranch)
					.then(CharacterCustomizationCommand.getCustomizeCharacterBranch())
			);
			dispatcher.register(literal("mqm").redirect(root));
			dispatcher.register(literal("customtoad").redirect(toadBranch));
		});
	}

	@FunctionalInterface
	private interface FreezerFunction {
		boolean tryFreeze(IceFlowerFreezable freezable);
	}

	private static int executeFreezeCommand(CommandContext<ServerCommandSource> context, boolean isFatal) throws CommandSyntaxException {
		Collection<? extends Entity> targets = EntityArgumentType.getEntities(context, "target");

		int successCount = 0;
		FreezerFunction freezer = isFatal
				? freezable -> freezable.mqm$encase() && freezable.mqm$attemptFatalFreeze(null)
				: IceFlowerFreezable::mqm$encase;

		for(Entity target : targets)
			if(freezer.tryFreeze((IceFlowerFreezable) target))
				successCount++;

		if(successCount == 0) {
			throw (isFatal
					? PERMA_FREEZE_FAILED_EXCEPTION
					: FREEZE_FAILED_EXCEPTION
			).create();
		}

		if(successCount == 1) {
			Text name = targets.stream().findFirst().orElseThrow().getName();
			context.getSource().sendFeedback(() -> isFatal
							? Text.translatable("commands.mario_qua_mario.freeze.snowgrave.success.single", name)
							: Text.translatable("commands.mario_qua_mario.freeze.success.single", name),
					true);
		}
		else context.getSource().sendFeedback(() -> isFatal
						? Text.translatable("commands.mario_qua_mario.freeze.snowgrave.success.multiple", targets.size())
						: Text.translatable("commands.mario_qua_mario.freeze.success.multiple", targets.size()),
				true);

		return successCount;
	}
}

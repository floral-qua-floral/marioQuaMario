package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.action.ParsedAction;
import com.floralquafloral.registries.character.ParsedCharacter;
import com.floralquafloral.registries.powerup.ParsedPowerUp;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class MarioDataPackets {
	public static void registerCommon() {
		// S2C
		SetEnabledS2CPayload.register();
		SetActionS2CPayload.register();
		SetPowerUpS2CPayload.register();
		SetCharacterS2CPayload.register();

		// C2S
		SetActionC2SPayload.register();
		SetActionC2SPayload.registerReceiver();
	}
	public static void registerClient() {
		SetEnabledS2CPayload.registerReceiver();
		SetActionS2CPayload.registerReceiver();
		SetPowerUpS2CPayload.registerReceiver();
		SetCharacterS2CPayload.registerReceiver();
	}

	public static String setMarioEnabled(ServerPlayerEntity player, boolean enabled) {
		getMarioData(player).setEnabled(enabled);
		MarioPackets.sendPacketToTrackers(player, new MarioDataPackets.SetEnabledS2CPayload(player, enabled));

		return((enabled ? "Enabled" : "Disabled") + " Mario mode for " + player.getName().getString());
	}

	public static void setMarioAction(ServerPlayerEntity mario, ParsedAction action, long seed) {
		MarioData data = getMarioData(mario);
		boolean foundTransition = data.getAction().transitionTo((MarioPlayerData) data, action, seed);
		if(foundTransition || !mario.getWorld().getGameRules().getBoolean(MarioQuaMario.REJECT_INVALID_ACTION_TRANSITIONS)) {
			data.setActionTransitionless(action);
			MarioPackets.sendPacketToTrackersExclusive(mario, new SetActionS2CPayload(mario, action, false, seed));
		}
		else // Reject transition and forcefully set Mario back to the action we last had him on
			forceSetMarioAction(mario, data.getAction());
	}
	public static void broadcastSetMarioAction(@NotNull ParsedAction action, long seed) {
		ClientPlayNetworking.send(new SetActionC2SPayload(action, seed));
	}
	public static String forceSetMarioAction(ServerPlayerEntity mario, ParsedAction action) {
		getMarioData(mario).setActionTransitionless(action);
		MarioPackets.sendPacketToTrackers(mario, new SetActionS2CPayload(mario, action, true, 0));

		return(mario.getName().getString() + "'s Action has been set to " + action.ID);
	}

	public static String setMarioPowerUp(ServerPlayerEntity mario, ParsedPowerUp powerUp) {
		getMarioData(mario).setPowerUp(powerUp);
		MarioPackets.sendPacketToTrackers(mario, new SetPowerUpS2CPayload(mario, powerUp));

		return(mario.getName().getString() + "'s Power-up has been set to " + powerUp.ID);
	}

	public static String setMarioCharacter(ServerPlayerEntity mario, ParsedCharacter character) {
		getMarioData(mario).setCharacter(character);
		MarioPackets.sendPacketToTrackers(mario, new SetCharacterS2CPayload(mario, character));

		return(mario.getName().getString() + " has been set to play as " + character.ID);
	}

	private record SetEnabledS2CPayload(int player, boolean isMario) implements CustomPayload {
		public static final Id<SetEnabledS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_enabled"));
		public static final PacketCodec<RegistryByteBuf, SetEnabledS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetEnabledS2CPayload::player,
				PacketCodecs.BOOL, SetEnabledS2CPayload::isMario,
				SetEnabledS2CPayload::new
		);
		public SetEnabledS2CPayload(PlayerEntity player, boolean isMario) {
			this(player.getId(), isMario);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					getMarioData(context, payload.player).setEnabled(payload.isMario));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	private record SetActionS2CPayload(int mario, int newAction, boolean transitionless, long seed) implements CustomPayload {
		public static final Id<SetActionS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_action_s2c"));
		public static final PacketCodec<RegistryByteBuf, SetActionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionS2CPayload::mario,
				PacketCodecs.INTEGER, SetActionS2CPayload::newAction,
				PacketCodecs.BOOL, SetActionS2CPayload::transitionless,
				PacketCodecs.VAR_LONG, SetActionS2CPayload::seed,
				SetActionS2CPayload::new
		);
		public SetActionS2CPayload(PlayerEntity mario, ParsedAction newAction, boolean transitionless, long seed) {
			this(mario.getId(), RegistryManager.ACTIONS.getRawIdOrThrow(newAction), transitionless, seed);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioData data = getMarioData(context, payload.mario);
				ParsedAction action = RegistryManager.ACTIONS.get(payload.newAction);
				if(payload.transitionless)
					data.setActionTransitionless(action);
				else
					data.setAction(action, payload.seed);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	private record SetPowerUpS2CPayload(int player, int powerUp) implements CustomPayload {
		public static final Id<SetPowerUpS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_power_up"));
		public static final PacketCodec<RegistryByteBuf, SetPowerUpS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetPowerUpS2CPayload::player,
				PacketCodecs.INTEGER, SetPowerUpS2CPayload::powerUp,
				SetPowerUpS2CPayload::new
		);
		public SetPowerUpS2CPayload(PlayerEntity player, ParsedPowerUp powerUp) {
			this(player.getId(), RegistryManager.POWER_UPS.getRawIdOrThrow(powerUp));
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					getMarioData(context, payload.player).setPowerUp(RegistryManager.POWER_UPS.get(payload.powerUp)));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	private record SetCharacterS2CPayload(int player, int character) implements CustomPayload {
		public static final Id<SetCharacterS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_character"));
		public static final PacketCodec<RegistryByteBuf, SetCharacterS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetCharacterS2CPayload::player,
				PacketCodecs.INTEGER, SetCharacterS2CPayload::character,
				SetCharacterS2CPayload::new
		);
		public SetCharacterS2CPayload(PlayerEntity player, ParsedCharacter character) {
			this(player.getId(), RegistryManager.CHARACTERS.getRawIdOrThrow(character));
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					getMarioData(context, payload.player).setCharacter(RegistryManager.CHARACTERS.get(payload.character)));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	private record SetActionC2SPayload(int newAction, long seed) implements CustomPayload {
		public static final Id<SetActionC2SPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_action_c2s"));
		public static final PacketCodec<RegistryByteBuf, SetActionC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionC2SPayload::newAction,
				PacketCodecs.VAR_LONG, SetActionC2SPayload::seed,
				SetActionC2SPayload::new
		);
		public SetActionC2SPayload(@NotNull ParsedAction newAction, long seed) {
			this(RegistryManager.ACTIONS.getRawIdOrThrow(newAction), seed);
			MarioQuaMario.LOGGER.info("Made new SetActionC2S packet");
		}
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					setMarioAction(context.player(), RegistryManager.ACTIONS.get(payload.newAction), payload.seed));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}
}

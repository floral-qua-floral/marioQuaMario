package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

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

		// Listeners
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sendAllData(handler.player, handler.player);
			ServerPlayNetworking.send(handler.player,
					new MarioPackets.SyncUseCharacterStatsS2CPayload(
							handler.player.getWorld().getGameRules().getBoolean(MarioQuaMario.USE_CHARACTER_STATS)));
		});

		EntityTrackingEvents.START_TRACKING.register((entityBeingTracked, tracker) -> {
			if(entityBeingTracked instanceof PlayerEntity playerBeingTracked) {
				sendAllData(tracker, playerBeingTracked);
			}
		});
	}
	public static void registerClient() {
		SetEnabledS2CPayload.registerReceiver();
		SetActionS2CPayload.registerReceiver();
		SetPowerUpS2CPayload.registerReceiver();
		SetCharacterS2CPayload.registerReceiver();
	}

	public static void sendAllData(ServerPlayerEntity toWho, PlayerEntity aboutWho) {
		MarioPlayerData data = getMarioData(aboutWho);
		// am I supposed to send one packet with all this data or is this fine????
		ServerPlayNetworking.send(toWho, new SetEnabledS2CPayload(aboutWho, data.isEnabled()));
		ServerPlayNetworking.send(toWho, new SetActionS2CPayload(aboutWho, data.getAction(), true, 0));
		ServerPlayNetworking.send(toWho, new SetPowerUpS2CPayload(aboutWho, data.getPowerUp(), true));
		ServerPlayNetworking.send(toWho, new SetCharacterS2CPayload(aboutWho, data.getCharacter()));
	}

	public static String setMarioEnabled(ServerPlayerEntity player, boolean enabled) {
		getMarioData(player).setEnabledInternal(enabled);
		MarioPackets.sendPacketToTrackers(player, new MarioDataPackets.SetEnabledS2CPayload(player, enabled));

		return((enabled ? "Enabled" : "Disabled") + " Mario mode for " + player.getName().getString());
	}

	public static boolean setMarioAction(ServerPlayerEntity mario, ParsedAction action, long seed, boolean networkToMario) {
		MarioServerData data = (MarioServerData) getMarioData(mario);
		boolean foundTransition = data.getAction().transitionTo(data, action, seed);
		data.applyModifiedVelocity();
		if(foundTransition || !mario.getWorld().getGameRules().getBoolean(MarioQuaMario.REJECT_INVALID_ACTION_TRANSITIONS)) {
			data.setActionTransitionless(action);
			SetActionS2CPayload payload = new SetActionS2CPayload(mario, action, false, seed);
			MarioPackets.sendPacketToTrackersExclusive(mario, payload);
			if(networkToMario) ServerPlayNetworking.send(mario, payload);
		}
		else // Reject transition and forcefully set Mario back to the action we last had him on
			forceSetMarioAction(mario, data.getAction());

		return foundTransition;
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
		MarioPackets.sendPacketToTrackers(mario, new SetPowerUpS2CPayload(mario, powerUp, false));

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
					getMarioData(context, payload.player).setEnabledInternal(payload.isMario));
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
				MarioPlayerData data = getMarioData(context, payload.mario);
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

	private record SetPowerUpS2CPayload(int player, int powerUp, boolean transitionless) implements CustomPayload {
		public static final Id<SetPowerUpS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_power_up"));
		public static final PacketCodec<RegistryByteBuf, SetPowerUpS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetPowerUpS2CPayload::player,
				PacketCodecs.INTEGER, SetPowerUpS2CPayload::powerUp,
				PacketCodecs.BOOL, SetPowerUpS2CPayload::transitionless,
				SetPowerUpS2CPayload::new
		);
		public SetPowerUpS2CPayload(PlayerEntity player, ParsedPowerUp powerUp, boolean transitionless) {
			this(player.getId(), RegistryManager.POWER_UPS.getRawIdOrThrow(powerUp), transitionless);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					getMarioData(context, payload.player).setPowerUp(Objects.requireNonNull(RegistryManager.POWER_UPS.get(payload.powerUp))));
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
		}
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					setMarioAction(context.player(), RegistryManager.ACTIONS.get(payload.newAction), payload.seed, false));
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}
}

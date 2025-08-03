package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioDataPackets {
	public static void disableMarioS2C(ServerPlayerEntity mario) {
		MarioPackets.sendToTrackers(
				mario,
				new DisableMarioS2CPayload(mario.getId()),
				true
		);
	}

	public static void transitionToActionS2C(
			ServerPlayerEntity mario, boolean networkToMario,
			AbstractParsedAction fromAction, AbstractParsedAction toAction,
			long seed
	) {
		MarioPackets.sendToTrackers(
				mario,
				new ActionTransitionS2CPayload(mario.getId(), fromAction.getIntID(), toAction.getIntID(), seed),
				networkToMario
		);
	}

	public static void assignActionS2C(
			ServerPlayerEntity mario, boolean networkToMario, AbstractParsedAction newAction
	) {
		MarioPackets.sendToTrackers(
				mario,
				new AssignActionS2CPayload(mario.getId(), newAction.getIntID()),
				networkToMario
		);
	}

	public static void empowerRevertS2C(
			ServerPlayerEntity mario, ParsedPowerUp toPower, boolean isReversion, long seed
	) {
		MarioPackets.sendToTrackers(
				mario,
				new EmpowerRevertS2CPayload(mario.getId(), RegistryManager.POWER_UPS.getRawIdOrThrow(toPower), isReversion, seed),
				true
		);
	}

	public static void assignPowerUpS2C(
			ServerPlayerEntity mario, ParsedPowerUp newPowerUp
	) {
		MarioPackets.sendToTrackers(
				mario,
				new AssignPowerUpS2CPayload(mario.getId(), RegistryManager.POWER_UPS.getRawIdOrThrow(newPowerUp)),
				true
		);
	}

	public static void assignCharacterS2C(
			ServerPlayerEntity mario, ParsedCharacter newCharacter
	) {
		MarioPackets.sendToTrackers(
				mario,
				new AssignCharacterS2CPayload(mario.getId(), RegistryManager.CHARACTERS.getRawIdOrThrow(newCharacter)),
				true
		);
	}

	public static void syncMarioDataToPlayerS2C(
			ServerPlayerEntity mario, ServerPlayerEntity syncTo
	) {
		MarioServerPlayerData data = mario.mqm$getMarioData();
		if(!data.isEnabled()) return;
		ServerPlayNetworking.send(syncTo, new SyncMarioDataS2CPayload(
				mario.getId(),
				RegistryManager.CHARACTERS.getRawIdOrThrow(data.getCharacter()),
				RegistryManager.POWER_UPS.getRawIdOrThrow(data.getPowerUp()),
				RegistryManager.ACTIONS.getRawIdOrThrow(data.getAction())
		));
	}

	public static void transmitWallYawS2C(
			ServerPlayerEntity mario, float wallYaw
	) {
		MarioPackets.sendToTrackers(mario, new TransmitWallYawS2CPayload(mario.getId(), wallYaw), false);
	}

	protected record DisableMarioS2CPayload(int marioID) implements CustomPayload {
		public static final Id<DisableMarioS2CPayload> ID = MarioPackets.makeID("disable_mario_s2c");
		public static final PacketCodec<RegistryByteBuf, DisableMarioS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, DisableMarioS2CPayload::marioID,
				DisableMarioS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record SetActionC2SPayload(int fromAction, int toAction, long seed) implements CustomPayload {
		public static final Id<SetActionC2SPayload> ID = MarioPackets.makeID("set_action_c2s");
		public static final PacketCodec<RegistryByteBuf, SetActionC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionC2SPayload::fromAction,
				PacketCodecs.INTEGER, SetActionC2SPayload::toAction,
				PacketCodecs.VAR_LONG, SetActionC2SPayload::seed,
				SetActionC2SPayload::new
		);

		public static void receive(SetActionC2SPayload payload, ServerPlayNetworking.Context context) {
			AbstractParsedAction fromAction = ParsedActionHelper.get(payload.fromAction());
			AbstractParsedAction toAction = ParsedActionHelper.get(payload.toAction());
//			MarioQuaMario.LOGGER.info("Received setActionC2S: {}->{}", stompTypeID.ID, toAction.ID);
			boolean rejectInvalid = context.player().getWorld().getGameRules().getBoolean(MarioGamerules.REJECT_INVALID_ACTION_TRANSITIONS);
			if(context.player().mqm$getMarioData().setAction(fromAction, toAction, payload.seed, !rejectInvalid, false)) {
				MarioPackets.sendToTrackers(context.player(), new ActionTransitionS2CPayload(
						context.player().getId(),
						payload.fromAction,
						payload.toAction,
						payload.seed
				), false);
			}
			else {
				// Reject the transition and instead tell Mario to go back to the action we think he's in
				ServerPlayNetworking.send(context.player(), new AssignActionS2CPayload(
						context.player().getId(), context.player().mqm$getMarioData().getAction().getIntID()
				));
			}
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, SetActionC2SPayload::receive);
		}
	}

	protected record ActionTransitionS2CPayload(int marioID, int fromAction, int toAction, long seed) implements CustomPayload {
		public static final Id<ActionTransitionS2CPayload> ID = MarioPackets.makeID("set_action_s2c");
		public static final PacketCodec<RegistryByteBuf, ActionTransitionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ActionTransitionS2CPayload::marioID,
				PacketCodecs.INTEGER, ActionTransitionS2CPayload::fromAction,
				PacketCodecs.INTEGER, ActionTransitionS2CPayload::toAction,
				PacketCodecs.VAR_LONG, ActionTransitionS2CPayload::seed,
				ActionTransitionS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record AssignActionS2CPayload(int marioID, int newAction) implements CustomPayload {
		public static final Id<AssignActionS2CPayload> ID = MarioPackets.makeID("set_action_transitionless_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignActionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignActionS2CPayload::marioID,
				PacketCodecs.INTEGER, AssignActionS2CPayload::newAction,
				AssignActionS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record EmpowerRevertS2CPayload(int marioID, int toPower, boolean isReversion, long seed) implements CustomPayload {
		public static final Id<EmpowerRevertS2CPayload> ID = MarioPackets.makeID("empower_s2c");
		public static final PacketCodec<RegistryByteBuf, EmpowerRevertS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, EmpowerRevertS2CPayload::marioID,
				PacketCodecs.INTEGER, EmpowerRevertS2CPayload::toPower,
				PacketCodecs.BOOL, EmpowerRevertS2CPayload::isReversion,
				PacketCodecs.VAR_LONG, EmpowerRevertS2CPayload::seed,
				EmpowerRevertS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record AssignPowerUpS2CPayload(int marioID, int newPower) implements CustomPayload {
		public static final Id<AssignPowerUpS2CPayload> ID = MarioPackets.makeID("assign_power_up_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignPowerUpS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignPowerUpS2CPayload::marioID,
				PacketCodecs.INTEGER, AssignPowerUpS2CPayload::newPower,
				AssignPowerUpS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record AssignCharacterS2CPayload(int marioID, int newCharacter) implements CustomPayload {
		public static final Id<AssignCharacterS2CPayload> ID = MarioPackets.makeID("assign_character_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignCharacterS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignCharacterS2CPayload::marioID,
				PacketCodecs.INTEGER, AssignCharacterS2CPayload::newCharacter,
				AssignCharacterS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record SyncMarioDataS2CPayload(int marioID, int character, int powerUp, int action) implements CustomPayload {
		public static final Id<SyncMarioDataS2CPayload> ID = MarioPackets.makeID("sync_mario_data_s2c");
		public static final PacketCodec<RegistryByteBuf, SyncMarioDataS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SyncMarioDataS2CPayload::marioID,
				PacketCodecs.INTEGER, SyncMarioDataS2CPayload::character,
				PacketCodecs.INTEGER, SyncMarioDataS2CPayload::powerUp,
				PacketCodecs.INTEGER, SyncMarioDataS2CPayload::action,
				SyncMarioDataS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record TransmitWallYawC2SPayload(float yaw) implements CustomPayload {
		public static final Id<TransmitWallYawC2SPayload> ID = MarioPackets.makeID("transmit_wall_yaw_c2s");
		public static final PacketCodec<RegistryByteBuf, TransmitWallYawC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.FLOAT, TransmitWallYawC2SPayload::yaw,
				TransmitWallYawC2SPayload::new
		);

		public static void receive(TransmitWallYawC2SPayload payload, ServerPlayNetworking.Context context) {
			context.player().mqm$getMarioData().receiveWallYaw(payload.yaw());
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, TransmitWallYawC2SPayload::receive);
		}
	}

	protected record TransmitWallYawS2CPayload(int marioID, float yaw) implements CustomPayload {
		public static final Id<TransmitWallYawS2CPayload> ID = MarioPackets.makeID("transmit_wall_yaw_s2c");
		public static final PacketCodec<RegistryByteBuf, TransmitWallYawS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, TransmitWallYawS2CPayload::marioID,
				PacketCodecs.FLOAT, TransmitWallYawS2CPayload::yaw,
				TransmitWallYawS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}

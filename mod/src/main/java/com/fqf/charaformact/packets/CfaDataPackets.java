package com.fqf.charaformact.packets;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact.util.CfaGamerules;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class CfaDataPackets {
	public static void setNoCharacterS2C(ServerPlayerEntity player) {
		CfaPackets.sendToTrackers(
				player,
				new SetNoCharacterS2CPayload(player.getId()),
				true
		);
	}

	public static void transitionToActionS2C(
			ServerPlayerEntity player, boolean networkToTransitioner,
			AbstractParsedAction fromAction, AbstractParsedAction toAction,
			long seed
	) {
		CfaPackets.sendToTrackers(
				player,
				new ActionTransitionS2CPayload(player.getId(), fromAction.getIntID(), toAction.getIntID(), seed),
				networkToTransitioner
		);
	}

	public static void assignActionS2C(
			ServerPlayerEntity player, boolean networkToTransitioner, AbstractParsedAction newAction
	) {
		CfaPackets.sendToTrackers(
				player,
				new AssignActionS2CPayload(player.getId(), newAction.getIntID()),
				networkToTransitioner
		);
	}

	public static void empowerRevertS2C(
			ServerPlayerEntity player, ParsedForm toPower, boolean isReversion, long seed
	) {
		CfaPackets.sendToTrackers(
				player,
				new EmpowerRevertS2CPayload(player.getId(), RegistryManager.FORMS.getRawIdOrThrow(toPower), isReversion, seed),
				true
		);
	}

	public static void assignFormS2C(
			ServerPlayerEntity player, ParsedForm newForm
	) {
		CfaPackets.sendToTrackers(
				player,
				new AssignFormS2CPayload(player.getId(), RegistryManager.FORMS.getRawIdOrThrow(newForm)),
				true
		);
	}

	public static void assignCharacterS2C(
			ServerPlayerEntity player, ParsedCharacter newCharacter
	) {
		CfaPackets.sendToTrackers(
				player,
				new AssignCharacterS2CPayload(player.getId(), RegistryManager.CHARACTERS.getRawIdOrThrow(newCharacter)),
				true
		);
	}

	public static void syncCfaDataToPlayerS2C(
			ServerPlayerEntity player, ServerPlayerEntity syncTo
	) {
		CfaServerPlayerData data = player.cfa$getCfaData();
		if(!data.isEnabled()) return;
		ServerPlayNetworking.send(syncTo, new SyncCfaDataS2CPayload(
				player.getId(),
				RegistryManager.CHARACTERS.getRawIdOrThrow(data.getCharacter()),
				RegistryManager.FORMS.getRawIdOrThrow(data.getForm()),
				RegistryManager.ACTIONS.getRawIdOrThrow(data.getAction())
		));
	}

	public static void transmitWallYawS2C(
			ServerPlayerEntity player, float wallYaw
	) {
		CfaPackets.sendToTrackers(player, new TransmitWallYawS2CPayload(player.getId(), wallYaw), false);
	}

	protected record SetNoCharacterS2CPayload(int playerID) implements CustomPayload {
		public static final Id<SetNoCharacterS2CPayload> ID = CfaPackets.makeID("set_no_character_s2c");
		public static final PacketCodec<RegistryByteBuf, SetNoCharacterS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetNoCharacterS2CPayload::playerID,
				SetNoCharacterS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record SetActionC2SPayload(int fromAction, int toAction, long seed) implements CustomPayload {
		public static final Id<SetActionC2SPayload> ID = CfaPackets.makeID("set_action_c2s");
		public static final PacketCodec<RegistryByteBuf, SetActionC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionC2SPayload::fromAction,
				PacketCodecs.INTEGER, SetActionC2SPayload::toAction,
				PacketCodecs.VAR_LONG, SetActionC2SPayload::seed,
				SetActionC2SPayload::new
		);

		public static void receive(SetActionC2SPayload payload, ServerPlayNetworking.Context context) {
			AbstractParsedAction fromAction = ParsedActionHelper.get(payload.fromAction());
			AbstractParsedAction toAction = ParsedActionHelper.get(payload.toAction());
//			CharaFormAct.LOGGER.info("Received setActionC2S: {}->{}", collisionAttackID.ID, toAction.ID);
			boolean rejectInvalid = context.player().getWorld().getGameRules().getBoolean(CfaGamerules.REJECT_INVALID_ACTION_TRANSITIONS)
					&& !(CharaFormAct.CONFIG.shouldAllowIllegalTransitionsInSingleplayer() && Objects.requireNonNull(context.player().getServer()).isHost(context.player().getGameProfile()));
			if(context.player().cfa$getCfaData().setAction(fromAction, toAction, payload.seed, !rejectInvalid, false)) {
				CfaPackets.sendToTrackers(context.player(), new ActionTransitionS2CPayload(
						context.player().getId(),
						payload.fromAction,
						payload.toAction,
						payload.seed
				), false);
			}
			else {
				// Reject the transition and instead tell the player to go back to the action we think she's in
				ServerPlayNetworking.send(context.player(), new AssignActionS2CPayload(
						context.player().getId(), context.player().cfa$getCfaData().getAction().getIntID()
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

	protected record ActionTransitionS2CPayload(int playerID, int fromAction, int toAction, long seed) implements CustomPayload {
		public static final Id<ActionTransitionS2CPayload> ID = CfaPackets.makeID("set_action_s2c");
		public static final PacketCodec<RegistryByteBuf, ActionTransitionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, ActionTransitionS2CPayload::playerID,
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

	protected record AssignActionS2CPayload(int playerID, int newAction) implements CustomPayload {
		public static final Id<AssignActionS2CPayload> ID = CfaPackets.makeID("set_action_transitionless_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignActionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignActionS2CPayload::playerID,
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

	protected record EmpowerRevertS2CPayload(int playerID, int toPower, boolean isReversion, long seed) implements CustomPayload {
		public static final Id<EmpowerRevertS2CPayload> ID = CfaPackets.makeID("empower_s2c");
		public static final PacketCodec<RegistryByteBuf, EmpowerRevertS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, EmpowerRevertS2CPayload::playerID,
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

	protected record AssignFormS2CPayload(int playerID, int newPower) implements CustomPayload {
		public static final Id<AssignFormS2CPayload> ID = CfaPackets.makeID("assign_form_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignFormS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignFormS2CPayload::playerID,
				PacketCodecs.INTEGER, AssignFormS2CPayload::newPower,
				AssignFormS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record AssignCharacterS2CPayload(int playerID, int newCharacter) implements CustomPayload {
		public static final Id<AssignCharacterS2CPayload> ID = CfaPackets.makeID("assign_character_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignCharacterS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, AssignCharacterS2CPayload::playerID,
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

	protected record SyncCfaDataS2CPayload(int playerID, int character, int form, int action) implements CustomPayload {
		public static final Id<SyncCfaDataS2CPayload> ID = CfaPackets.makeID("sync_cfa_data_s2c");
		public static final PacketCodec<RegistryByteBuf, SyncCfaDataS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SyncCfaDataS2CPayload::playerID,
				PacketCodecs.INTEGER, SyncCfaDataS2CPayload::character,
				PacketCodecs.INTEGER, SyncCfaDataS2CPayload::form,
				PacketCodecs.INTEGER, SyncCfaDataS2CPayload::action,
				SyncCfaDataS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record TransmitWallYawC2SPayload(float yaw) implements CustomPayload {
		public static final Id<TransmitWallYawC2SPayload> ID = CfaPackets.makeID("transmit_wall_yaw_c2s");
		public static final PacketCodec<RegistryByteBuf, TransmitWallYawC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.FLOAT, TransmitWallYawC2SPayload::yaw,
				TransmitWallYawC2SPayload::new
		);

		public static void receive(TransmitWallYawC2SPayload payload, ServerPlayNetworking.Context context) {
			context.player().cfa$getCfaData().receiveWallYaw(payload.yaw());
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, TransmitWallYawC2SPayload::receive);
		}
	}

	protected record TransmitWallYawS2CPayload(int playerID, float yaw) implements CustomPayload {
		public static final Id<TransmitWallYawS2CPayload> ID = CfaPackets.makeID("transmit_wall_yaw_s2c");
		public static final PacketCodec<RegistryByteBuf, TransmitWallYawS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, TransmitWallYawS2CPayload::playerID,
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

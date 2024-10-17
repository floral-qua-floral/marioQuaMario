package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.action.ParsedAction;
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

import static com.floralquafloral.mariodata.MarioDataManager.getMarioData;

public class MarioDataPackets {
	public static void registerCommon() {
		// S2C
		SetEnabledS2CPayload.register();
		SetActionS2CPayload.register();

		// C2S
		SetActionC2SPayload.register();
		SetActionC2SPayload.registerReceiver();
	}
	public static void registerClient() {
		SetEnabledS2CPayload.registerReceiver();
		SetActionS2CPayload.registerReceiver();
	}

	public static String setMarioEnabled(ServerPlayerEntity player, boolean enabled) {
		getMarioData(player).setEnabled(enabled);
		MarioPackets.sendPacketToTrackers(player, new MarioDataPackets.SetEnabledS2CPayload(player, enabled));

		return((enabled ? "Enabled" : "Disabled") + " Mario mode for " + player.getName().getString());
	}

	public static void setMarioAction(ServerPlayerEntity mario, ParsedAction action) {
		getMarioData(mario).setAction(action);
		MarioPackets.sendPacketToTrackersExclusive(mario, new SetActionS2CPayload(mario, action, false));
	}
	public static void broadcastSetMarioAction(ParsedAction action) {
		ClientPlayNetworking.send(new SetActionC2SPayload(action));
	}
	public static String forceSetMarioAction(ServerPlayerEntity mario, ParsedAction action) {
		getMarioData(mario).setAction(action);
		MarioPackets.sendPacketToTrackers(mario, new SetActionS2CPayload(mario, action, true));

		return("Set " + mario.getName().getString() + "'s Action has been set to " + action.ID);
	}

	public record SetEnabledS2CPayload(int player, boolean isMario) implements CustomPayload {
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

	public record SetActionS2CPayload(int mario, int newAction, boolean transitionless) implements CustomPayload {
		public static final Id<SetActionS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_action_s2c"));
		public static final PacketCodec<RegistryByteBuf, SetActionS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionS2CPayload::mario,
				PacketCodecs.INTEGER, SetActionS2CPayload::newAction,
				PacketCodecs.BOOL, SetActionS2CPayload::transitionless,
				SetActionS2CPayload::new
		);
		public SetActionS2CPayload(PlayerEntity mario, ParsedAction newAction, boolean transitionless) {
			this(mario.getId(), RegistryManager.ACTIONS.getRawIdOrThrow(newAction), transitionless);
		}
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioData data = getMarioData(context, payload.mario);
				ParsedAction action = RegistryManager.ACTIONS.get(payload.newAction);
				if(payload.transitionless)
					data.setActionTransitionless(action);
				else
					data.setAction(action);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	public record SetActionC2SPayload(int newAction) implements CustomPayload {
		public static final Id<SetActionC2SPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "set_action_c2s"));
		public static final PacketCodec<RegistryByteBuf, SetActionC2SPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, SetActionC2SPayload::newAction,
				SetActionC2SPayload::new
		);
		public SetActionC2SPayload(ParsedAction newAction) {
			this(RegistryManager.ACTIONS.getRawIdOrThrow(newAction));
		}
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
				setMarioAction(context.player(), RegistryManager.ACTIONS.get(payload.newAction))
			);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}
}

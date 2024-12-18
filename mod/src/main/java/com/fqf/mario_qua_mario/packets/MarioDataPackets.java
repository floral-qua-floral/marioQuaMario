package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioDataPackets {
	public static void setActionS2C(
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

	public static void setActionTransitionlessS2C(
			ServerPlayerEntity mario, boolean networkToMario, AbstractParsedAction newAction
	) {
		MarioPackets.sendToTrackers(
				mario,
				new AssignActionS2CPayload(mario.getId(), newAction.getIntID()),
				networkToMario
		);
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
			AbstractParsedAction fromAction = ParsedActionHelper.get(payload.toAction());
			AbstractParsedAction toAction = ParsedActionHelper.get(payload.toAction());
			if(context.player().mqm$getMarioData().setAction(fromAction, toAction, payload.seed, false)) {
				MarioPackets.sendToTrackers(context.player(), new ActionTransitionS2CPayload(
						context.player().getId(),
						payload.fromAction,
						payload.toAction,
						payload.seed
				), false);
			}
			else {
				// Reject the transition and instead tell Mario to go back to the state we think he's in
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

	protected record EmpowerS2CPayload(int toPower, long seed) implements CustomPayload {
		public static final Id<EmpowerS2CPayload> ID = MarioPackets.makeID("empower_s2c");
		public static final PacketCodec<RegistryByteBuf, EmpowerS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, EmpowerS2CPayload::toPower,
				PacketCodecs.VAR_LONG, EmpowerS2CPayload::seed,
				EmpowerS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record RevertS2CPayload(int toPower, long seed) implements CustomPayload {
		public static final Id<RevertS2CPayload> ID = MarioPackets.makeID("revert_s2c");
		public static final PacketCodec<RegistryByteBuf, RevertS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, RevertS2CPayload::toPower,
				PacketCodecs.VAR_LONG, RevertS2CPayload::seed,
				RevertS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record AssignPowerUpS2CPayload(int newPower) implements CustomPayload {
		public static final Id<AssignPowerUpS2CPayload> ID = MarioPackets.makeID("assign_power_up_s2c");
		public static final PacketCodec<RegistryByteBuf, AssignPowerUpS2CPayload> CODEC = PacketCodec.tuple(
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
}

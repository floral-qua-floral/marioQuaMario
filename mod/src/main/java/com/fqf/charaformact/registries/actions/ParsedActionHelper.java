package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.packets.CfaDataPackets;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact_api.definitions.states.actions.*;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.parsed.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class ParsedActionHelper {
	public static AbstractParsedAction parseAction(Identifier id, IncompleteActionDefinition definition) {
		return switch (definition) {
			case GenericActionDefinition def -> new ParsedGenericAction(id, def);
			case GroundedActionDefinition def -> new ParsedGroundedAction(id, def);
			case AirborneActionDefinition def -> new ParsedAirborneAction(id, def);
			case AquaticActionDefinition def -> new ParsedAquaticAction(id, def);
			case WallboundActionDefinition def -> new ParsedWallboundAction(id, def);
			case MountedActionDefinition def -> new ParsedMountedAction(id, def);
			default -> throw new AssertionError("Action Definition wasn't one of the known types?!?!");
		};
	}

	public static void attemptTransitions(CfaMoveableData data, TransitionPhase phase) {
//		CharaFormAct.LOGGER.info("Start checking on {}:", data.isClient() ? "CLIENT": "SERVER");
		TransitionPhase usePhase = phase == TransitionPhase.WORLD_COLLISION_EARLY ? TransitionPhase.WORLD_COLLISION : phase;
		for(ParsedTransition transition : data.isClient() ? data.getAction().CLIENT_TRANSITIONS.get(usePhase) : data.getAction().SERVER_TRANSITIONS.get(usePhase)) {
//			if(Objects.equals(data.getActionID(), CharaFormAct.makeID("jump")))
//				CharaFormAct.LOGGER.info("Testing transition from {}->{}:\n{}", data.getActionID(), transition.targetAction().ID, transition.evaluator().shouldTransition(data));
			if(transition.evaluator().shouldTransition(data)) {
				long seed = data.getPlayer().getRandom().nextLong();

				if(data.isServer()) {
					CfaDataPackets.transitionToActionS2C((ServerPlayerEntity) data.getPlayer(), transition.fullyNetworked(),
							data.getAction(), transition.targetAction(), seed);
				}
				else {
					if(transition.targetAction().CATEGORY == ActionCategory.WALLBOUND) {
						ParsedWallboundAction wallAction = (ParsedWallboundAction) transition.targetAction();
						float wallYaw = wallAction.getWallYaw(data);
						data.getWallInfo().setYaw(wallYaw);

						if(!wallAction.verifyWallLegality(data, Vec3d.ZERO)) {
//							CharaFormAct.LOGGER.info("Client is cancelling successful wallbound transition without networking " +
//									"because legality check failed ({} -> {})", data.getActionID(), wallAction.ID);
							continue;
						}

						CfaClientHelperManager.packetSender.transmitWallYawC2S(data, wallYaw);
					}

					CfaClientHelperManager.packetSender.conditionallySaveTransitionToReplayMod(data.getAction(), transition.targetAction(), seed);
					if (transition.fullyNetworked()) {
						CfaClientHelperManager.packetSender.setActionC2S(data.getAction(), transition.targetAction(), seed, phase);
					}
				}

				executeTransition(data, transition, seed);
				data.handleInputUnbuffering(true);
				return;
			}
			else
				data.handleInputUnbuffering(false);
		}
	}

	public static boolean attemptTransitionTo(CfaPlayerData data, AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
		ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
		if(transition == null) return false;

		executeTransition(data, transition, seed);
		return true;
	}

	public static void executeTransition(CfaPlayerData data, ParsedTransition transition, long seed) {
		if(CharaFormAct.CONFIG.logAllActionTransitions()) CharaFormAct.LOGGER.info("Executing transition for {} on {}: {} -> {}",
				data.getPlayer().getName().getString(), data.isClient() ? "CLIENT" : "SERVER",
				data.getActionID(), transition.targetAction().ID
		);

		if(data instanceof CfaMoveableData moveableData && transition.travelExecutor() != null)
			transition.travelExecutor().execute(moveableData);
		if(data instanceof CfaClientData clientData && transition.clientsExecutor() != null)
			transition.clientsExecutor().execute(clientData, data.getPlayer().isMainPlayer(), seed);

		data.setActionTransitionless(transition.targetAction());
	}

	public static AbstractParsedAction get(int ID) {
		return RegistryManager.ACTIONS.get(ID);
	}
}

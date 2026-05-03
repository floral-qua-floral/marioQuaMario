package com.fqf.charapoweract.registries.actions;

import com.fqf.charapoweract.cpadata.CPAMoveableData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract.util.MarioClientHelperManager;
import com.fqf.charapoweract.MarioQuaMario;
import com.fqf.charapoweract_api.definitions.states.actions.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract.packets.MarioDataPackets;
import com.fqf.charapoweract.registries.RegistryManager;
import com.fqf.charapoweract.registries.actions.parsed.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Set;

public class ParsedActionHelper {
	public static AbstractParsedAction parseAction(
			IncompleteActionDefinition definition,
			HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections
	) {
		return switch (definition) {
			case GenericActionDefinition def -> new ParsedGenericAction(def, allInjections);
			case GroundedActionDefinition def -> new ParsedGroundedAction(def, allInjections);
			case AirborneActionDefinition def -> new ParsedAirborneAction(def, allInjections);
			case AquaticActionDefinition def -> new ParsedAquaticAction(def, allInjections);
			case WallboundActionDefinition def -> new ParsedWallboundAction(def, allInjections);
			case MountedActionDefinition def -> new ParsedMountedAction(def, allInjections);
			default -> throw new AssertionError("Action Definition wasn't one of the known types?!?!");
		};
	}

	public static void attemptTransitions(CPAMoveableData data, TransitionPhase phase) {
//		MarioQuaMario.LOGGER.info("Start checking on {}:", data.isClient() ? "CLIENT": "SERVER");
		TransitionPhase usePhase = phase == TransitionPhase.WORLD_COLLISION_EARLY ? TransitionPhase.WORLD_COLLISION : phase;
		for(ParsedTransition transition : data.isClient() ? data.getAction().CLIENT_TRANSITIONS.get(usePhase) : data.getAction().SERVER_TRANSITIONS.get(usePhase)) {
//			if(Objects.equals(data.getActionID(), MarioQuaMario.makeID("jump")))
//				MarioQuaMario.LOGGER.info("Testing transition from {}->{}:\n{}", data.getActionID(), transition.targetAction().ID, transition.evaluator().shouldTransition(data));
			if(transition.evaluator().shouldTransition(data)) {
				long seed = data.getPlayer().getRandom().nextLong();

				if(data.isServer()) {
					MarioDataPackets.transitionToActionS2C((ServerPlayerEntity) data.getPlayer(), transition.fullyNetworked(),
							data.getAction(), transition.targetAction(), seed);
				}
				else {
					if(transition.targetAction().CATEGORY == ActionCategory.WALLBOUND) {
						ParsedWallboundAction wallAction = (ParsedWallboundAction) transition.targetAction();
						float wallYaw = wallAction.getWallYaw(data);
						data.getWallInfo().setYaw(wallYaw);

						if(!wallAction.verifyWallLegality(data, Vec3d.ZERO)) {
//							MarioQuaMario.LOGGER.info("Client is cancelling successful wallbound transition without networking " +
//									"because legality check failed ({} -> {})", data.getActionID(), wallAction.ID);
							continue;
						}

						MarioClientHelperManager.packetSender.transmitWallYawC2S(data, wallYaw);
					}

					MarioClientHelperManager.packetSender.conditionallySaveTransitionToReplayMod(data.getAction(), transition.targetAction(), seed);
					if (transition.fullyNetworked()) {
						MarioClientHelperManager.packetSender.setActionC2S(data.getAction(), transition.targetAction(), seed, phase);
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

	public static boolean attemptTransitionTo(CPAPlayerData data, AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed) {
		ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
		if(transition == null) return false;

		executeTransition(data, transition, seed);
		return true;
	}

	public static void executeTransition(CPAPlayerData data, ParsedTransition transition, long seed) {
		if(MarioQuaMario.CONFIG.logAllActionTransitions()) MarioQuaMario.LOGGER.info("Executing transition for {} on {}: {} -> {}",
				data.getPlayer().getName().getString(), data.isClient() ? "CLIENT" : "SERVER",
				data.getActionID(), transition.targetAction().ID
		);

		if(data instanceof CPAMoveableData moveableData && transition.travelExecutor() != null)
			transition.travelExecutor().execute(moveableData);
		if(data instanceof ICPAClientData clientData && transition.clientsExecutor() != null)
			transition.clientsExecutor().execute(clientData, data.getPlayer().isMainPlayer(), seed);

		data.setActionTransitionless(transition.targetAction());
	}

	public static AbstractParsedAction get(int ID) {
		return RegistryManager.ACTIONS.get(ID);
	}
}

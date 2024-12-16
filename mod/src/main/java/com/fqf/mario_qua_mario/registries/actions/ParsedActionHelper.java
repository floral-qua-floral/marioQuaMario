package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.actions.*;
import com.fqf.mario_qua_mario.definitions.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.parsed.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.RandomSeed;

import java.util.HashMap;
import java.util.List;
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
			default -> null;
		};
	}

	public static void attemptTransitions(MarioMoveableData data, TransitionPhase phase) {
		for(ParsedTransition transition : data.isClient() ? data.getAction().CLIENT_TRANSITIONS.get(phase) : data.getAction().SERVER_TRANSITIONS.get(phase)) {
			if(transition.evaluator().shouldTransition(data)) {
				long seed = data.getMario().getRandom().nextLong();
				executeTransition(data, transition, seed);

				if(!data.isClient()) {
					// Send S2C transition packet
					// If fullyNetworked(), then send the packet to Mario too
				}
				else if(transition.fullyNetworked()) {
					// Send C2S transition packet
				}
			}
		}
	}

	public static void executeTransition(IMarioData data, ParsedTransition transition, long seed) {
		MarioQuaMario.LOGGER.info("Executing transition for {} on {}:\n\t{} -> {}",
				data.getMario().getName().getString(),
				data.isClient() ? "CLIENT" : "SERVER",
				data.getActionID(),
				transition.targetAction().ID
		);

		if(data instanceof MarioMoveableData moveableData && transition.travelExecutor() != null)
			transition.travelExecutor().execute(moveableData);
		if(data instanceof IMarioClientData clientData && transition.clientsExecutor() != null)
			transition.clientsExecutor().execute(clientData, data.getMario().isMainPlayer(), seed);
	}
}

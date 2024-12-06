package com.fqf.mario_qua_mario.definitions.actions.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TransitionDefinition {
	@FunctionalInterface
	public interface Evaluator {
		boolean shouldTransition(IMarioTravelData data);
	}

	@FunctionalInterface
	public interface TravelExecutor {
		boolean execute(IMarioTravelData data);
	}

	@FunctionalInterface
	public interface ClientsExecutor {
		boolean execute(IMarioClientData data, boolean isSelf, long seed);
	}

	public TransitionDefinition(
			@NotNull String targetID,
			@NotNull Evaluator evaluator,
			@Nullable TravelExecutor travelExecutor,
			@Nullable ClientsExecutor clientsExecutor
	) {
		this.TARGET_IDENTIFIER = Identifier.of(targetID);
		this.EVALUATOR = evaluator;
		this.TRAVEL_EXECUTOR = travelExecutor;
		this.CLIENTS_EXECUTOR = clientsExecutor;
	}

	public TransitionDefinition(@NotNull String targetID, @NotNull Evaluator evaluator) {
		this(targetID, evaluator, null, null);
	}

	public final Identifier TARGET_IDENTIFIER;
	public final Evaluator EVALUATOR;
	public final TravelExecutor TRAVEL_EXECUTOR;
	public final ClientsExecutor CLIENTS_EXECUTOR;
}

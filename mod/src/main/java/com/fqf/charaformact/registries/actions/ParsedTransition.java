package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact_api.definitions.states.actions.util.SizeChangeBehavior;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public record ParsedTransition(
		@NotNull AbstractParsedAction targetAction,
		@NotNull Predicate<CfaReadableMotionData> evaluator,
		boolean networkedS2C,
		boolean networkedC2S,
		boolean serverValidated,
		boolean canBeForcedBySmallSpace,
		@Nullable ActionTransitionDetails.TravelExecutor travelExecutor,
		@Nullable ActionTransitionDetails.ClientsExecutor clientsExecutor
) {
	private ParsedTransition(ActionTransitionDetails definition, SizeChangeBehavior sizeChangeBehavior) {
		this(
				getTargetAction(definition),
				definition.evaluator(),
				definition.environment().IS_FULLY_NETWORKED,
				definition.environment().IS_FULLY_NETWORKED,
				definition.environment() == EvaluatorEnvironment.CLIENT_CHECKED,
				switch (sizeChangeBehavior) {
					case AUTOMATIC -> throw new IllegalStateException("Size change behavior passed to ParsedTransition" +
							" constructor can never be AUTOMATIC!");
					case NO_CHECKING, REQUIRE_SUFFICIENT_SPACE -> false;
					case FORCE_IN_TIGHT_SPACES, COMPLEX -> true;
				},
				definition.travelExecutor(),
				definition.clientsExecutor()
		);
	}

	public static ParsedTransition of(AbstractParsedAction from, ActionTransitionDetails definition) {
		AbstractParsedAction to = RegistryManager.ACTIONS.get(definition.targetID());

		SizeChangeBehavior effectiveSizeChangeBehavior;
		if(to == null)
			effectiveSizeChangeBehavior = SizeChangeBehavior.NO_CHECKING;
		else if(definition.sizeChangeBehavior() == SizeChangeBehavior.AUTOMATIC) {
			int widthDifferenceSign = MathHelper.sign(from.WIDTH_FACTOR - to.WIDTH_FACTOR);
			int heightDifferenceSign = MathHelper.sign(from.HEIGHT_FACTOR - to.HEIGHT_FACTOR);

			if(widthDifferenceSign == 0 && heightDifferenceSign == 0)
				// Hitboxes are the same
				effectiveSizeChangeBehavior = SizeChangeBehavior.NO_CHECKING;
			else if(Math.abs(widthDifferenceSign - heightDifferenceSign) == 2)
				// Hitbox is bigger on one axis but smaller on the other
				effectiveSizeChangeBehavior = SizeChangeBehavior.COMPLEX;
			else if(widthDifferenceSign > 0 || heightDifferenceSign > 0)
				// Hitbox is smaller on at least 1 axis
				effectiveSizeChangeBehavior = SizeChangeBehavior.FORCE_IN_TIGHT_SPACES;
			else
				// Hitbox is bigger on at least 1 axis
				effectiveSizeChangeBehavior = SizeChangeBehavior.REQUIRE_SUFFICIENT_SPACE;

			if(effectiveSizeChangeBehavior != SizeChangeBehavior.NO_CHECKING && CharaFormAct.CONFIG.gameLaunchLogging())
				CharaFormAct.LOGGER.info("Transition {}->{} involves a change of hitbox, and has automagically" +
						" been assigned Size Change Behavior {}.", from.ID, definition.targetID(), effectiveSizeChangeBehavior);
		}
		else effectiveSizeChangeBehavior = definition.sizeChangeBehavior();

		if(effectiveSizeChangeBehavior == SizeChangeBehavior.REQUIRE_SUFFICIENT_SPACE
				|| effectiveSizeChangeBehavior == SizeChangeBehavior.COMPLEX)
			definition = attachSufficientSpaceRequirement(definition);

		return new ParsedTransition(definition, effectiveSizeChangeBehavior);
	}

	// Either value being assigned here has drawbacks...
	// If true, then the player will be able to stand up and wedge their head in a block if they clip themself into a
	// trapdoor or fence gate first.
	// If false, then they won't be able to stand up at all while clipped into such a block.
	private static final boolean ASSUME_SUFFICIENT_SPACE_IF_ALREADY_CLIPPED = false;

	private static ActionTransitionDetails attachSufficientSpaceRequirement(ActionTransitionDetails original) {
		AbstractParsedAction target = RegistryManager.ACTIONS.get(original.targetID());
		// ^ If this is null, then just tolerate it for now; it'll throw a better exception later when parsing transitions
		if(target == null) return original;

		Predicate<CfaReadableMotionData> fittingPredicate;
		if(ASSUME_SUFFICIENT_SPACE_IF_ALREADY_CLIPPED) fittingPredicate = data -> {
			CfaPlayerData pData = (CfaPlayerData) data;
			return pData.canFitInAction(target) || !pData.canFitInAction(pData.getAction());
		};
		else fittingPredicate = data -> ((CfaPlayerData) data).canFitInAction(target);

		boolean originallyClientOnly = original.environment() == EvaluatorEnvironment.CLIENT_ONLY;
		return original.variate(
				null,
				originallyClientOnly
						? fittingPredicate.and(original.evaluator().or(CfaData::isServer))
						: fittingPredicate.and(original.evaluator()),
				originallyClientOnly
						? EvaluatorEnvironment.CLIENT_CHECKED
						: null,
				null, null,
				null
		);
	}

	public static @NotNull AbstractParsedAction getTargetAction(ActionTransitionDetails definition) {
		AbstractParsedAction targetAction = RegistryManager.ACTIONS.get(definition.targetID());
		if(targetAction == null) throw new CrashException(new CrashReport(
				"Attempting to register a transition into action \"" + definition.targetID()
						+ "\", but that action isn't registered! Check your entrypoints!",
				new InvalidTargetActionException("Transition to " + definition.targetID())));
		if(targetAction.CATEGORY == ActionCategory.WALLBOUND && switch (definition.environment()) {
			case CLIENT_ONLY, CLIENT_CHECKED -> false;
			case SERVER_ONLY, COMMON -> true;
		}) throw new CrashException(new CrashReport(
				"Attempting to register a server-only or common-sided transition into a wallbound action. " +
						"Wallbound action transitions must ALWAYS be initiated by the client!!! Use CLIENT_CHECKED " +
						"instead!",
				new InvalidTargetActionException("Non-client-initiated transition to wallbound action (" + targetAction.ID + ")")));
		return targetAction;
	}

	public static class InvalidTargetActionException extends IllegalArgumentException {
		public InvalidTargetActionException(String message) {
			super(message);
		}
	}
}

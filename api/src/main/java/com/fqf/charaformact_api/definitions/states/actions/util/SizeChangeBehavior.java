package com.fqf.charaformact_api.definitions.states.actions.util;

/// Describes how any difference in size between actions affects the transition.
///
/// * `AUTOMATIC`: Automatically chooses between `NO_CHECKING`, `FORCE_IN_TIGHT_SPACES`, `REQUIRE_SUFFICIENT_SPACE`,
/// or `COMPLEX` based on any difference between the Action this transition is being applied to, and the Action it leads
/// into. This option is recommended unless you have a good reason to choose something else.
/// * `NO_CHECKING`: The transition behaves exactly as programmed, with no restrictions and no special cases that may
/// force it to occur, regardless of how it may or may not change the size of the player's hitbox. Chosen by `AUTOMATIC`
/// if the initial and target actions have the exact same hitbox size.
///
///
/// * `FORCE_IN_TIGHT_SPACES`: The transition may be forced to occur, completely bypassing its Evaluator, if doing so
/// would allow the player to fit in a space that is too small for their current action. Chosen by `AUTOMATIC` if the
/// transition would cause the player's hitbox to shrink.
/// * `REQUIRE_SUFFICIENT_SPACE`: The transition is disallowed from occurring if doing so would put the
public enum SizeChangeBehavior {
	AUTOMATIC,
	NO_CHECKING,

	FORCE_IN_TIGHT_SPACES,
	REQUIRE_SUFFICIENT_SPACE,
	COMPLEX
}

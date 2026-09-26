package com.fqf.charaformact_api.definitions.states.actions.util;

/// Describes how an Action Transition will behave between the Client and Server sides.
/// Note: Regardless of which of these is provided, remote players on the client side will NEVER evaluate Action
/// Transitions, they will always be received from the server!
/// * `CLIENT_ONLY`: The main client triggers the transition and sends it to the server. The server executes it without
/// any custom validation. Example: Transitioning from an idle action to a walking forwards action.
/// * `SERVER_ONLY`: The server triggers the transition and sends it to the main client. Example: A transition that
/// fires when a hostile mob starts targeting a player.
/// * `COMMON`: The main client and the server trigger the transition independently, AND DO NOT NETWORK IT TO ONE
/// ANOTHER! This can lead to a desync if it is triggered based on anything that isn't always kept synchronized.
/// Example: A transition that fires when the player's health gets low, their Form changes, or they land from a jump.
/// * `COMMON_CLIENT_TRIGGERABLE`: Identical to `COMMON`, except when the main client triggers the transition, it will
/// network it to the server, in case the server has not triggered it on its own. The server will still never network
/// this to the main client! Example: A crouching transition, that can be common-sided (when there is no space for the
/// player to fit) or client-triggered (when the player holds the Sneak key).
/// * `CLIENT_CHECKED`: Identical to `CLIENT_ONLY`, except when the server receives the transition, it will also run the
/// evaluator. If it returns false, the transition is rejected and the player is forced back to their previous action.
/// Use this to prevent clients from spoofing an action transition that could be used to cheat, while still allowing
/// them to perform it responsively. Keep in mind that you can check the sidedness within the evaluator to only check
/// certain things on certain sides - for example, check inputs and Form on the client, but only check Form on the
/// server. Example: Pressing Jump to transition into an Action, but only if the player is in a specific Form.
public enum EvaluatorEnvironment {
	CLIENT_ONLY(true, false),
	SERVER_ONLY(false, true),
	COMMON(true, true),
	CLIENT_CHECKED(true, false);

	public final boolean EVALUATE_ON_CLIENT, EVALUATE_ON_SERVER, IS_FULLY_NETWORKED;
	EvaluatorEnvironment(boolean checkOnClient, boolean checkOnServer) {
		this.EVALUATE_ON_CLIENT = checkOnClient;
		this.EVALUATE_ON_SERVER = checkOnServer;
		this.IS_FULLY_NETWORKED = checkOnClient != checkOnServer;
	}
}

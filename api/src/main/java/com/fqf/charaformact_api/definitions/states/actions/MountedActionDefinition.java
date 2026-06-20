package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * IMPORTANT: Vanilla dismounting logic is entirely disabled for anyone playing as a Character! You'll need to handle
 * that by yourself using the MountedActionHelper. If you don't provide a way for the player to dismount, they won't be
 * able to do so willingly! They can still be forced off their mount by external factors, such as a minecart rolling
 * over an activator rail, or a boat sinking.
 * <p>
 * IMPORTANT 2: If the player is forcefully dismounted, they will remain in this Action until you Transition them to a
 * different one!
 */
public interface MountedActionDefinition extends IncompleteActionDefinition {
	/**
	 * @param vanillaHint The vanilla hint text. This will be empty if the method is being called on a dedicated server.
	 *                    If you're using the vanilla dismount control scheme, return this object unmodified.
	 * @return The hint that will appear on the player's HUD upon mounting an entity, if mounting said entity puts the
	 * player into this Action. The return value only matters on the client; the logical server never uses this value.
	 * This is empty by default because no default dismounting functionality is provided either.
	 */
	default @NotNull MutableText defineDismountHint(
			MutableText vanillaHint,
			Text sneakKeybind, Text jumpKeybind,
			Text attackKeybind,
			Text forwardKeybind, Text backwardKeybind
	) {
		return Text.empty();
	}

	/**
	 * @return Return true to cancel the vanilla player physics. Return false to allow vanilla player physics to apply.
	 * This only affects the player, not the mount. It probably doesn't matter what you return here, since vanilla
	 * physics do almost nothing to the player while mounted.
	 */
	default boolean travel(CfaTravelData data, Entity mount, MountedActionHelper helper) {
		return false;
	}

	default void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, MountedActionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, MountedActionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, MountedActionHelper helper) {

	}

	/**
	 * Contains a number of methods intended to help with the creation of Mounted Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface MountedActionHelper {
		Entity getMount(CfaReadableMotionData data);

		void dismount(CfaTravelData data, boolean reposition);

		double getSlipFactor(Entity mount);
	}
}

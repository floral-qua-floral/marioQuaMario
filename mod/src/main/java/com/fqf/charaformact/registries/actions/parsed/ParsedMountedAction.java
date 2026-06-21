package com.fqf.charaformact.registries.actions.parsed;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.UniversalActionDefinitionHelper;
import com.google.common.collect.ImmutableList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class ParsedMountedAction extends AbstractParsedAction {
	private final MountedActionDefinition MOUNTED_DEFINITION;
	public final @NotNull MutableText DISMOUNT_HINT;

	public static Text sneakKeybind = Text.empty();
	public static Text jumpKeybind = Text.empty();
	public static Text attackKeybind = Text.empty();
	public static Text forwardKeybind = Text.empty();
	public static Text backwardKeybind = Text.empty();
	public static MutableText vanillaHint = Text.empty();

	public ParsedMountedAction(MountedActionDefinition definition) {
		super(definition);
		this.MOUNTED_DEFINITION = definition;
		this.DISMOUNT_HINT = definition.defineDismountHint(vanillaHint, sneakKeybind, jumpKeybind, attackKeybind, forwardKeybind, backwardKeybind);
		CharaFormAct.LOGGER.info("\nClient Helper Manager: {}\nClient Packet Sender: {}", CfaClientHelperManager.helper, CfaClientHelperManager.packetSender);
	}

	@Override
	public boolean travelHook(CfaMoveableData data) {
		data.jumpCapped = false;
		UniversalActionDefinitionHelper helper = UniversalActionDefinitionHelper.INSTANCE;
		return this.MOUNTED_DEFINITION.travel(data, helper.getMount(data), helper);
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.MOUNTED;
	}

	@Override
	protected void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.MOUNTED_DEFINITION.accumulateBasicTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.MOUNTED_DEFINITION.accumulateInputTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder) {
		this.MOUNTED_DEFINITION.accumulateCollisionTransitions(builder, UniversalActionDefinitionHelper.INSTANCE);
	}
}

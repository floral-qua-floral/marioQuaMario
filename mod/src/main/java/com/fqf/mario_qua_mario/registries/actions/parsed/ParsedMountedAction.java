package com.fqf.mario_qua_mario.registries.actions.parsed;

import com.fqf.mario_qua_mario.MarioClientHelperManager;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedMountedAction extends AbstractParsedAction {
	private final MountedActionDefinition MOUNTED_DEFINITION;

	public ParsedMountedAction(MountedActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.MOUNTED_DEFINITION = definition;
		MarioQuaMario.LOGGER.info("\nClient Helper Manager: {}\nClient Packet Sender: {}", MarioClientHelperManager.helper, MarioClientHelperManager.packetSender);
	}

	public MutableText getDismountHint() {
		return this.MOUNTED_DEFINITION.dismountingHint();
	}

	@Override
	public boolean travelHook(MarioMoveableData data) {
		data.jumpCapped = false;
		UniversalActionDefinitionHelper helper = UniversalActionDefinitionHelper.INSTANCE;
		return this.MOUNTED_DEFINITION.travelHook(data, helper.getMount(data), helper);
	}

	@Override
	protected ActionCategory getCategory() {
		return ActionCategory.MOUNTED;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.MOUNTED_DEFINITION.getBasicTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.MOUNTED_DEFINITION.getInputTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.MOUNTED_DEFINITION.getWorldCollisionTransitions(UniversalActionDefinitionHelper.INSTANCE);
	}
}

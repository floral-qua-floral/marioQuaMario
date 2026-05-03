package com.fqf.charapoweract.util;

import com.fqf.charapoweract.registries.actions.AnimationHelperImpl;
import com.fqf.charapoweract.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.charapoweract_api.HelperGetter;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;

public class HelperGetterImplementation extends HelperGetter {
	public static void staticInitialize() {

	}
	static {
		HelperGetter.instance = new HelperGetterImplementation();
	}

	@Override
	protected TransitionInjectionDefinition.TransitionCreator.CastableHelper getInstanceCastableActionHelper() {
		return UniversalActionDefinitionHelper.INSTANCE;
	}

	@Override
	protected AnimationHelper getInstanceAnimationHelper() {
		return AnimationHelperImpl.INSTANCE;
	}
}

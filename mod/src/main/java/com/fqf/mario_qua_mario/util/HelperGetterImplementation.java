package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.registries.actions.AnimationHelperImpl;
import com.fqf.mario_qua_mario.registries.actions.UniversalActionDefinitionHelper;
import com.fqf.mario_qua_mario_api.HelperGetter;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;

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

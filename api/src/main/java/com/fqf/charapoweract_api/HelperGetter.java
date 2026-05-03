package com.fqf.charapoweract_api;

import com.fqf.charapoweract_api.definitions.states.actions.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;

public abstract class HelperGetter {
	public static TransitionInjectionDefinition.TransitionCreator.CastableHelper getCastableActionHelper() {
		return instance.getInstanceCastableActionHelper();
	}

	public static GroundedActionDefinition.GroundedActionHelper getGroundedActionHelper() {
		return getCastableActionHelper().asGrounded();
	}
	public static AirborneActionDefinition.AirborneActionHelper getAirborneActionHelper() {
		return getCastableActionHelper().asAirborne();
	}
	public static AquaticActionDefinition.AquaticActionHelper getAquaticActionHelper() {
		return getCastableActionHelper().asAquatic();
	}
	public static MountedActionDefinition.MountedActionHelper getMountedActionHelper() {
		return getCastableActionHelper().asMounted();
	}
	public static WallboundActionDefinition.WallboundActionHelper getWallboundActionHelper() {
		return getCastableActionHelper().asWallbound();
	}

	public static AnimationHelper getAnimationHelper() {
		return instance.getInstanceAnimationHelper();
	}

	protected static HelperGetter instance;
	protected abstract TransitionInjectionDefinition.TransitionCreator.CastableHelper getInstanceCastableActionHelper();
	protected abstract AnimationHelper getInstanceAnimationHelper();
}

package com.fqf.charaformact_api;

import com.fqf.charaformact_api.definitions.states.actions.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;

public abstract class HelperGetter {
	public static GenericActionDefinition.CastableHelper getCastableActionHelper() {
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
	protected abstract GenericActionDefinition.CastableHelper getInstanceCastableActionHelper();
	protected abstract AnimationHelper getInstanceAnimationHelper();
}

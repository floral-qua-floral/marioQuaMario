package com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera;

import com.fqf.charapoweract_api.definitions.states.actions.util.animation.Arrangement;

public record CameraAnimation(
		CameraProgressHandler progressHandler,
		Arrangement.Mutator mutator
) {

}

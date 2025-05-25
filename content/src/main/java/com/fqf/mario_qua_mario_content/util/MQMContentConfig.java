package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "mario_qua_mario_content")
public class MQMContentConfig implements ConfigData {
//	private boolean backflipFromVehicles = true;

	private CameraAnimationOption backflipCameraAnim = CameraAnimationOption.GENTLE;
	private CameraAnimationOption sideflipCameraAnim = CameraAnimationOption.AUTHENTIC;
	private CameraAnimationOption tripleJumpCameraAnim = CameraAnimationOption.GENTLE;

	private CameraAnimationOption tailWhipCameraAnim = CameraAnimationOption.GENTLE;
	private CameraAnimationOption tailSpinCameraAnim = CameraAnimationOption.AUTHENTIC;

	private boolean welcomeMessage = true;
	private boolean nagMessage = true;

//	public boolean getBackflipFromVehicles() {
//		return this.backflipFromVehicles;
//	}
	public boolean isWelcomeMessageEnabled() {
		return this.welcomeMessage;
	}
	public boolean isNagMessageEnabled() {
		return this.nagMessage;
	}
	public CameraAnimationOption getBackflipCameraAnim() {
		return this.backflipCameraAnim;
	}
	public CameraAnimationOption getSideflipCameraAnim() {
		return this.sideflipCameraAnim;
	}
	public CameraAnimationOption getTripleJumpCameraAnim() {
		return this.tripleJumpCameraAnim;
	}
	public CameraAnimationOption getTailWhipCameraAnim() {
		return this.tailWhipCameraAnim;
	}
	public CameraAnimationOption getTailSpinCameraAnim() {
		return this.tailSpinCameraAnim;
	}
}

package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "mario_qua_mario_content")
public class MQMContentConfig implements ConfigData {
	private boolean backflipFromVehicles = true;

	private CameraAnimationOption backflipCameraAnim = CameraAnimationOption.GENTLE;
	private CameraAnimationOption sideflipCameraAnim = CameraAnimationOption.AUTHENTIC;
	private CameraAnimationOption tripleJumpCameraAnim = CameraAnimationOption.GENTLE;

	public boolean getBackflipFromVehicles() {
		return this.backflipFromVehicles;
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
}

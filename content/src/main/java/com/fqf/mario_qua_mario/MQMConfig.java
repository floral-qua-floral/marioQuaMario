package com.fqf.mario_qua_mario;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationOption;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
@Config(name = "mario_qua_mario")
public class MQMConfig implements ConfigData {
	private boolean backflipFromVehicles = true;
	private boolean autoLadder = true;
	private boolean squashDamageAnimation = true;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption backflipCameraAnim = CameraAnimationOption.GENTLE;
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption sideflipCameraAnim = CameraAnimationOption.AUTHENTIC;
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption tripleJumpCameraAnim = CameraAnimationOption.GENTLE;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption groundPoundCameraAnim = CameraAnimationOption.GENTLE;

	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption tailWhipCameraAnim = CameraAnimationOption.GENTLE;
	@ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
	private CameraAnimationOption tailSpinCameraAnim = CameraAnimationOption.AUTHENTIC;

	private boolean welcomeMessage = true;
	private boolean nagMessage = true;

	public boolean doBackflipFromVehicles() {
		return this.backflipFromVehicles;
	}
	public boolean doAutoLadder() {
		return this.autoLadder;
	}
	public boolean doSquashDamageAnimation() {
		return this.squashDamageAnimation;
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

	public CameraAnimationOption getGroundPoundCameraAnim() {
		return this.groundPoundCameraAnim;
	}

	public CameraAnimationOption getTailWhipCameraAnim() {
		return this.tailWhipCameraAnim;
	}
	public CameraAnimationOption getTailSpinCameraAnim() {
		return this.tailSpinCameraAnim;
	}

	public boolean isWelcomeMessageEnabled() {
		return this.welcomeMessage;
	}
	public boolean isNagMessageEnabled() {
		return this.nagMessage;
	}
}

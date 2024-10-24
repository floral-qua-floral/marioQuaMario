package com.floralquafloral.mariodata.client;

import com.floralquafloral.MarioQuaMario;
import net.minecraft.client.network.ClientPlayerEntity;

public enum Input {
	FORWARD,
	BACKWARD,
	RIGHT,
	LEFT,

	JUMP,
	DUCK,
	SPIN;

	private boolean isHeld;
	private int pressBuffer;

	private static double forwardInput, strafeInput;
	private static final double INPUT_FACTOR = 1.02040816327;

	Input() {
		this.isHeld = false;
		this.pressBuffer = 0;
	}

	public boolean isHeld() {
		return this.isHeld;
	}
	public boolean isPressed() {
		return this.pressBuffer > 0;
	}
	public void unbuffer() {
		this.pressBuffer = 0;
	}

	public static double getForwardInput() {
		return forwardInput;
	}
	public static double getStrafeInput() {
		return strafeInput;
	}

	static void update(ClientPlayerEntity mario) {
		FORWARD.update(mario.input.pressingForward);
		BACKWARD.update(mario.input.pressingBack);
		RIGHT.update(mario.input.pressingRight);
		LEFT.update(mario.input.pressingLeft);

		JUMP.update(mario.input.jumping);

		DUCK.update(mario.input.sneaking || mario.isInSneakingPose(), mario.input.sneaking);

		SPIN.update(false);
	}
	static void updateDirections(double forwardInput, double strafeInput) {
		Input.forwardInput = forwardInput * INPUT_FACTOR;
		Input.strafeInput = strafeInput * INPUT_FACTOR;
	}

	private static final Input[] VALUES = Input.values();
	static void tick() {
//		for(Input tickInput : VALUES) {
//			tickInput.instanceTick();
//		}
	}

	private void update(boolean isHeld) {
		this.update(isHeld, isHeld);
	}
	private void update(boolean isHeld, boolean isPressed) {
		if(isHeld && isPressed && !this.isHeld)
			this.pressBuffer = MarioQuaMario.CONFIG.getBufferLength();
		else
			this.pressBuffer--;
		this.isHeld = isHeld;
	}
}

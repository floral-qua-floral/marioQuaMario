package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MarioServerPlayerData extends MarioMoveableData implements IMarioAuthoritativeData {
	private ServerPlayerEntity mario;
	public MarioServerPlayerData(ServerPlayerEntity mario) {
		super();
		this.mario = mario;
	}
	@Override public ServerPlayerEntity getMario() {
		return this.mario;
	}

	@Override public void setEnabled(boolean enable) {

	}

	@Override public boolean setAction(Identifier actionID) {
		return false;
	}

	@Override public boolean setAction(String actionID) {
		return false;
	}

	@Override public void setActionTransitionless(Identifier actionID) {

	}

	@Override public void setActionTransitionless(String actionID) {

	}

	@Override public void setPowerUp(Identifier powerUpID) {

	}

	@Override public void setPowerUp(String powerUpID) {

	}

	@Override public void setCharacter(Identifier characterID) {

	}

	@Override public void setCharacter(String characterID) {

	}

	@Override public void setMario(PlayerEntity mario) {
		this.mario = (ServerPlayerEntity) mario;
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public void tick() {

	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
//		ServerPlayerEntity mario = this.getMario();
//		mario.mqm$getMarioData();


		return false;
	}

	@Override public MarioInputs getInputs() {
		return PHONY_INPUTS;
	}

	private static final MarioInputs PHONY_INPUTS;
	static {
		MarioInputs.MarioButton phonyButton = new MarioInputs.MarioButton() {
			@Override public boolean isPressed() {
				return false;
			}
			@Override public boolean isHeld() {
				return false;
			}
		};
		PHONY_INPUTS = new MarioInputs(phonyButton, phonyButton, phonyButton) {
			@Override public double getForwardInput() {
				return 0;
			}
			@Override public double getStrafeInput() {
				return 0;
			}
			@Override public boolean isReal() {
				return false;
			}
		};
	}
}

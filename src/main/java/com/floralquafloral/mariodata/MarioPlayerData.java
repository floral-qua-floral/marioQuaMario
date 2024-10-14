package com.floralquafloral.mariodata;

import com.floralquafloral.MarioQuaMario;
import net.minecraft.entity.player.PlayerEntity;

public class MarioPlayerData implements MarioData {
	private boolean enabled;
	private String state;
	private String powerUp;
	private String character;

	public final PlayerEntity MARIO;

	@Override public boolean isEnabled() {
		return enabled;
	}
	@Override public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	@Override public String getState() {
		return state;
	}
	@Override public void setState(String state) {
		this.state = state;
	}
	@Override public String getPowerUp() {
		return powerUp;
	}
	@Override public void setPowerUp(String powerUp) {
		this.powerUp = powerUp;
	}
	@Override public String getCharacter() {
		return character;
	}
	@Override public void setCharacter(String character) {
		this.character = character;
	}

	public MarioPlayerData(PlayerEntity mario) {
		this.MARIO = mario;
		this.setEnabled(true);
		this.setState("nil");
		this.setPowerUp("nil");
		this.setCharacter("nil");

		MarioQuaMario.LOGGER.info("Initialized a MarioData: {}", this);
	}
}

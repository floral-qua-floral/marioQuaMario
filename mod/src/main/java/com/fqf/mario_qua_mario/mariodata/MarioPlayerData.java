package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * The most advanced form of MarioData that can be applied for all players.
 */
public abstract class MarioPlayerData implements IMarioData {
	public abstract void setMario(PlayerEntity mario);

	@Override
	public double getStat(CharaStat stat) {
		return 0;
	}

	@Override
	public double getStatMultiplier(CharaStat stat) {
		return 0;
	}

	@Override
	public int getBumpStrengthModifier() {
		return 0;
	}

	@Override
	public boolean canSneak() {
		return false;
	}

	@Override
	public boolean canSprint() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public Identifier getActionID() {
		return null;
	}

	@Override
	public Identifier getPowerUpID() {
		return null;
	}

	@Override
	public Identifier getCharacterID() {
		return null;
	}
}

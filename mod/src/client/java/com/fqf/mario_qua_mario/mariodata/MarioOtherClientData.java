package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class MarioOtherClientData extends MarioPlayerData implements IMarioClientDataImpl {
	private OtherClientPlayerEntity mario;
	public MarioOtherClientData() {
		super();
	}
	@Override public OtherClientPlayerEntity getMario() {
		return this.mario;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.mario = (OtherClientPlayerEntity) mario;
		super.setMario(mario);
	}

	@Override
	public void setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion, long seed) {
		this.handlePowerTransitionSound(isReversion, newPowerUp, seed);
		super.setPowerUp(newPowerUp, isReversion, seed);
	}

	@Override public void setActionTransitionless(AbstractParsedAction action) {
		this.handleSlidingSound(action);
		super.setActionTransitionless(action);
	}

	@Override public void tick() {
		super.tick();
		this.getAction().clientTick(this, false);
		this.getPowerUp().clientTick(this, false);
		this.getCharacter().clientTick(this, false);
	}

	private final Map<Identifier, SoundInstance> STORED_SOUNDS = new HashMap<>();
	@Override public Map<Identifier, SoundInstance> getStoredSounds() {
		return this.STORED_SOUNDS;
	}
}

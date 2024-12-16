package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/**
 * The most advanced form of MarioData that can be applied for all players.
 */
public abstract class MarioPlayerData implements IMarioData {
	protected MarioPlayerData() {
		this.enabled = false;
		this.setEnabledInternal(true);
		this.action = null;
		this.setActionTransitionlessInternal(RegistryManager.ACTIONS.get(MarioQuaMario.makeID("debug")));
//		this.powerUp = null;
//		this.character = null;
	}

	private boolean enabled;
	private static final Identifier FALL_RESISTANCE_ID = Identifier.of(MarioQuaMario.MOD_ID, "mario_fall_resistance");
	private static final EntityAttributeModifier FALL_RESISTANCE = new EntityAttributeModifier(
			FALL_RESISTANCE_ID, 8, EntityAttributeModifier.Operation.ADD_VALUE
	);

	private static final Identifier ATTACK_SLOWDOWN_ID = Identifier.of(MarioQuaMario.MOD_ID, "mario_fall_resistance");
	private static final EntityAttributeModifier ATTACK_SLOWDOWN = new EntityAttributeModifier(
			ATTACK_SLOWDOWN_ID, -0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
	);
	@Override public boolean isEnabled() {
		return this.enabled;
	}
	public void setEnabledInternal(boolean enabled) {
		this.enabled = enabled;
	}

	private AbstractParsedAction action;
	@Override public Identifier getActionID() {
		return this.getAction().ID;
	}
	public AbstractParsedAction getAction() {
		return this.action;
	}

	public boolean setActionInternal(AbstractParsedAction action, long seed, boolean forced) {
		boolean transitionedNaturally = ParsedActionHelper.attemptTransitionTo(this, action, seed);
		if(!transitionedNaturally && forced) this.setActionTransitionlessInternal(action);
		return transitionedNaturally || forced;
	}
	public void setActionTransitionlessInternal(AbstractParsedAction action) {
		this.action = action;
	}

	@Override public Identifier getPowerUpID() {
		return null;
	}

	@Override public Identifier getCharacterID() {
		return null;
	}

	public abstract void setMario(PlayerEntity mario);
	public abstract void tick();

	@Override public double getStat(CharaStat stat) {
		return stat.BASE * this.getStatMultiplier(stat);
	}

	@Override public double getStatMultiplier(CharaStat stat) {
			return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}
}

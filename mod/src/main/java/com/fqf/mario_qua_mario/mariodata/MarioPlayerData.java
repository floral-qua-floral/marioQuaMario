package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario.registries.actions.ParsedActionHelper;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The most advanced form of MarioData that can be applied for all players.
 */
public abstract class MarioPlayerData implements IMarioData {
	protected MarioPlayerData() {
		this.enabled = false;
		this.setEnabledInternal(true);

		this.character = Objects.requireNonNull(RegistryManager.CHARACTERS.get(MarioQuaMario.makeID("mario")),
				"Mario isn't registered; can't initialize player!");
		this.action = this.character.INITIAL_ACTION;
		this.powerUp = this.character.INITIAL_POWER_UP;

//		this.action = RegistryManager.ACTIONS.get(MarioQuaMario.makeID("debug"));
//		this.setActionTransitionlessInternal(this.action);
//		this.powerUp = null;
//		this.character = null;
	}

	private boolean enabled;
	private static final Identifier FALL_RESISTANCE_ID = MarioQuaMario.makeID("mario_fall_resistance");
	private static final EntityAttributeModifier FALL_RESISTANCE = new EntityAttributeModifier(
			FALL_RESISTANCE_ID, 8, EntityAttributeModifier.Operation.ADD_VALUE
	);

	private static final Identifier ATTACK_SLOWDOWN_ID = MarioQuaMario.makeID("mario_fall_resistance");
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
	public AbstractParsedAction getAction() {
		return this.action;
	}
	@Override public Identifier getActionID() {
		return this.getAction().ID;
	}

	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced) {
		if(fromAction == null) fromAction = this.getAction();
		else if(!forced) {
			// Check if we were recently in fromAction. If not, return false.
		}
		boolean transitionedNaturally = ParsedActionHelper.attemptTransitionTo(this, fromAction, toAction, seed);
		if(!transitionedNaturally && forced) this.setActionTransitionless(toAction);
		return transitionedNaturally || forced;
	}
	public void setActionTransitionless(AbstractParsedAction action) {
		this.RECENT_ACTIONS.add(new Pair<>(this.action, this.getMario().getWorld().getTime() + 10L));
		this.action = action;
	}

	private ParsedPowerUp powerUp;
	public ParsedPowerUp getPowerUp() {
		return this.powerUp;
	}
	@Override public Identifier getPowerUpID() {
		return this.getPowerUp().ID;
	}

	public void setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion) {
		this.setPowerUpInternal(newPowerUp);
	}
	public void setPowerUpInternal(ParsedPowerUp newPowerUp) {
		this.powerUp = newPowerUp;
		refreshPlayerModel();
		refreshPowerSet();
	}

	private ParsedCharacter character;
	public ParsedCharacter getCharacter() {
		return this.character;
	}
	@Override public Identifier getCharacterID() {
		return this.getCharacter().ID;
	}

	public void setCharacter(ParsedCharacter character) {
		this.character = character;
		refreshPlayerModel();
		refreshPowerSet();
	}

	public void refreshPlayerModel() {

	}
	private final Set<String> POWERS = new HashSet<>();
	public void refreshPowerSet() {
		this.POWERS.clear();
		this.POWERS.addAll(this.getPowerUp().POWERS);
		this.POWERS.addAll(this.getCharacter().POWERS);
	}
	@Override public boolean hasPower(String power) {
		return this.POWERS.contains(power);
	}

	public abstract void setMario(PlayerEntity mario);

	public void tick() {
		long worldTime = this.getMario().getWorld().getTime();
		this.RECENT_ACTIONS.removeIf(pair -> worldTime > pair.getRight());
	}

	@Override public double getStat(CharaStat stat) {
		return stat.BASE * this.getStatMultiplier(stat);
	}

	@Override public double getStatMultiplier(CharaStat stat) {
			return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return this.getPowerUp().BUMP_STRENGTH_MODIFIER + this.getCharacter().BUMP_STRENGTH_MODIFIER;
	}

	private final Set<Pair<AbstractParsedAction, Long>> RECENT_ACTIONS = new HashSet<>();
	private boolean recentlyInAction(AbstractParsedAction action) {
		return true;
	}
}

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

public abstract class MarioPlayerData implements IMarioData {
	protected MarioPlayerData() {
		MarioQuaMario.LOGGER.info("Created new MarioData: {}", this);

		this.enabled = true;

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

	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced, boolean fromCommand) {
		if(!this.getAction().equals(fromAction) && !forced && !fromCommand) {
			// Check if we were recently in fromAction. If not, return false.
			if(fromAction == null || this.RECENT_ACTIONS.stream().noneMatch(pair -> pair.getLeft().equals(fromAction))) {
				Identifier fromActionID = fromAction == null ? null : fromAction.ID;
				MarioQuaMario.LOGGER.info("TRANSITION REJECTED:\nCurrent action: {}\nFrom action: {}\nTo action: {}", this.getActionID(), fromActionID, toAction.ID);
				return false;
			}
		}
		boolean transitionedNaturally = ParsedActionHelper.attemptTransitionTo(this, fromAction == null ? this.getAction() : fromAction, toAction, seed);
		if(transitionedNaturally && this instanceof MarioMoveableData moveableData) moveableData.applyModifiedVelocity();
		else if(forced) this.setActionTransitionless(toAction);
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

	public void setPowerUp(ParsedPowerUp newPowerUp, boolean isReversion, long seed) {
		this.setPowerUpTransitionless(newPowerUp);
	}
	public void setPowerUpTransitionless(ParsedPowerUp newPowerUp) {
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

	public void setMario(PlayerEntity mario) {
		MarioQuaMario.LOGGER.info("Assigning player to MarioData: {} to {}", mario.getName().getString(), this);
		this.setEnabledInternal(this.isEnabled());
		this.setActionTransitionless(this.action);
		this.setPowerUpTransitionless(this.powerUp);
		this.setCharacter(this.character);
	}

	public void tick() {
		long worldTime = this.getMario().getWorld().getTime();
		this.RECENT_ACTIONS.removeIf(pair -> worldTime > pair.getRight());
	}

	@Override public double getStat(CharaStat stat) {
		return stat.BASE_VALUE * this.getStatMultiplier(stat);
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

	public boolean doMarioTravel() {
		return this.isEnabled() && !this.getMario().getAbilities().flying && !this.getMario().isFallFlying() && !this.getMario().isUsingRiptide();
	}

	public boolean attemptDismount;
}

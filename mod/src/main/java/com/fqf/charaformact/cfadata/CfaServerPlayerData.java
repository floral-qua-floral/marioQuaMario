package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.packets.CfaDataPackets;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.actions.AbstractParsedAction;
import com.fqf.charaformact.registries.actions.ParsedActionHelper;
import com.fqf.charaformact.registries.actions.ParsedTransition;
import com.fqf.charaformact.registries.actions.TransitionPhase;
import com.fqf.charaformact.registries.actions.parsed.ParsedWallboundAction;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import net.minecraft.entity.MovementType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.RandomSeed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CfaServerPlayerData extends CfaMoveableData implements CfaAuthoritativeData {
	private final ServerPlayerEntity PLAYER;
	public CfaServerPlayerData(ServerPlayerEntity player) {
		super();
		this.PLAYER = player;
	}
	@Override public ServerPlayerEntity getPlayer() {
		return this.PLAYER;
	}

	private final Set<Pair<AbstractParsedAction, Long>> RECENT_ACTIONS = new HashSet<>();

	@Override public void initialApply() {
		if(this.isEnabled()) {
			ParsedForm preApplyForm = this.getForm();
			this.setCharacter(this.getCharacter());
			this.setFormTransitionless(preApplyForm);
			CfaDataPackets.syncCfaDataToPlayerS2C(this.getPlayer(), this.getPlayer());
		}
		else super.initialApply();
//		this.syncToClient(this.getPlayer());
	}

	@Override
	public void updatePassiveUniversalTraits(boolean enabled) {
		super.updatePassiveUniversalTraits(enabled);
		if(enabled) this.updatePlayerModel();
//		else CPMCompat.getCommonAPI().resetPlayerModel(PlayerEntity.class, this.getPlayer());
	}

	@Override
	public boolean setAction(@Nullable AbstractParsedAction fromAction, AbstractParsedAction toAction, long seed, boolean forced, boolean fromCommand) {
		if(!forced && !fromCommand) {
			if(!this.getAction().equals(fromAction)) {
				if(fromAction == null) {
					CharaFormAct.LOGGER.warn("TRANSITION REJECTED: fromAction is null. Trying to transition from null to {}",
							toAction.ID);
					return false;
				}

				// Check if we were recently in fromAction. If not, return false.
				if(!this.recentlyInAction(fromAction)) {
					if (CharaFormAct.LOGGER.isWarnEnabled()) { // is there a reason to bother checking this????
						StringBuilder recentActionsString = new StringBuilder();
						for (Pair<AbstractParsedAction, Long> recentAction : RECENT_ACTIONS) {
							recentActionsString.append("\n").append(recentAction.getLeft().ID);
						}
						CharaFormAct.LOGGER.warn("""
								TRANSITION REJECTED: Not recently in fromAction.
								Server-sided action: {}
								Attempted {} -> {}
								Recent actions: {}""", this.getActionID(), fromAction.ID, toAction.ID, recentActionsString);
					}
					return false;
				}
			}

			// Check if our current action is a Mounted action and the fromAction isn't. If so, return false.
			if(this.getActionCategory() == ActionCategory.MOUNTED && fromAction.CATEGORY != ActionCategory.MOUNTED) {
				CharaFormAct.LOGGER.warn("""
							TRANSITION REJECTED: Trying to execute non-mounted transition while in mounted action.
							Server-sided action: {}
							Attempted: {} -> {}""", this.getActionID(), fromAction.ID, toAction.ID);
				return false;
			}

			// Check if we're trying to transition into a Wallbound action.
			if(toAction.CATEGORY == ActionCategory.WALLBOUND) {
				ParsedWallboundAction wallAction = (ParsedWallboundAction) toAction;

				// If last received wall yaw packet was too long ago, reject
				if(this.getPlayer().getWorld().getTime() > this.lastReceivedWallYawTime + 60L) {
					CharaFormAct.LOGGER.warn("""
							TRANSITION REJECTED: Trying to enter Wallbound Action, but last wall yaw packet was\
							 received too long ago.
							Server-sided action: {}
							Attempted: {} -> {}
							Last wall yaw given was {}
							Last wall yaw packet received at {}""",
							this.getActionID(), fromAction.ID, toAction.ID, this.lastReceivedWallYaw, this.lastReceivedWallYawTime);
					return false;
				}

				// Else, assign the new yaw:
				this.getWallInfo().setYaw(this.lastReceivedWallYaw);
				// And if legality check fails, REJECT
				if(!wallAction.verifyWallLegality(this, Vec3d.ZERO)) {
					CharaFormAct.LOGGER.warn("""
							TRANSITION REJECTED: Trying to enter Wallbound Action, but server-sided legality check failed.
							Server-sided action: {}
							Attempted: {} -> {}""", this.getActionID(), fromAction.ID, toAction.ID);
					return false;
				}

				// Else, broadcast wall yaw to clients:
				CfaDataPackets.transmitWallYawS2C(this.getPlayer(), this.lastReceivedWallYaw);
			}

			@Nullable ParsedTransition transition = fromAction.TRANSITIONS_FROM_TARGETS.get(toAction);
			if(transition != null && transition.serverChecked() && !transition.evaluator().shouldTransition(this)) {
				CharaFormAct.LOGGER.warn("""
						TRANSITION REJECTED: Transition is server-checked and evaluator failed.
						Attempted {} -> {}""", fromAction.ID, toAction.ID);
				return false;
			}
		}

		return super.setAction(fromAction, toAction, seed, forced, fromCommand);
	}

	public boolean recentlyInAction(AbstractParsedAction checkAction) {
		return this.getAction() == checkAction || this.RECENT_ACTIONS.stream().anyMatch(pair -> pair.getLeft().ID.equals(checkAction.ID));
	}

	@Override
	public void setActionTransitionless(AbstractParsedAction action) {
		this.RECENT_ACTIONS.add(new Pair<>(this.getAction(), this.getPlayer().getWorld().getTime() + 10L));
		super.setActionTransitionless(action);
	}

	@Override
	public boolean setFormTransitionless(ParsedForm form) {
		if(!this.updatePlayerModel(form)) return false;
		return super.setFormTransitionless(form);
	}

	private boolean updatePlayerModel(ParsedForm form) {
//		ModelFile newModel = this.getCharacter().MODELS.get(form);
//		if(newModel == null) {
//			CharaFormAct.LOGGER.error("Attempting to set {}'s form, however there is no model for combination {} + {}!",
//					this.getPlayer().getName().getString(), this.getCharacterID(), form.ID);
//			return false;
//		}
//		if(this.getPlayer().networkHandler != null)
//			CPMCompat.getCommonAPI().setPlayerModel(PlayerEntity.class, this.getPlayer(), newModel, true);
		return true;
	}
	public void updatePlayerModel() {
		this.updatePlayerModel(this.getForm());
	}

	@Override
	public boolean isClient() {
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.getAction().serverTick(this);
		this.getForm().serverTick(this);
		this.getCharacter().serverTick(this);

		long worldTime = this.getPlayer().getWorld().getTime();
		this.RECENT_ACTIONS.removeIf(pair -> worldTime > pair.getRight());

		this.skipDismountRepositioningTicks--;
	}

	private float lastReceivedWallYaw = Float.NaN;
	private long lastReceivedWallYawTime = Long.MIN_VALUE;
	public void receiveWallYaw(float wallYaw) {
		this.lastReceivedWallYaw = wallYaw;
		// NaN yaws will never be checked
		this.lastReceivedWallYawTime = Float.isNaN(wallYaw) ? Long.MIN_VALUE : this.getPlayer().getWorld().getTime();
	}

	@Override
	public boolean travelHook(double forwardInput, double strafeInput) {
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.BASIC);
		// Check for Elytra and Creative Flight
		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION_EARLY);

		boolean cancelVanillaTravel = this.getAction().travelHook(this);

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.INPUT);

		this.applyModifiedVelocity();
		this.getPlayer().move(MovementType.SELF, this.getMovementWithFluidPushing());

		ParsedActionHelper.attemptTransitions(this, TransitionPhase.WORLD_COLLISION);

		this.getPlayer().updateLimbs(false);
		return cancelVanillaTravel;
	}

	@Override
	public void handleInputUnbuffering(boolean transitionSuccessful) {

	}

	@Override public Inputs getInputs() {
		return PHONY_INPUTS;
	}

	public static final Inputs PHONY_INPUTS;
	static {
		Inputs.ButtonInput phonyButton = new Inputs.ButtonInput() {
			@Override public boolean isPressed() {
				return false;
			}
			@Override public boolean isHeld() {
				return false;
			}
		};
		PHONY_INPUTS = new Inputs(phonyButton, phonyButton, phonyButton) {
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

	private int skipDismountRepositioningTicks;
	public void skipDismountRepositioning() {
		this.skipDismountRepositioningTicks = 10;
	}
	public boolean isSkippingDismountRepositioning() {
		return this.isEnabled() && this.skipDismountRepositioningTicks > 0;
	}

	// CUTOFF FOR CfaAuthoritativeData IMPLEMENTATION:---------------------------------------------------------------
	@Override public void disable() {
		this.disableInternal();
		CfaDataPackets.setNoCharacterS2C(this.getPlayer());
	}

	@Override public ActionTransitionResult forceActionTransition(@Nullable Identifier fromID, @NotNull Identifier toID) {
		if(!this.isEnabled()) return ActionTransitionResult.NOT_ENABLED;
		long seed = RandomSeed.getSeed();

		AbstractParsedAction fromAction;
		if(fromID == null) fromAction = this.getAction();
		else fromAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(fromID),
				"Pre-transition action (" + toID + ") doesn't exist!");

		AbstractParsedAction toAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(toID),
				"Target action (" + toID + ") doesn't exist!");

		if(this.setAction(fromAction, toAction, seed, false, true)) {
			CfaDataPackets.transitionToActionS2C(this.getPlayer(), true, fromAction, toAction, seed);
			return ActionTransitionResult.SUCCESS;
		}
		return ActionTransitionResult.NO_SUCH_TRANSITION;
	}
	@Override public ActionTransitionResult forceActionTransition(@Nullable String fromID, @NotNull String toID) {
		return this.forceActionTransition(fromID == null ? null : Identifier.of(fromID), Identifier.of(toID));
	}

	@Override public ActionChangeOperationResult assignAction(Identifier actionID) {
		if(!this.isEnabled()) return ActionChangeOperationResult.NOT_ENABLED;
		AbstractParsedAction newAction = Objects.requireNonNull(RegistryManager.ACTIONS.get(actionID),
				"Target action (" + actionID + ") doesn't exist!");
		this.setActionTransitionless(newAction);
		CfaDataPackets.assignActionS2C(this.getPlayer(), true, newAction);
		return ActionChangeOperationResult.SUCCESS;
	}
	@Override public ActionChangeOperationResult assignAction(String actionID) {
		return this.assignAction(Identifier.of(actionID));
	}

	@Override public FormChangeOperationResult empowerTo(Identifier formID) {
		if(!this.isEnabled()) return FormChangeOperationResult.NOT_ENABLED;
		ParsedForm newForm = Objects.requireNonNull(RegistryManager.FORMS.get(formID),
				"Target form (" + formID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setForm(newForm, false, seed)) return FormChangeOperationResult.MISSING_PLAYERMODEL;
		CfaDataPackets.empowerRevertS2C(this.getPlayer(), newForm, false, seed);
		return FormChangeOperationResult.SUCCESS;
	}
	@Override public FormChangeOperationResult empowerTo(String formID) {
		return this.empowerTo(Identifier.of(formID));
	}

	@Override
	public ReversionResult executeReversion() {
		if(!this.isEnabled()) return ReversionResult.NOT_ENABLED;

		ServerPlayerEntity player = this.getPlayer();
		Identifier reversionTarget = this.getForm().REVERSION_TARGET_ID;
		if(reversionTarget == null) return ReversionResult.NO_WEAKER_FORM;

		if(player.getWorld().getGameRules().getBoolean(CfaGamerules.REVERT_TO_SMALL)) {
			while(Objects.requireNonNull(RegistryManager.FORMS.get(reversionTarget)).REVERSION_TARGET_ID != null) {
				reversionTarget = Objects.requireNonNull(RegistryManager.FORMS.get(reversionTarget)).REVERSION_TARGET_ID;
			}
		}
		if(this.revertTo(reversionTarget) == FormChangeOperationResult.MISSING_PLAYERMODEL) {
			CharaFormAct.LOGGER.warn(
					"{}'s current form ({}) should revert to {}, however this is illegal for their character! ({})",
					player.getName().getString(), this.getFormID(), reversionTarget, this.getCharacterID()
			);
			return ReversionResult.MISSING_PLAYERMODEL;
		}
		player.setHealth(player.getMaxHealth());
		return ReversionResult.SUCCESS;
	}

	@Override public FormChangeOperationResult revertTo(Identifier formID) {
		if(!this.isEnabled()) return FormChangeOperationResult.NOT_ENABLED;
		ParsedForm newForm = Objects.requireNonNull(RegistryManager.FORMS.get(formID),
				"Target form (" + formID + ") doesn't exist!");

		long seed = RandomSeed.getSeed();
		if(!this.setForm(newForm, true, seed)) return FormChangeOperationResult.MISSING_PLAYERMODEL;
		CfaDataPackets.empowerRevertS2C(this.getPlayer(), newForm, true, seed);
		return FormChangeOperationResult.SUCCESS;
	}
	@Override public FormChangeOperationResult revertTo(String formID) {
		return this.revertTo(Identifier.of(formID));
	}

	@Override public FormChangeOperationResult assignForm(Identifier formID) {
		if(!this.isEnabled()) return FormChangeOperationResult.NOT_ENABLED;
		ParsedForm newForm = Objects.requireNonNull(RegistryManager.FORMS.get(formID),
				"Target form (" + formID + ") doesn't exist!");

		if(!this.setFormTransitionless(newForm)) return FormChangeOperationResult.MISSING_PLAYERMODEL;
		CfaDataPackets.assignFormS2C(this.getPlayer(), newForm);
		return FormChangeOperationResult.SUCCESS;
	}
	@Override public FormChangeOperationResult assignForm(String formID) {
		return this.assignForm(Identifier.of(formID));
	}

	@Override public void assignCharacter(Identifier characterID) {
		ParsedCharacter newCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(characterID),
				"Target character (" + characterID + ") doesn't exist!");

		this.setCharacter(newCharacter);
		CfaDataPackets.assignCharacterS2C(this.getPlayer(), newCharacter);
	}
	@Override public void assignCharacter(String characterID) {
		this.assignCharacter(Identifier.of(characterID));
	}
}

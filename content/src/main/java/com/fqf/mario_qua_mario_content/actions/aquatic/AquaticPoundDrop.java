package com.fqf.mario_qua_mario_content.actions.aquatic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario_content.actions.grounded.GroundPoundLand;
import com.fqf.mario_qua_mario_content.stomp_types.AquaticGroundPound;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class AquaticPoundDrop implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("aquatic_ground_pound_drop");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return GroundPoundDrop.makeAnimation(helper);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return new BappingRule(0, 3);
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return AquaticGroundPound.ID;
	}

	public static CharaStat AQUATIC_GROUND_POUND_DRAG = new CharaStat(0.19, WATER_DRAG);
	public static CharaStat AQUATIC_GROUND_POUND_DRAG_MIN = new CharaStat(0.02, WATER_DRAG);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AquaticActionHelper helper) {
		int depthChargeLevel = AquaticGroundPound.getDepthChargeLevel(data.getMario().getEquippedStack(EquipmentSlot.LEGS), data);
		CharaStat drag;
		if(depthChargeLevel == 0) drag = AQUATIC_GROUND_POUND_DRAG;
		else drag = AQUATIC_GROUND_POUND_DRAG.variate(1.0 / (depthChargeLevel + 1));

		helper.applyWaterDrag(data, drag, AQUATIC_GROUND_POUND_DRAG_MIN);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						Submerged.ID,
						data -> data.getYVel() >= -0.01,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				Swim.SWIM
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				Fall.LANDING.variate(
						AquaticPoundLand.ID,
						null, null,
						data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioContentSFX.AQUATIC_GROUND_POUND_DROP);
							data.playSound(MarioContentSFX.AQUATIC_GROUND_POUND_LAND, seed);
						}
				),
				Submerged.EXIT_WATER.variate(
						GroundPoundDrop.ID,
						null, null,
						null,
						(data, isSelf, seed) -> {
							data.stopStoredSound(MarioContentSFX.AQUATIC_GROUND_POUND_DROP);
							data.storeSound(data.playSound(MarioContentSFX.GROUND_POUND_DROP, seed));
						}
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
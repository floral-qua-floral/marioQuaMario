package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.BappingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario.collision_attacks.AquaticGroundPound;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.WATER_DRAG;

public class AquaticPoundDrop implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound_drop");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return GroundPoundDrop.ANIMATION;
	}

	@Override
	public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.add(ActionFlag.PROHIBIT_SPRINT);
		builder.add(ActionFlag.DIVING);
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return new BappingRule(0, 3);
	}
	@Override public void accumulateCollisionAttacks(ImmutableSet.Builder<Identifier> builder) {
		builder.add(AquaticGroundPound.ID);
	}

	private static final StateAttachmentKey<MutableDouble> PREV_Y =
			StateAttachmentKey.action(data -> new MutableDouble(data.getPlayer().getY()));

	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.sustainSound(MarioSFX.AQUATIC_GROUND_POUND_DROP, data.getPlayer(), SoundCategory.PLAYERS);

		MutableDouble prevY = data.get(PREV_Y);
		if(data.getPlayer().getY() - prevY.doubleValue() < -0.1)
			for(int incrementeroo = 0; incrementeroo < 4; incrementeroo++)
				makeBubble(data, 0.3);
		prevY.setValue(data.getPlayer().getY());
	}

	private static void makeBubble(CfaData data, double speedFactor) {
		Vec3d motion = new Vec3d(
				data.getPlayer().getRandom().nextTriangular(0, 1),
				0,
				data.getPlayer().getRandom().nextTriangular(0, 1)
		).normalize();
		data.getPlayer().getWorld().addParticle(
				ParticleTypes.BUBBLE,
				data.getPlayer().getX() + motion.x / 4, data.getPlayer().getY() + 0.15, data.getPlayer().getZ() + motion.z / 4,
				motion.x * speedFactor, 0, motion.z * speedFactor
		);
	}

	public static CfaStat AQUATIC_GROUND_POUND_DRAG = new CfaStat(0.19, WATER_DRAG);
	public static CfaStat AQUATIC_GROUND_POUND_DRAG_MIN = new CfaStat(0.02, WATER_DRAG);

	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		int depthChargeLevel = AquaticGroundPound.getDepthChargeLevel(data.getPlayer().getEquippedStack(EquipmentSlot.LEGS), data);
		CfaStat drag;
		if(depthChargeLevel == 0) drag = AQUATIC_GROUND_POUND_DRAG;
		else drag = AQUATIC_GROUND_POUND_DRAG.variate(1.0 / (depthChargeLevel + 1));

		helper.applyWaterDrag(data, drag, AQUATIC_GROUND_POUND_DRAG_MIN);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				Submerged.ID,
				data -> data.getYVel() >= -0.01,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(Swim.SWIM);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				Fall.LANDING.variate(
						AquaticPoundLand.ID,
						null, null,
						null, data -> data.setForwardStrafeVel(0, 0),
						(data, isSelf, seed) -> {
							data.playSound(MarioSFX.AQUATIC_GROUND_POUND_LAND, seed);
							for(int incrementeroo = 0; incrementeroo < 48; incrementeroo++) {
								makeBubble(data, 1.2);
							}
						}
				),
				Submerged.EXIT_WATER.variate(GroundPoundDrop.ID, null)
		);
	}
}
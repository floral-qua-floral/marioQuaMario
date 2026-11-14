package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.CfaMarioAddon;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.Sideflip;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Skid implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("skid");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.y -= 4;
					arrangement.setAngles(
							0,
							42.5F,
							17.5F
					);
				},
				(posture, data, animationTime, helper) -> {
					posture.HEAD.roll -= 15;

					posture.RIGHT_ARM.addAngles(-32, -35, 80);
					posture.LEFT_ARM.addAngles(-45, 0, -30);
					posture.RIGHT_LEG.addAngles(-57.5F, 45F, -20);
					posture.LEFT_LEG.addPos(0, -4.1F, -3.9F);
					posture.LEFT_LEG.addAngles(5, 15, 0);

					if(posture.TAIL != null)
						posture.TAIL.setAngles(5, 42, -17.5F);
				}
		);
	}

	@Override
	public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.SLIDING);
		builder.add(ActionFlag.PROHIBIT_SPRINT);
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return new BappingRule(0, 0, 3, new CfaStat(0, THRESHOLD));
	}

	public static final StateAttachmentKey<Vec3d> PREV_POS = StateAttachmentKey.action(data -> data.getPlayer().getPos());
	public static final StateAttachmentKey<Vec3d> PREV_MOTION = StateAttachmentKey.action(data -> Vec3d.ZERO);

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {
		float slidingSpeed = (float) ((CfaReadableMotionData) data).getHorizVelSquared();
		data.sustainSound(MarioSFX.SKID, data.getPlayer(), SoundCategory.PLAYERS,
				1.0F + Math.min(0.15F, 0.5F * slidingSpeed),
				Math.min(1.0F, ((float) data.get(DuckSlide.SKID_SOUND_TIMER).getAndIncrement()) / 3.0F) * Math.min(1.0F, 0.4F + 0.7F * slidingSpeed) * 0.4F
		);

		Vec3d deltaPos = getDeltaPos(data, true);
		spawnParticles(data, 2, deltaPos, 1, 1 / Math.max(deltaPos.horizontalLength() * 20, 0.01));
	}

	public static Vec3d getDeltaPos(CfaData data, boolean forceNonZero) {
		Vec3d deltaPos = data.getPlayer().getPos().subtract(data.get(PREV_POS)).multiply(1, 0, 1);
		data.put(PREV_POS, data.getPlayer().getPos());
		if(forceNonZero) {
			if(deltaPos.horizontalLengthSquared() == 0) deltaPos = data.get(PREV_MOTION);
			else data.put(PREV_MOTION, deltaPos);
		}
		return deltaPos;
	}

	public static void spawnParticles(CfaData data, double count, Vec3d deltaPos, double horizontalVel, double yVel) {
		BlockState floorBlock = data.getPlayer().getWorld().getBlockState(data.getPlayer().getSteppingPos());
		if(count > 0 && floorBlock.getRenderType() != BlockRenderType.INVISIBLE && deltaPos.horizontalLengthSquared() != 0) {
			Vec3d generalMotion = deltaPos.normalize();
			Random random = data.getPlayer().getRandom();

			if(count % 1 > random.nextDouble())
				count++;

			for(int incrementeroo = 0; incrementeroo < count; incrementeroo++) {
				float rotation = (float) random.nextTriangular(0, Math.PI / 8);
				Vec3d motion = generalMotion.rotateY(rotation).multiply(horizontalVel);
				Vec3d offset = generalMotion.rotateY(rotation * 2.25F).multiply(random.nextTriangular(0.5, 0.2));
				data.getPlayer().getWorld().addParticle(
						new BlockStateParticleEffect(
								ParticleTypes.BLOCK,
								floorBlock
						),
						data.getPlayer().getX() + offset.x,
						data.getPlayer().getY() + 0.15,
						data.getPlayer().getZ() + offset.z,
						motion.x, yVel, motion.z
				);
			}
		}
	}

	public static final CfaStat SKID_THRESHOLD = new CfaStat(0.285, RUNNING, THRESHOLD);

	public static final CfaStat SKID_DRAG = new CfaStat(0.185, RUNNING, DRAG);
	public static final CfaStat SKID_DRAG_MIN = new CfaStat(0.045, RUNNING, DRAG);
	public static final CfaStat SKID_REDIRECTION = new CfaStat(4.5, RUNNING, REDIRECTION);

	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.get(CfaMarioAddon.ACTION_TIMER).increment();
		helper.applyDrag(
				data, SKID_DRAG, SKID_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SKID_REDIRECTION
		);
	}

	public static final ActionTransitionDetails SKID = new ActionTransitionDetails(
			ID,
			data -> data.getInputs().getForwardInput() < -0.675 && data.getForwardVel() > SKID_THRESHOLD.get(data),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SubWalk.ID,
				data -> data.getHorizVelSquared() == 0 || data.getInputs().getForwardInput() >= 0,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						Sideflip.ID,
						data -> data.getForwardVel() < Sideflip.SIDEFLIP_THRESHOLD.get(data) && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						SizeChangeBehavior.AUTOMATIC,
						data -> {
							helper.performJump(data, Sideflip.SIDEFLIP_VEL, null);
							data.setForwardStrafeVel(Sideflip.SIDEFLIP_BACKWARDS_SPEED.get(data), 0);
							data.forceBodyAlignment(true);
						},
						(data, isSelf, seed) -> {
							data.forceBodyAlignment(true);
							data.playJumpSound(seed);
							data.voice(Voicelines.SIDEFLIP, seed);
						}
				),
				Jump.makeJumpTransition(helper)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL,
				UnderwaterWalk.SUBMERGE,
				BonkAir.BONK.variate(
						BonkGround.BACKWARD_ID,
						null,
						null,
						null, data ->
								data.setVelocity(data.getRecordedCollisions().getHorizontallyReflectedVelocity().multiply(1.25)),
						null
				)
		);
	}
}

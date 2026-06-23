package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.BappingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.google.common.collect.ImmutableList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class LavaBoost extends Fall implements AirborneActionDefinition {
    public static final Identifier ID = MarioQuaMario.makeID("lava_boost");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.pitch += 28.45F;
					arrangement.y -= 6.75F;
				},
				(posture, data, animationTime, helper) -> {
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.addPos(0, 1.25F, 1);
						arrangement.addAngles(120, 88, 90);
					});

					float progress = animationTime / 2.2F;
					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) ->
							arrangement.addAngles(-57.75F + sideFactor * MathHelper.sin(progress) * 40, 0, 0));
				}
		);
	}

	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	public static CfaStat BOOST_VEL = new CfaStat(1.4, JUMP_VELOCITY);

	public static CfaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_FORWARD_SPEED = Fall.FORWARD_DRIFT_SPEED.variate(0.5);
	public static CfaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_BACKWARD_SPEED = Fall.BACKWARD_DRIFT_SPEED.variate(0.5);
	public static CfaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_STRAFE_SPEED = Fall.STRAFE_DRIFT_SPEED.variate(0.5);

	public static CfaStat BOOST_REDIRECTION = new CfaStat(3, DRIFTING, REDIRECTION);

	private static class LavaBoostVars {
		private double bounceVel;
	}
	@Override public @Nullable Object provideStateData(CfaData data) {
		return new LavaBoostVars();
	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		data.retrieveStateData(LavaBoostVars.class).bounceVel = data.getYVel() * -0.6;
		if(data.getYVel() < 0) Fall.drift(data, helper);
		else helper.airborneAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				BOOST_REDIRECTION
		);
	}

	private static final double MAX_EJECTION_DISTANCE = 5;

	public static @Nullable Vec3d findLavaBoostEjectionSpot(CfaData data) {
		PlayerEntity player = data.getPlayer();
		Box playerBoundingBox = player.getBoundingBox();
		Box ejectionCheckBox = playerBoundingBox.withMaxY(playerBoundingBox.minY + MAX_EJECTION_DISTANCE);

		int minX = MathHelper.floor(ejectionCheckBox.minX); int maxX = MathHelper.ceil(ejectionCheckBox.maxX);
		int minY = MathHelper.floor(ejectionCheckBox.minY); int maxY = MathHelper.ceil(ejectionCheckBox.maxY);
		int minZ = MathHelper.floor(ejectionCheckBox.minZ); int maxZ = MathHelper.ceil(ejectionCheckBox.maxZ);
		World world = data.getPlayer().getWorld();
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for(int checkX = minX; checkX < maxX; checkX++) {
			for(int checkY = maxY; checkY >= minY; checkY--) {
				for(int checkZ = minZ; checkZ < maxZ; checkZ++) {
					mutable.set(checkX, checkY, checkZ);
					FluidState fluidState = world.getFluidState(mutable);
					if(fluidState.isIn(MQMTags.PROHIBITS_LAVA_BOOST_EJECTION)) {
						double fluidSurface = checkY + fluidState.getHeight(world, mutable);
						if(fluidSurface < ejectionCheckBox.maxY) {
							// We've found the top of the liquid!
							// Now check if it's actually possible for the player to rise up to this height.
							Box checkCollisionsBox = playerBoundingBox.withMaxY(fluidSurface + player.getHeight());
							if(world.isSpaceEmpty(player, checkCollisionsBox))
								return player.getPos().withAxis(Direction.Axis.Y, fluidSurface);
						}

						// We found lava, but we didn't find the surface of it. Since we start from the top and search
						// downwards, if we haven't found the surface yet, we're for sure not gonna find it even lower.
						return null;
					}
				}
			}
		}

		// Doesn't seem like we're in lava to begin with???? This can be caused by a Lava Cauldron.
		// Trying to lava boost in place is TOO RISKY. If Mario's in a position that causes lava damage every tick and
		// never finds actual lava (such as in a lava cauldron), then lava boosting in place would lock him in that
		// position while he gets cooked to death. Awful...! We could try moving him up an arbitrary amount and then
		// lava boost in place, which would work great for Lava Cauldrons, but bad if you imagine something like a
		// Tinker's Construct smeltery. Mario would get forced up to its ceiling, and every time he sinks he'd trigger
		// a Lava Boost and get forced back up.
		return null;
	}

	public static final ActionTransitionDetails MANUALLY_TRIGGERABLE_LAVA_BOOST = new ActionTransitionDetails(
			LavaBoost.ID,
			data -> false,
			EvaluatorEnvironment.SERVER_ONLY,
			data -> {
				Vec3d lavaBoostEjectionPos = LavaBoost.findLavaBoostEjectionSpot(data);
				if(lavaBoostEjectionPos == null)
					MarioQuaMario.LOGGER.error("Triggered Lava Boost transition, but then couldn't find" +
							" the ejection pos?! Player is at {}", data.getPlayer().getPos());
				else
					data.goTo(lavaBoostEjectionPos);

				data.setYVel(LavaBoost.BOOST_VEL.get(data));
				data.setForwardStrafeVel(0, 0);
			},
			(data, isSelf, seed) -> data.voice(Voicelines.BURNT, seed)
	);

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(
						LavaBoost.ID,
						data ->
								data.getYVel() <= 0
										&& data.retrieveStateData(LavaBoostVars.class).bounceVel > 0.06
										&& Fall.LANDING.evaluator().shouldTransition(data),
						null,
						data -> {
							data.setYVel(data.retrieveStateData(LavaBoostVars.class).bounceVel);
							data.setForwardStrafeVel(data.getForwardVel() * 0.5, data.getStrafeVel() * 0.5);
						},
						(data, isSelf, seed) -> {}
				),
				Fall.LANDING.variate(null, data -> data.getYVel() <= 0 && Fall.LANDING.evaluator().shouldTransition(data)),
				// Mario can start climbing from a Lava Boost, but can't wall-jump
				ClimbTransitions.CLIMB_NON_SOLID_DIRECTIONAL,
				ClimbTransitions.CLIMB_NON_SOLID_NON_DIRECTIONAL,
				ClimbTransitions.CLIMB_SOLID
		);
	}
}
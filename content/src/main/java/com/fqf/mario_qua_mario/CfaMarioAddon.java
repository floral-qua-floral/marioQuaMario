package com.fqf.mario_qua_mario;

import com.fqf.charaformact_api.CharaFormActAddon;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.charaformact_api.definitions.CollisionAttackTypeDefinition;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.charaformact_api.definitions.states.FormDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;
import com.fqf.mario_qua_mario.appearances.mario.*;
import com.fqf.mario_qua_mario.appearances.luigi.*;
import com.fqf.mario_qua_mario.characters.*;
import com.fqf.mario_qua_mario.forms.*;
import com.fqf.mario_qua_mario.actions.generic.*;
import com.fqf.mario_qua_mario.actions.grounded.*;
import com.fqf.mario_qua_mario.actions.airborne.*;
import com.fqf.mario_qua_mario.actions.aquatic.*;
import com.fqf.mario_qua_mario.actions.wallbound.*;
import com.fqf.mario_qua_mario.actions.mounted.*;
import com.fqf.mario_qua_mario.actions.form.*;
import com.fqf.mario_qua_mario.collision_attacks.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

public class CfaMarioAddon implements CharaFormActAddon {
	@Override
	public void accumulateCharacters(ImmutableMap.Builder<Identifier, CharacterDefinition> builder) {
		builder.put(Mario.ID, new Mario());
		builder.put(Luigi.ID, new Luigi());
		builder.put(Toadette.ID, new Toadette());
		builder.put(CustomToad.ID, new CustomToad());
	}

	@Override
	public void accumulateForms(ImmutableMap.Builder<Identifier, FormDefinition> builder) {
		builder.put(Small.ID, new Small());
		builder.put(Super.ID, new Super());
		builder.put(Fire.ID, new Fire());
		builder.put(Raccoon.ID, new Raccoon());
		builder.put(Mini.ID, new Mini());
	}

	private static final boolean INCLUDE_DEBUG_ACTIONS = true;

	@Override
	public void accumulateActionDefinitions(ImmutableMap.Builder<Identifier, IncompleteActionDefinition> builder) {
		// Debug Actions
		if(INCLUDE_DEBUG_ACTIONS) {
			// FIXME: This is currently required!!! Because Debug contains the Lava Boost transition that's used in
			//  AbstractMarioSeriesCharacter.
			builder.put(Debug.ID, new Debug());
			builder.put(DebugSprint.ID, new DebugSprint());
			builder.put(DebugSpinPitch.ID, new DebugSpinPitch());
			builder.put(DebugSpinYaw.ID, new DebugSpinYaw());
			builder.put(DebugSpinRoll.ID, new DebugSpinRoll());
			builder.put(DebugVanillaTravel.ID, new DebugVanillaTravel());
		}

		// Basic grounded movement
		builder.put(SubWalk.ID, new SubWalk());
		builder.put(WalkRun.ID, new WalkRun());
		builder.put(PRun.ID, new PRun());
		builder.put(Skid.ID, new Skid());
		builder.put(UnderwaterWalk.ID, new UnderwaterWalk());

		// Basic airborne actions
		builder.put(Fall.ID, new Fall());
		builder.put(SpecialFall.ID, new SpecialFall());
		builder.put(StompBounce.ID, new StompBounce());
		builder.put(LavaBoost.ID, new LavaBoost());

		// Jumps
		builder.put(Jump.ID, new Jump());
		builder.put(PJump.ID, new PJump());
		builder.put(DoubleJump.ID, new DoubleJump());
		builder.put(TripleJump.ID, new TripleJump());
		builder.put(LongJump.ID, new LongJump());
		builder.put(Backflip.ID, new Backflip());
		builder.put(Sideflip.ID, new Sideflip());
		builder.put(WaterExitJump.ID, new WaterExitJump());
		builder.put(WallJump.ID, new WallJump());

		// Ducking
		builder.put(DuckWaddle.ID, new DuckWaddle());
		builder.put(DuckSlide.ID, new DuckSlide());
		builder.put(UnderwaterDuck.ID, new UnderwaterDuck());
		builder.put(DuckFall.ID, new DuckFall());
		builder.put(DuckJump.ID, new DuckJump());

		// Ground Pounds
		builder.put(GroundPoundFlip.ID, new GroundPoundFlip());
		builder.put(GroundPoundDrop.ID, new GroundPoundDrop());
		builder.put(GroundPoundLandHold.ID, new GroundPoundLandHold());
		builder.put(GroundPoundLand.ID, new GroundPoundLand());
		builder.put(AquaticPoundFlip.ID, new AquaticPoundFlip());
		builder.put(AquaticPoundDrop.ID, new AquaticPoundDrop());
		builder.put(AquaticPoundLand.ID, new AquaticPoundLand());

		// Bonking
		builder.put(BonkAir.ID, new BonkAir());
		builder.put(BonkGroundBackward.ID, new BonkGroundBackward());
		builder.put(BonkGroundForward.ID, new BonkGroundForward()); // TODO: Unite with BonkGroundBackward

		// Swimming
		builder.put(Submerged.ID, new Submerged());
		builder.put(Swim.ID, new Swim());
		builder.put(Paddle.ID, new Paddle());

		// Climbing
		builder.put(ClimbPole.ID, new ClimbPole());
		builder.put(ClimbIntangibleDirectional.ID, new ClimbIntangibleDirectional());
		builder.put(ClimbIntangibleSideHang.ID, new ClimbIntangibleSideHang());
		builder.put(ClimbWall.ID, new ClimbWall());
		builder.put(ClimbWallSideHang.ID, new ClimbWallSideHang());

		// Tail
		builder.put(TailSpinGround.ID, new TailSpinGround());
		builder.put(TailStall.ID, new TailStall());
		builder.put(TailStallDucking.ID, new TailStallDucking());
		builder.put(TailFly.ID, new TailFly());
		builder.put(TailSpinFall.ID, new TailSpinFall());
		builder.put(TailSpinJump.ID, new TailSpinJump());

		// Miscellaneous
		builder.put(WallSlide.ID, new WallSlide());
		builder.put(Mounted.ID, new Mounted());
	}

	@Override
	public void accumulateTransitionInjectionDefinitions(ImmutableList.Builder<TransitionInjectionDefinition> builder) {
		builder.add(
				WalkRun.GROUNDED_INJECTION,
				WalkRun.AIRBORNE_INJECTION,
				SpecialFall.INJECTION,

				DoubleJump.INJECTION,
				TripleJump.INJECTION,

				DuckSlide.GROUNDED_INJECTION,
				DuckSlide.AIRBORNE_INJECTION,

				GroundPoundLandHold.INJECTION,

				WaterExitJump.INJECTION,

				ClimbWallSideHang.INJECTION,
				ClimbIntangibleSideHang.INJECTION,

				TailStall.INJECTION,
				TailStallDucking.INJECTION
		);
	}

	@Override
	public void accumulateVoicelines(ImmutableSet.Builder<Identifier> builder) {
		Voicelines.addAll(builder);
	}

	@Override
	public void accumulateCollisionAttackDefinitions(ImmutableMap.Builder<Identifier, CollisionAttackTypeDefinition> builder) {
		builder.put(Stomp.ID, new Stomp());
		builder.put(GroundPound.ID, new GroundPound());
		builder.put(AquaticGroundPound.ID, new AquaticGroundPound());
	}

	@Override
	public void accumulateCommonAppearances(AppearanceMapBuilder<CommonAppearanceDefinition> builder) {
		builder.putMatching(Mario.ID, Small.ID, new SmallMarioCommonAppearance());
		builder.putMatching(Mario.ID, Super.ID, new SuperMarioCommonAppearance());
		builder.putMatching(Mario.ID, Fire.ID, new SuperMarioCommonAppearance());
		builder.putMatching(Mario.ID, Raccoon.ID, new SuperMarioCommonAppearance());
		builder.putMatching(Mario.ID, Mini.ID, new MiniMarioCommonAppearance());

		builder.putMatching(Luigi.ID, Small.ID, new SmallLuigiCommonAppearance());
		builder.putMatching(Luigi.ID, Super.ID, new SuperLuigiCommonAppearance());
		builder.putMatching(Luigi.ID, Fire.ID, new SuperLuigiCommonAppearance());
		builder.putMatching(Luigi.ID, Raccoon.ID, new FoxLuigiCommonAppearance());
		builder.putMatching(Luigi.ID, Mini.ID, new MiniMarioCommonAppearance());
	}
}

package fqf.qua_mario.mariostates.states.groundbound;

import fqf.qua_mario.Input;
import fqf.qua_mario.MarioClient;
import fqf.qua_mario.VoiceLine;
import fqf.qua_mario.characters.CharaStat;
import fqf.qua_mario.mariostates.GroundedState;
import fqf.qua_mario.mariostates.MarioState;
import fqf.qua_mario.mariostates.states.airborne.DuckJump;

import java.util.ArrayList;
import java.util.List;

public class DuckWaddle extends GroundedState {
	public static final DuckWaddle INSTANCE = new DuckWaddle();

//	public static final MarioStateTransition BACKFLIP = () -> {
//		if(MarioClient.forwardVel <= 0 && MarioClient.forwardInput < 0 && Input.JUMP.isPressed()) {
//			CommonTransitions.performJump(CharaStat.SIDEFLIP_VELOCITY, null);
//			VoiceLine.BACKFLIP.broadcast();
////						return DuckJump.INSTANCE;
//		}
//		return null;
//	};

	private DuckWaddle() {
		this.name = "Duck Waddle";

		preTickTransitions = new ArrayList<>(List.of(
				GroundedTransitions.FALL,
				() -> {
					// Release duck
					if(!Input.DUCK.isHeld()) {
						return Grounded.INSTANCE;
					}
					return null;
				}
		));

		postTickTransitions = new ArrayList<>(List.of(
//				BACKFLIP,
				() -> {
					if(Input.JUMP.isPressed()) {
						GroundedTransitions.performJump(CharaStat.DUCK_JUMP_VELOCITY, null);
						VoiceLine.DUCK_JUMP.broadcast();
						return DuckJump.INSTANCE;
					}
					return null;
				}
		));
	}

	@Override
	public void groundedTick() {
		MarioClient.groundAccel(
				MarioClient.forwardVel >= 0 ? CharaStat.WADDLE_ACCEL : CharaStat.WADDLE_BACKPEDAL_ACCEL,
				MarioClient.forwardVel >= 0 ? CharaStat.WADDLE_SPEED : CharaStat.WADDLE_BACKPEDAL_SPEED,
				1.0,
				CharaStat.WADDLE_STRAFE_ACCEL,
				CharaStat.WADDLE_STRAFE_SPEED,
				1.0,
				CharaStat.WADDLE_REDIRECTION
		);
	}
}

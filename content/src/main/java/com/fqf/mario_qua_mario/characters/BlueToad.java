package com.fqf.mario_qua_mario.characters;

import com.fqf.charapoweract_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.charapoweract_api.util.StatCategory.*;

public class BlueToad extends AbstractMario implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("blue_toad");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull String getVoiceName() {
		return "toad";
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioSFX.TOAD_JUMP;
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of(
				// Blue Toad walks and runs EXTRA faster
				new StatModifier(Set.of(FORWARD, WALKING, SPEED), 1.3),
				new StatModifier(Set.of(FORWARD, RUNNING, SPEED), 1.34),
				new StatModifier(Set.of(FORWARD, P_RUNNING, SPEED), 1.34),

				// Blue Toad's jumps are a lot shorter
				new StatModifier(Set.of(JUMPING_GRAVITY), 1.1),
				new StatModifier(Set.of(JUMP_VELOCITY), 0.885)
		);
	}
}

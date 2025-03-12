package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Luigi extends AbstractMario implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("luigi");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioContentSFX.LUIGI_JUMP;
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of(
				// Luigi walks and runs faster
				new StatModifier(Set.of(FORWARD, WALKING, SPEED), 1.45),
				new StatModifier(Set.of(FORWARD, RUNNING, SPEED), 1.2),
				new StatModifier(Set.of(FORWARD, P_RUNNING, SPEED), 1.2),
				new StatModifier(Set.of(FORWARD, RUNNING, ACCELERATION), 1.2),

				// Luigi jumps higher
				new StatModifier(Set.of(JUMPING_GRAVITY), 0.94),
				new StatModifier(Set.of(JUMP_VELOCITY), 1.05),

				// Luigi is slipperier
				new StatModifier(Set.of(FRICTION), 0.4),
				new StatModifier(Set.of(DRAG), 0.4)
		);
	}
}

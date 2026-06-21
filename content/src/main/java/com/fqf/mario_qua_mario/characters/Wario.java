package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;
import static net.minecraft.entity.attribute.EntityAttributeModifier.Operation.*;
import static net.minecraft.entity.attribute.EntityAttributes.*;

public class Wario extends AbstractMarioSeriesCharacter {
	public static final Identifier ID = MarioQuaMario.makeID("wario");

	@Override public @NotNull String defineVoiceName() {
		return "wario";
	}

	@Override
	public @NotNull SoundEvent defineJumpSound() {
		return MarioSFX.WARIO_JUMP;
	}

	@Override
	public int defineBapStrengthModifier() {
		return 1;
	}

	@Override
	public void accumulateAttributeModifiers(ImmutableSet.Builder<AttributeModifierInstruction> builder) {
		super.accumulateAttributeModifiers(builder);
		builder.add(
				new AttributeModifierInstruction(GENERIC_ATTACK_DAMAGE, 4, ADD_VALUE),
				new AttributeModifierInstruction(GENERIC_ATTACK_SPEED, -0.7, ADD_MULTIPLIED_TOTAL)
		);
	}

	@Override
	public void accumulateStatModifiers(ImmutableSet.Builder<StatModifier> builder) {
		builder.add(
				// Wario deals more damage
				new StatModifier(Set.of(StatCategory.DAMAGE), (base, categories) -> base + 4),

				// Wario walks and runs slower in all directions
				new StatModifier(Set.of(WALKING, SPEED), 0.8),
				new StatModifier(Set.of(RUNNING, SPEED), 0.65),
				new StatModifier(Set.of(P_RUNNING, SPEED), 0.65),

				// Wario's jumps are a lot shorter
				new StatModifier(Set.of(JUMP_VELOCITY), 0.87)
		);
	}
}

package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Wario extends AbstractMarioSeriesCharacter {
	public static final Identifier ID = MarioQuaMario.makeID("wario");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioSFX.WARIO_JUMP;
	}

	@Override
	public int getBapStrengthModifier() {
		return 1;
	}

	@Override
	public Set<AttributeModifierInstruction> getAttributeModifiers() {
		return Set.of(
				new AttributeModifierInstruction(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4, EntityAttributeModifier.Operation.ADD_VALUE),
				new AttributeModifierInstruction(EntityAttributes.GENERIC_ATTACK_SPEED, -0.7, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
		);
	}

	@Override
	public Set<StatModifier> getStatModifiers() {
		return Set.of(
				// Wario deals more damage
				new StatModifier(Set.of(StatCategory.DAMAGE), (base, categories) -> base + 4),

				// Wario walks and runs slower in all directions
				new StatModifier(Set.of(WALKING, SPEED), 0.8),
				new StatModifier(Set.of(RUNNING, SPEED), 0.6),
				new StatModifier(Set.of(P_RUNNING, SPEED), 0.6),

				// Wario's jumps are a lot shorter
				new StatModifier(Set.of(JUMP_VELOCITY), 0.87)
		);
	}
}

package com.floralquafloral.registries.states.character.basecharacters;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.character.CharacterDefinition;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

import static com.floralquafloral.stats.StatCategory.*;

public class Luigi implements CharacterDefinition {
	@Override
	public void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers) {
		// Luigi walks and runs faster
		modifiers.put(Set.of(FORWARD, WALKING, SPEED), 1.45);
		modifiers.put(Set.of(FORWARD, RUNNING, SPEED), 1.2);
		modifiers.put(Set.of(FORWARD, P_RUNNING, SPEED), 1.2);
		modifiers.put(Set.of(FORWARD, RUNNING, ACCELERATION), 1.2);

		// Luigi jumps higher
		modifiers.put(Set.of(JUMPING_GRAVITY), 0.945);
		modifiers.put(Set.of(JUMP_VELOCITY), 1.05);

		// Luigi is slipperier
		modifiers.put(Set.of(FRICTION), 0.4);
		modifiers.put(Set.of(DRAG), 0.4);
	}

	@Override
	public float getWidthFactor() {
		return 1.0F;
	}

	@Override
	public float getHeightFactor() {
		return 1.0F;
	}

	@Override
	public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "luigi");
	}

	@Override
	public void selfTick(MarioClientData data) {

	}

	@Override
	public void otherClientsTick(MarioPlayerData data) {

	}

	@Override
	public void serverTick(MarioPlayerData data) {

	}
}

package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Stomp extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "stomp");
	}

	public static final CharaStat CAP = new CharaStat(0.65, StatCategory.JUMP_CAP);

	@Override protected @Nullable CharaStat getJumpCap() {
		return CAP;
	}
}

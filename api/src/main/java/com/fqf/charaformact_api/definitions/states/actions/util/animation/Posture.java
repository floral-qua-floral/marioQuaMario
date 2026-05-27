package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Posture {
	public final @NotNull Arrangement HEAD, TORSO;
	public final @NotNull Arrangement RIGHT_ARM, LEFT_ARM;
	public final @NotNull Arrangement RIGHT_LEG, LEFT_LEG;

	public final @Nullable Arrangement TAIL;
	public final @Nullable Arrangement RIGHT_EAR, LEFT_EAR;

	public Posture(Arrangement[] arrangements) {
		this.HEAD = arrangements[0]; this.TORSO = arrangements[1];
		this.RIGHT_ARM = arrangements[2]; this.LEFT_ARM = arrangements[3];
		this.RIGHT_LEG = arrangements[4]; this.LEFT_LEG = arrangements[5];
		this.TAIL = arrangements[6]; this.RIGHT_EAR = arrangements[7]; this.LEFT_EAR = arrangements[8];
	}
}

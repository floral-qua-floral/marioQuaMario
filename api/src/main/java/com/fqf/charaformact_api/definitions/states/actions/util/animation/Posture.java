package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Posture {
	public final @NotNull Arrangement EVERYTHING, HEAD, TORSO;
	public final @NotNull Arrangement RIGHT_ARM, LEFT_ARM;
	public final @NotNull Arrangement RIGHT_LEG, LEFT_LEG;

	public final @Nullable Arrangement TAIL;
	public final @Nullable Arrangement RIGHT_EAR, LEFT_EAR;

	public Posture(Arrangement[] arrangements) {
		this.EVERYTHING = arrangements[0]; this.HEAD = arrangements[1]; this.TORSO = arrangements[2];
		this.RIGHT_ARM = arrangements[3]; this.LEFT_ARM = arrangements[4];
		this.RIGHT_LEG = arrangements[5]; this.LEFT_LEG = arrangements[6];
		this.TAIL = arrangements[7]; this.RIGHT_EAR = arrangements[8]; this.LEFT_EAR = arrangements[9];
	}
}

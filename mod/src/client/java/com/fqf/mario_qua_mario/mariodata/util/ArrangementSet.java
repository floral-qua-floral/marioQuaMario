package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;

public class ArrangementSet {
	public final Arrangement EVERYTHING;
	public final Arrangement HEAD;
	public final Arrangement BODY;

	public final Arrangement RIGHT_ARM;
	public final Arrangement LEFT_ARM;

	public final Arrangement RIGHT_LEG;
	public final Arrangement LEFT_LEG;

	public final Arrangement CAPE;

	public ArrangementSet() {
		this.EVERYTHING = new Arrangement(0, 0, 0);
		this.HEAD = new Arrangement(0, 0, 0);
		this.BODY = new Arrangement(0, 0, 0);

		this.RIGHT_ARM = new Arrangement(0, 0, 0);
		this.LEFT_ARM = new Arrangement(0, 0, 0);

		this.RIGHT_LEG = new Arrangement(0, 0, 0);
		this.LEFT_LEG = new Arrangement(0, 0, 0);

		this.CAPE = new Arrangement(0, 0, 0);
	}
}

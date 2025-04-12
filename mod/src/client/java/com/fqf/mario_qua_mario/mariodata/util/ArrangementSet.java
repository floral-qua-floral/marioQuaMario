package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.Arrangement;

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
		this.EVERYTHING = new Arrangement();
		this.HEAD = new Arrangement();
		this.BODY = new Arrangement();

		this.RIGHT_ARM = new Arrangement();
		this.LEFT_ARM = new Arrangement();

		this.RIGHT_LEG = new Arrangement();
		this.LEFT_LEG = new Arrangement();

		this.CAPE = new Arrangement();
	}
}

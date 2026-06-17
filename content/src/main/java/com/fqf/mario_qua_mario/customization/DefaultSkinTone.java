package com.fqf.mario_qua_mario.customization;

import java.util.Locale;

public enum DefaultSkinTone {
	MARIO(0xFFFFDB99),
	TOADETTE(0xFFEDC19F),
	ALEX(0xFFEFDABF),
	ARI(0xFFF9A786),
	EFE(0xFFAB724C),
	KAI(0xFFDF9658),
	MAKENA(0xFF443528),
	NOOR(0xFFB9674A),
	STEVE(0xFFB3795E),
	SUNNY(0xFFF29F5F),
	ZURI(0xFF7E5337);

	public static final DefaultSkinTone[] AVAILABLE_RANDOMLY = new DefaultSkinTone[]{
			ALEX, ARI, EFE, KAI, MAKENA,
			NOOR, STEVE, SUNNY, ZURI
	};

	public final int ARGB;
	DefaultSkinTone(int argb) {
		this.ARGB = argb;
	}
	public int getARBG() {
		return this.ARGB;
	}

	public String getName() {
		return this.toString().toLowerCase(Locale.ROOT);
	}
}

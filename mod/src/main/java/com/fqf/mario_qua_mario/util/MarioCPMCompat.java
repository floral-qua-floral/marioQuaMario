package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class MarioCPMCompat implements ICPMPlugin {

	@Override
	public void initClient(IClientAPI iClientAPI) {

	}

	@Override
	public void initCommon(ICommonAPI iCommonAPI) {

	}

	@Override
	public String getOwnerModId() {
		return MarioQuaMario.MOD_ID;
	}
}

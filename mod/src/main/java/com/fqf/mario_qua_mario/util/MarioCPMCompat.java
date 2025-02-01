package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class MarioCPMCompat implements ICPMPlugin {
	private static IClientAPI clientAPI;
	private static ICommonAPI commonAPI;

	public static IClientAPI getClientAPI() {
		return clientAPI;
	}
	public static ICommonAPI getCommonAPI() {
		return commonAPI;
	}

	@Override public String getOwnerModId() {
		return MarioQuaMario.MOD_ID;
	}
	@Override public void initClient(IClientAPI iClientAPI) {
		clientAPI = iClientAPI;
	}
	@Override public void initCommon(ICommonAPI iCommonAPI) {
		commonAPI = iCommonAPI;
	}
}

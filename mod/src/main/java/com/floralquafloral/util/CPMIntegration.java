package com.floralquafloral.util;

import com.floralquafloral.MarioQuaMario;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

public class CPMIntegration implements ICPMPlugin {
	@Override public String getOwnerModId() {
		return MarioQuaMario.MOD_ID;
	}

	public static IClientAPI clientAPI;
	public static ICommonAPI commonAPI;

	@Override
	public void initClient(IClientAPI api) {
		clientAPI = api;
	}

	@Override
	public void initCommon(ICommonAPI api) {
		commonAPI = api;
	}
}

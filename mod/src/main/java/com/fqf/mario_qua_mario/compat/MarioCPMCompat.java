package com.fqf.mario_qua_mario.compat;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.tom.cpm.api.CommonApi;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class MarioCPMCompat implements ICPMPlugin {
	private static IClientAPI clientAPI;
	private static ICommonAPI commonAPI;
	private static boolean instantiated = false;

	public MarioCPMCompat() {
		instantiated = true;
	}
	public static boolean isRegistered() {
		return instantiated;
	}

	public static IClientAPI getClientAPI() {
		return Objects.requireNonNull(clientAPI, "Client API is null; Mario qua Mario CPM Plugin not registered?!");
	}
	public static @NotNull ICommonAPI getCommonAPI() {
		return Objects.requireNonNull(commonAPI, "Common API is null; Mario qua Mario CPM Plugin not registered?!");
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

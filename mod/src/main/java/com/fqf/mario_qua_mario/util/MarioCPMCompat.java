package com.fqf.mario_qua_mario.util;

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
		return clientAPI;
	}
	public static @NotNull ICommonAPI getCommonAPI() {
//		if(commonAPI == null) {
//			MarioQuaMario.LOGGER.error("Common API is null; Mario qua Mario CPM Plugin not registered?!");
//			return new CPMCommonApiExtender(); // this is SO DUMB >:(
//		}

		return Objects.requireNonNull(commonAPI, "Common API is null; Mario qua Mario CPM Plugin not registered?!");
	}

	private static class CPMCommonApiExtender extends CommonApi {

	}

	@Override public String getOwnerModId() {
		return MarioQuaMario.MOD_ID;
	}
	@Override public void initClient(IClientAPI iClientAPI) {
		clientAPI = iClientAPI;
	}
	@Override public void initCommon(ICommonAPI iCommonAPI) {
		commonAPI = iCommonAPI;
		MarioQuaMario.LOGGER.info("CPM API received by Mario qua Mario!");
	}

	public static void ensureAPI() {

	}
}

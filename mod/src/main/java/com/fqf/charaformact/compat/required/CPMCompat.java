package com.fqf.charaformact.compat.required;

import com.fqf.charaformact.CharaFormAct;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CPMCompat implements ICPMPlugin {
	private static IClientAPI clientAPI;
	private static ICommonAPI commonAPI;
	private static boolean instantiated = false;

	public CPMCompat() {
		instantiated = true;
	}
	public static boolean isRegistered() {
		return instantiated;
	}

	public static IClientAPI getClientAPI() {
		return Objects.requireNonNull(clientAPI, "Client API is null; CharaFormAct CPM Plugin not registered?!");
	}
	public static @NotNull ICommonAPI getCommonAPI() {
		return Objects.requireNonNull(commonAPI, "Common API is null; CharaFormAct CPM Plugin not registered?!");
	}

	@Override public String getOwnerModId() {
		return CharaFormAct.MOD_ID;
	}
	@Override public void initClient(IClientAPI iClientAPI) {
		clientAPI = iClientAPI;
	}
	@Override public void initCommon(ICommonAPI iCommonAPI) {
		commonAPI = iCommonAPI;
	}
}

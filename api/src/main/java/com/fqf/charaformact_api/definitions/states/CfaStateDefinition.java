package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CfaStateDefinition {
	@NotNull Identifier defineID();

	default @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	default void clientTick(CfaClientData data, boolean isSelf) {

	}
	default void serverTick(CfaAuthoritativeData data) {

	}

	default void onEnter(CfaData data) {

	}
	default void onExit(CfaData data) {

	}
}

package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CfaStateDefinition {
	@NotNull Identifier getID();

	@Nullable Object provideStateData(CfaData data);
	void clientTick(CfaClientData data, boolean isSelf);
	void serverTick(CfaAuthoritativeData data);
}

package com.fqf.charapoweract_api.definitions.states;

import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPAData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CPAStateDefinition {
	@NotNull Identifier getID();

	@Nullable Object provideStateData(ICPAData data);
	void clientTick(ICPAClientData data, boolean isSelf);
	void serverTick(ICPAAuthoritativeData data);
}

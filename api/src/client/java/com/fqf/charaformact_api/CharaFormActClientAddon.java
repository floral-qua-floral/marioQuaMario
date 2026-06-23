package com.fqf.charaformact_api;

import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;

public interface CharaFormActClientAddon {
	default void accumulateClientAppearances(AppearanceMapBuilder<ClientAppearanceDefinition> builder) {

	}
}

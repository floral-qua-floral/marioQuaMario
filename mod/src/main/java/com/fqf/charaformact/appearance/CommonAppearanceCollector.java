package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.AppearanceKey;
import com.fqf.charaformact_api.CharaFormActAddon;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;

public class CommonAppearanceCollector extends AbstractAppearanceCollector<CommonAppearanceDefinition, ParsedCommonAppearance> {
	public static final CommonAppearanceCollector INSTANCE = new CommonAppearanceCollector();

	@Override
	protected Map<AppearanceKey.Registerable, CommonAppearanceDefinition> getDefinitions() {
		AppearanceMapBuilderImpl<CommonAppearanceDefinition> builder = new AppearanceMapBuilderImpl<>();
		for(CharaFormActAddon addon : RegistryManager.getAndClearAddons()) {
			addon.accumulateCommonAppearances(builder);
		}
		return builder.build();
	}

	@Override
	protected ParsedCommonAppearance parse(AppearanceKey.Registerable key, CommonAppearanceDefinition definition) {
		return new ParsedCommonAppearance(key.ID, definition);
	}

	@Override
	protected ParsedCommonAppearance refine(ParsedCommonAppearance from) {
		return from;
	}

	public void validate(Map<Identifier, Pair<AppearanceKey, ParsedCommonAppearance>> clientValidationMap) {
		StringBuilder discrepancies = new StringBuilder();
		Set<Identifier> allKeys = new ImmutableSet.Builder<Identifier>()
				.addAll(clientValidationMap.keySet())
				.addAll(this.validationMap.keySet()).build();

		for(Identifier id : allKeys) {
			Pair<AppearanceKey, ParsedCommonAppearance> commonPair = this.validationMap.get(id);
			Pair<AppearanceKey, ParsedCommonAppearance> clientPair = clientValidationMap.get(id);

			if(commonPair == null && clientPair == null) {
				throw new IllegalStateException("Something has gone terribly wrong during CharaFormAct's Appearance validation!");
			}
			else if(commonPair == null)
				discrepancies.append(id).append(" is registered client-side at the intersection of ")
						.append(clientPair.getLeft()).append(", but is missing common-side!\n");
			else if(clientPair == null)
				discrepancies.append(id).append(" is registered common-side at the intersection of ")
						.append(commonPair.getLeft()).append(", but is missing client-side!\n");
			else {
				ParsedCommonAppearance commonAppearance = commonPair.getRight();
				ParsedCommonAppearance clientAppearance = clientPair.getRight();

				float commonStrideLength = commonAppearance.STRIDE_LENGTH;
				float clientStrideLength = clientAppearance.STRIDE_LENGTH;
				if(commonStrideLength != clientStrideLength)
					discrepancies.append(id).append("'s Stride Length is not equal between client & server: ")
							.append(clientStrideLength).append(" on client, but ")
							.append(commonStrideLength).append(" on server!\n");

				float commonArmLength = commonAppearance.ARM_LENGTH;
				float clientArmLength = clientAppearance.ARM_LENGTH;
				if(commonArmLength != clientArmLength)
					discrepancies.append(id).append("'s Arm Length is not equal between client & server: ")
							.append(clientArmLength).append(" on client, but ")
							.append(commonArmLength).append(" on server!\n");
			}
		}

		if(!discrepancies.isEmpty()) {
			if(CharaFormAct.CONFIG.doValidateAppearances()) {
				throw new IllegalStateException("""
					This crash was triggered deliberately by CharaFormAct, because another mod which uses it has failed\
					 to properly register its Appearances (player models). Try looking at the namespaced IDs at the\
					 start of the following lines to figure out which mod bungled it. If this is your own mod, you may\
					 have forgotten to register an Appearance on one side, or you may have failed to make the Client\
					 and Common definitions match up where necessary. If you're a player, please report this crash to\
					 the creator of the mod responsible. You can bypass this validation by disabling Appearance \
					 Validation in CharaFormAct's config.
					\t""" + discrepancies);
			}
			else {
				CharaFormAct.LOGGER.error("Appearance validation failed, but was suppressed!");
			}
		}

		// Clear Common validation map, we don't need it anymore!
		this.validationMap = null;
	}
}

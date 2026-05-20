package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import net.minecraft.util.Identifier;

import java.util.*;

public class CommonAppearanceCollector extends AbstractAppearanceCollector<CommonAppearanceDefinition, ParsedCommonAppearance> {
	public static final CommonAppearanceCollector INSTANCE = new CommonAppearanceCollector();

	@Override protected String getEntrypoint() {
		return "cfa-common-appearances";
	}
	@Override protected Class<CommonAppearanceDefinition> getEntrypointClass() {
		return CommonAppearanceDefinition.class;
	}

	@Override
	protected ParsedCommonAppearance parse(CommonAppearanceDefinition definition, ParsedCharacter character, ParsedForm form) {
		return new ParsedCommonAppearance(definition, character, form);
	}

	public void validate(Map<CharacterFormCombo, Identifier> clientMap) {
		StringBuilder discrepancies = new StringBuilder();
		Set<CharacterFormCombo> allKeys = new HashSet<>(clientMap.keySet());
		allKeys.addAll(this.map.keySet());
		for(CharacterFormCombo key : allKeys) {
			Identifier clientID = clientMap.get(key);
			Identifier commonID = this.map.containsKey(key) ? this.map.get(key).ID : null;
			if(!Objects.equals(commonID, clientID)) {
				if(clientID == null) discrepancies.append(commonID).append(" was never registered on the client!\n\t");
				else if(commonID == null) discrepancies.append(clientID).append(" was never registered common-side!\n\t");
				else discrepancies.append("Two different IDs (").append(clientID).append(" and ").append(commonID)
							.append(") were both registered to the same Character + Form intersection (").append(key)
							.append(")!\n\t");
			}
		}

		String discrepanciesString = discrepancies.toString();
		if(!discrepancies.isEmpty()) {
			CharaFormAct.LOGGER.info("Yikes! The same appearances were not registered between the two entrypoints!");
			throw new IllegalStateException("""
				This crash was triggered deliberately by CharaFormAct, because another mod which uses it has failed to \
				 properly register its Appearances (player models). Try looking at the namespaced IDs at the start of\
				 the following lines to figure out which mod bungled it. If this is your own mod, you probably forgot to\
				 add an Appearance Definition to one of the entrypoints in fabric.mod.json. If you're a player, please\
				 report this crash to the creator of the mod responsible.
				\t""" + discrepanciesString);
		}
	}
}

package com.fqf.mario_qua_mario;

import com.fqf.charaformact_api.CharaFormActClientAddon;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;
import com.fqf.mario_qua_mario.appearances.luigi.FoxLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.luigi.SmallLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.luigi.SuperLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.mario.*;
import com.fqf.mario_qua_mario.characters.*;
import com.fqf.mario_qua_mario.forms.*;

public class CfaMarioClientAddon implements CharaFormActClientAddon {
	@Override
	public void accumulateClientAppearances(AppearanceMapBuilder<ClientAppearanceDefinition> builder) {
		builder.putMatching(Mario.ID, Small.ID, new SmallMarioClientAppearance());
		builder.putMatching(Mario.ID, Super.ID, new SuperMarioClientAppearance());
		builder.putMatching(Mario.ID, Fire.ID, new SuperMarioClientAppearance());
		builder.putMatching(Mario.ID, Raccoon.ID, new RaccoonMarioClientAppearance());
		builder.putMatching(Mario.ID, Mini.ID, new MiniMarioClientAppearance());

		builder.putMatching(Luigi.ID, Small.ID, new SmallLuigiClientAppearance());
		builder.putMatching(Luigi.ID, Super.ID, new SuperLuigiClientAppearance());
		builder.putMatching(Luigi.ID, Fire.ID, new SuperLuigiClientAppearance());
		builder.putMatching(Luigi.ID, Raccoon.ID, new FoxLuigiClientAppearance());
		builder.putMatching(Luigi.ID, Mini.ID, new MiniMarioClientAppearance());
	}
}

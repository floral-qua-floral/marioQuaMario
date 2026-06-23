package com.fqf.mario_qua_mario;

import com.fqf.charaformact_api.CharaFormActClientAddon;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.charaformact_api.util.AppearanceMapBuilder;
import com.fqf.mario_qua_mario.appearances.luigi.FoxLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.luigi.SmallLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.luigi.SuperLuigiClientAppearance;
import com.fqf.mario_qua_mario.appearances.mario.*;
import com.fqf.mario_qua_mario.appearances.toads.MiniToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.RaccoonToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.SmallToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.SuperToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.custom.MiniCustomToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.custom.RaccoonCustomToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.custom.SmallCustomToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.toads.custom.SuperCustomToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.util.CustomizableTextureLayerFeature;
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

		builder.putMatching(Toadette.ID, Small.ID, new SmallToadClientAppearance());
		builder.putMatching(Toadette.ID, Super.ID, new SuperToadClientAppearance());
		builder.putMatching(Toadette.ID, Fire.ID, new SuperToadClientAppearance());
		builder.putMatching(Toadette.ID, Raccoon.ID, new RaccoonToadClientAppearance());
		builder.putMatching(Toadette.ID, Mini.ID, new MiniToadClientAppearance());

		builder.putMatching(CustomToad.ID, Small.ID, new SmallCustomToadClientAppearance());
		builder.putMatching(CustomToad.ID, Super.ID, new SuperCustomToadClientAppearance(CustomizableTextureLayerFeature.SpotsMode.DEFAULT));
		builder.putMatching(CustomToad.ID, Fire.ID, new SuperCustomToadClientAppearance(CustomizableTextureLayerFeature.SpotsMode.HARDCODED));
		builder.putMatching(CustomToad.ID, Raccoon.ID, new RaccoonCustomToadClientAppearance());
		builder.putMatching(CustomToad.ID, Mini.ID, new MiniCustomToadClientAppearance());
	}
}

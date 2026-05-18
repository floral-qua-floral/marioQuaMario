package com.fqf.charaformact.models;

import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.model.CharacterFormEntityModel;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class ParsedCharacterFormModel {
	private final CharacterFormModelDefinition DEFINITION;

	public final Identifier ID;
	public final ParsedCharacter CHARACTER;
	public final ParsedForm FORM;

	public final EntityModelLayer LAYER;
	public final Identifier TEXTURE_LOCATION;
	public final int TEXTURE_WIDTH, TEXTURE_HEIGHT;

	private CharacterFormEntityModel model;
	private CharacterFormRenderer renderer;

	public final float HELD_ITEM_X_TRANSLATION, HELD_ITEM_Y_TRANSLATION, HELD_ITEM_Z_TRANSLATION;
	public final float HELD_SHIELD_X_TRANSLATION, HELD_SHIELD_Y_TRANSLATION, HELD_SHIELD_Z_TRANSLATION;

	public final float LIMB_SWING_MULTIPLIER;

	public ParsedCharacterFormModel(CharacterFormModelDefinition definition, ParsedCharacter character, ParsedForm form) {
		DEFINITION = definition;

		this.ID = definition.getCharacterID();
		this.CHARACTER = character;
		this.FORM = form;

		this.LAYER = definition.getModelLayer();
		this.TEXTURE_LOCATION = definition.getTextureLocation();
		Vector2i textureSize = definition.getTextureSize();
		this.TEXTURE_WIDTH = textureSize.x;
		this.TEXTURE_HEIGHT = textureSize.y;

		// CharacterFormModelDefinition's held item offsets are at a different orientation than vanilla uses in its held
		// item render logic. Account for that here.
		Vector3f itemOffset = definition.getHeldItemPosition().div(16);
		this.HELD_ITEM_X_TRANSLATION = itemOffset.x;
		this.HELD_ITEM_Y_TRANSLATION = itemOffset.z; // swapped with Z
		this.HELD_ITEM_Z_TRANSLATION = itemOffset.y; // swapped with Y
		Vector3f shieldOffset = definition.getHeldShieldPosition().div(16);
		this.HELD_SHIELD_X_TRANSLATION = shieldOffset.x;
		this.HELD_SHIELD_Y_TRANSLATION = shieldOffset.z; // swapped with Z
		this.HELD_SHIELD_Z_TRANSLATION = shieldOffset.y; // swapped with Y

		this.LIMB_SWING_MULTIPLIER = definition.getLimbSwingMultiplier();
	}

	public CharacterFormEntityModel getModel(EntityRendererFactory.Context ctx) {
		if(this.model == null) this.model = this.DEFINITION.createModel(ctx.getPart(this.LAYER));
		return this.model;
	}

	public void setRenderer(CharacterFormRenderer renderer) {
		this.renderer = renderer;
	}

	public CharacterFormRenderer getRenderer() {
		return this.renderer;
	}
}

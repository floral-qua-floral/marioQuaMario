package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.AppearanceHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.MarioQuaMarioClient;
import com.fqf.mario_qua_mario.appearances.util.MqmAppearanceModel;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.forms.Small;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SmallMarioClientAppearance implements ClientAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("small_mario");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Mario.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Small.ID;
	}

	@Override public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 4, 6);
	}

	@Override
	public Vector3i getArmSize() {
		return SmallMarioCommonAppearance.ARM_SIZE;
	}

	@Override
	public Vector3i getLegSize() {
		return SmallMarioCommonAppearance.LEG_SIZE;
	}

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceHelper helper) {
		ModelPartData head = ClientAppearanceDefinition.super.makeHead(root, helper);
		PlumberClientAppearance.addNose(head, this.getHeadSize(), new Vector3i(3, 2, 2), new Vector2i(12, 16), helper);
		return head;
	}
}

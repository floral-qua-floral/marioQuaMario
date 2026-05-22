package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.charaformact_api.appearance.AppearanceHelper;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.util.RaccoonUtil;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;

public class FoxLuigiClientAppearance extends AbstractLuigiClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("fox_luigi");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Raccoon.ID;
	}

	@Override
	public ModelPartData makeTorso(ModelPartData root, AppearanceHelper helper) {
		ModelPartData torso = super.makeTorso(root, helper);
		RaccoonUtil.addTail(torso, this.getTorsoSize(), new Vector2i(0, 68), helper);
		return torso;
	}

	@Override
	protected ModelPartData makeCapStateHead(ModelPartData headPart, AppearanceHelper helper, boolean hasCap) {
		ModelPartData capStateHead = super.makeCapStateHead(headPart, helper, hasCap);

		Vector3f earPivot = new Vector3f(this.getHeadSize().x / 2F - 2.5F, -this.getHeadSize().y, 1);
		if(!hasCap) earPivot.add(1, -1, 0);

		RaccoonUtil.addEars(
				capStateHead,
				earPivot,
				new Vector2i(0, 48), new Vector2i(24, 48), new Vector2i(30, 48),
				helper);
		return capStateHead;
	}
}

package com.fqf.mario_qua_mario.appearances.toads;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.mario_qua_mario.appearances.util.RaccoonUtil;
import net.minecraft.client.model.ModelPartData;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class RaccoonToadClientAppearance extends SuperToadClientAppearance {

	@Override
	public ModelPartData makeHead(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData head = super.makeHead(root, helper);

		ModelPartData bulb = head.getChild(CAP_BULB);
		Vector3i bulbSize = this.getCapBulbSize();
		Vector3f pivot = new Vector3f(bulbSize.x / 2F - 2.5F, -bulbSize.y - 1, 0);
		RaccoonUtil.addEars(bulb, pivot, new Vector2i(36, 39), new Vector2i(36, 45), new Vector2i(42, 39), helper);

		return head;
	}

	@Override
	public ModelPartData makeTorso(ModelPartData root, AppearanceGeometryHelper helper) {
		ModelPartData torso = super.makeTorso(root, helper);
		RaccoonUtil.addTail(torso, this.getTorsoSize(), new Vector2i(36, 69), helper);
		return torso;
	}
}

package com.fqf.mario_qua_mario.appearances.toads;

import com.fqf.charaformact_api.appearance.AppearanceFeatureHelper;
import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.charaformact_api.appearance.TransformationInstructions;
import com.fqf.mario_qua_mario.forms.Mini;
import com.fqf.mario_qua_mario.forms.Small;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public abstract class AbstractMiniToadClientAppearance extends AbstractToadClientAppearance {
	@Override
	public @NotNull Identifier getFormID() {
		return Mini.ID;
	}

	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(32, 48);
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(4, 2, 4);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(4, 2, 2);
	}

	@Override
	public Vector3i getLegSize() {
		return AbstractMiniToadCommonAppearance.LEG_SIZE;
	}

	@Override
	public Vector3i getArmSize() {
		return AbstractMiniToadCommonAppearance.ARM_SIZE;
	}

	@Override
	public Vector3i getCapBulbSize() {
		return new Vector3i(6, 2, 6);
	}

	@Override
	public Vector3i getPigtailBottomSize() {
		return new Vector3i(2);
	}

	@Override
	public Vector3f getBulbPivot() {
		return super.getBulbPivot().add(0, -0.5F, 0);
	}

	@Override
	public Vector3f getPigtailTopPivot() {
		return super.getPigtailTopPivot().add(0.25F, 0.5F, 0);
	}

	@Override
	public Vector2i getCapBulbUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, 14);
	}

	@Override
	public Vector2i getPigtailTopUV() {
		return new Vector2i(0, 14);
	}

	@Override
	public Vector2i getPigtailBottomUV() {
		return new Vector2i(18, 22);
	}

	@Override
	public TransformationInstructions getBootsTransformation(AppearanceFeatureHelper helper) {
		return super.getBootsTransformation(helper).offset(0, 1.5F, 0);
	}
}

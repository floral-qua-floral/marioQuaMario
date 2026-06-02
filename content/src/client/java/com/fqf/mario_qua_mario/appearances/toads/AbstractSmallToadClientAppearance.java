package com.fqf.mario_qua_mario.appearances.toads;

import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.mario_qua_mario.forms.Small;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public abstract class AbstractSmallToadClientAppearance extends AbstractToadClientAppearance {
	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(8, 3, 8);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 4, 4);
	}

	@Override
	public Vector3i getCapBulbSize() {
		return new Vector3i(12, 6, 12);
	}

	@Override
	public Vector2i getCapBulbUV(AppearanceGeometryHelper helper) {
		return new Vector2i(0, 31);
	}

	@Override
	public Vector2i getPigtailTopUV() {
		return new Vector2i(0, 31);
	}

	@Override
	public Vector2i getPigtailBottomUV() {
		return new Vector2i(48, 31);
	}

	@Override
	public Vector3i getLegSize() {
		return AbstractSmallToadCommonAppearance.LEG_SIZE;
	}

	@Override
	public Vector3i getArmSize() {
		return AbstractSmallToadCommonAppearance.ARM_SIZE;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Small.ID;
	}
}

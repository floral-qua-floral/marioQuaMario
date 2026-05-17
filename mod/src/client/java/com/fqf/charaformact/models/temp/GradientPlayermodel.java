package com.fqf.charaformact.models.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import net.minecraft.client.model.ModelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class GradientPlayermodel implements CharacterFormModelDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("gradient");
	}
	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "toadette");
	}
	@Override public @NotNull Identifier getFormID() {
		return Identifier.of("mario_qua_mario", "super");
	}

	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return CharaFormAct.makeID("textures/entity/player/uwu/gradient.png");
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(6, 10, 6);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(9, 5, 6);
	}

	@Override
	public Vector3i getArmSize() {
		return new Vector3i(6, 20, 2);
	}

	@Override
	public Vector3i getLegSize() {
		return new Vector3i(2, 36, 2);
	}

	@Override
	public ModelData getModelData(CharacterFormModelHelper helper) {
		CharaFormAct.LOGGER.info("""
				Gradient model information:
				\tHead UV @ {}, {}  ->  {}, {}
				\tHat UV @ {}, {}  ->  {}, {}
				\tTorso UV @ {}, {}  ->  {}, {}
				\tJacket UV @ {}, {}  ->  {}, {}
				\tLeg UV @ {}, {}  ->  {}, {}
				\tPants UV @ {}, {}  ->  {}, {}
				\tArm UV @ {}, {}  ->  {}, {}
				\tSleeve UV @ {}, {}  ->  {}, {}""",
				this.getHeadUV().x, this.getHeadUV().y,
				helper.getBottomRightCorner(getHeadUV(), getHeadSize()).x,
				helper.getBottomRightCorner(getHeadUV(), getHeadSize()).y,
				this.getHatUV(helper).x, this.getHatUV(helper).y,
				helper.getBottomRightCorner(getHatUV(helper), getHeadSize()).x,
				helper.getBottomRightCorner(getHatUV(helper), getHeadSize()).y,
				this.getTorsoUV(helper).x, this.getTorsoUV(helper).y,
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).x,
				helper.getBottomRightCorner(getTorsoUV(helper), getTorsoSize()).y,
				this.getJacketUV(helper).x, this.getJacketUV(helper).y,
				helper.getBottomRightCorner(getJacketUV(helper), getTorsoSize()).x,
				helper.getBottomRightCorner(getJacketUV(helper), getTorsoSize()).y,
				this.getRightLegUV(helper).x, this.getRightLegUV(helper).y,
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).x,
				helper.getBottomRightCorner(getRightLegUV(helper), getLegSize()).y,
				this.getRightPantsUV(helper).x, this.getRightPantsUV(helper).y,
				helper.getBottomRightCorner(getRightPantsUV(helper), getLegSize()).x,
				helper.getBottomRightCorner(getRightPantsUV(helper), getLegSize()).y,
				this.getRightArmUV(helper).x, this.getRightArmUV(helper).y,
				helper.getBottomRightCorner(getRightArmUV(helper), getArmSize()).x,
				helper.getBottomRightCorner(getRightArmUV(helper), getArmSize()).y,
				this.getRightSleeveUV(helper).x, this.getRightSleeveUV(helper).y,
				helper.getBottomRightCorner(getRightSleeveUV(helper), getArmSize()).x,
				helper.getBottomRightCorner(getRightSleeveUV(helper), getArmSize()).y
		);

		return CharacterFormModelDefinition.super.getModelData(helper);
	}
}

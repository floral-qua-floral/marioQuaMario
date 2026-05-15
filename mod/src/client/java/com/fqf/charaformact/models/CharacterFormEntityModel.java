package com.fqf.charaformact.models;

import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;

public abstract class CharacterFormEntityModel extends PlayerEntityModel<AbstractClientPlayerEntity> {
	public CharacterFormEntityModel(ModelPart root) {
		super(root, false);
	}

	/*
	* TODO:
	*  I want to have a single class that the developer extends to define a custom model.
	*  Every custom model needs to get fed into EntityModelLayerRegistry. This happens before models are normally created.
	*  It looks like it's happening before texture atlases are created? I don't know if that matters, but I don't really
	*  wanna do this kind of texture-related stuff before that, because that feels weird. I'm also hesitant to feed
	*  information into EntityModelLayerRegistry later than in onInitializeClient, although maybe that's fine...?
	*  I don't think I should be instantiating models without a context, even if I could theoretically make a separate
	*  constructor for doing exactly that.
	*
	* Maybe I should be giving up on the "just one class" idea and separating it into two:
	*  CharacterFormModelDefinition: an interface with methods for defining all the texture model data related stuff.
	*   It also needs to get a Supplier for creating the actual model.
	*  CharacterFormEntityModel: an abstract class that the developer can extend to add custom logic?
	*   Do I even need this to be exposed in the API, actually...? Maybe if the interface is sufficiently complex, that
	*   can work?
	*
	* Features that will likely be difficult to recreate:
	*  Hiding Mario's cap when he wears a helmet...
	*  Slimming Mario's torso when he wears a chestplate or leggings...
	*
	* */

	public static TexturedModelData getTexturedModelData() {
		Dilation dilation = Dilation.NONE;
		ModelData modelData = BipedEntityModel.getModelData(dilation, 0.0F);
		ModelPartData modelPartData = modelData.getRoot();
		modelPartData.addChild("ear", ModelPartBuilder.create().uv(24, 0).cuboid(-3.0F, -6.0F, -1.0F, 6.0F, 6.0F, 1.0F, dilation), ModelTransform.NONE);
		modelPartData.addChild(
				"cloak", ModelPartBuilder.create().uv(0, 0).cuboid(-5.0F, 0.0F, -1.0F, 10.0F, 16.0F, 1.0F, dilation, 1.0F, 0.5F), ModelTransform.pivot(0.0F, 0.0F, 0.0F)
		);

		modelPartData.addChild(
				EntityModelPartNames.LEFT_ARM,
				ModelPartBuilder.create().uv(32, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
				ModelTransform.pivot(5.0F, 2.0F, 0.0F)
		);
		modelPartData.addChild(
				"left_sleeve",
				ModelPartBuilder.create().uv(48, 48).cuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
				ModelTransform.pivot(5.0F, 2.0F, 0.0F)
		);
		modelPartData.addChild(
				"right_sleeve",
				ModelPartBuilder.create().uv(40, 32).cuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
				ModelTransform.pivot(-5.0F, 2.0F, 0.0F)
		);

		modelPartData.addChild(
				EntityModelPartNames.LEFT_LEG,
				ModelPartBuilder.create().uv(16, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation),
				ModelTransform.pivot(1.9F, 12.0F, 0.0F)
		);
		modelPartData.addChild(
				"left_pants",
				ModelPartBuilder.create().uv(0, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
				ModelTransform.pivot(1.9F, 12.0F, 0.0F)
		);
		modelPartData.addChild(
				"right_pants",
				ModelPartBuilder.create().uv(0, 32).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, dilation.add(0.25F)),
				ModelTransform.pivot(-1.9F, 12.0F, 0.0F)
		);
		modelPartData.addChild(
				EntityModelPartNames.JACKET, ModelPartBuilder.create().uv(16, 32).cuboid(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, dilation.add(0.25F)), ModelTransform.NONE
		);
		return TexturedModelData.of(modelData, 64, 64);
	}
}

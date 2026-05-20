package com.fqf.charaformact_api.appearance;

import net.minecraft.client.model.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.jetbrains.annotations.Nullable;

import java.util.NoSuchElementException;

public class AppearanceModel extends PlayerEntityModel<AbstractClientPlayerEntity> {
	public final @Nullable ModelPart tail;
	public final @Nullable ModelPart rightEar;
	public final @Nullable ModelPart leftEar;
	public final @Nullable ModelPart rightWing;
	public final @Nullable ModelPart leftWing;

	public AppearanceModel(ModelPart root) {
		super(root, false);

		this.tail = this.getOptionalModelPart(this.getTailParent(root), EntityModelPartNames.TAIL);

		ModelPart earsParent = this.getEarsParent(root);
		this.rightEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.RIGHT_EAR);
		this.leftEar = this.getOptionalModelPart(earsParent, EntityModelPartNames.LEFT_EAR);

		ModelPart wingsParent = this.getWingsParent(root);
		this.rightWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.RIGHT_WING);
		this.leftWing = this.getOptionalModelPart(wingsParent, EntityModelPartNames.LEFT_WING);

		this.head.xScale = 2;
	}

	protected ModelPart getTailParent(ModelPart root) {
		return this.body;
	}

	protected ModelPart getEarsParent(ModelPart root) {
		return this.head;
	}

	protected ModelPart getWingsParent(ModelPart root) {
		return this.body;
	}

	protected @Nullable ModelPart getOptionalModelPart(ModelPart root, String name) {
		try {
			return root.getChild(name);
		}
		catch(NoSuchElementException exception) {
			return null;
		}
	}
}

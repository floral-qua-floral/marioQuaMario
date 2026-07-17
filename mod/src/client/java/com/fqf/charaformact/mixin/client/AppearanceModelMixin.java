package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.util.RotatorFromRootContainer;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * This mixin exists to try and keep highly complex and entangled behavior out of AppearanceModel, since that's only
 * meant to be an API class. None of this behavior is intended to be tampered with anyways.
 */
@Mixin(value = AppearanceModel.class, remap = false)
public abstract class AppearanceModelMixin extends PlayerEntityModel<AbstractClientPlayerEntity> implements RotatorFromRootContainer {
	public AppearanceModelMixin(ModelPart root, boolean thinArms) {
		super(root, thinArms);
	}

	@Unique private Map<ModelPart, Consumer<MatrixStack>> partRotatorsFromRoot;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void storeImprovedPartsList(Identifier id, ModelPart root, CallbackInfo ci) {
		if(this.partRotatorsFromRoot == null) this.partRotatorsFromRoot = new HashMap<>();
		this.partRotatorsFromRoot.put(root, matrices -> {});

		CharaFormAct.LOGGER.info("Mapping out the RotatorFromRoot consumers for all ModelParts of {}...", this);
		root.traverse().forEach(modelPart -> {
			// Force every single part we know about to have a Rotator stored.
			this.getRotatorFor(root, modelPart);
		});
		CharaFormAct.LOGGER.info("Finished mapping out RotatorFromRoot consumers!");
	}

	@Unique private @NotNull Consumer<MatrixStack> getRotatorFor(ModelPart root, ModelPart part) {
		return this.partRotatorsFromRoot.computeIfAbsent(part, modelPart -> {
			// This code here will only ever run one single time per ModelPart, hopefully.
			final Consumer<MatrixStack> parentRotator = this.getRotatorFor(root, findParent(root, part));
			return matrices -> {
				parentRotator.accept(matrices);
				part.rotate(matrices);
			};
		});
	}

	@Unique private static @NotNull ModelPart findParent(ModelPart root, ModelPart part) {
		if(part == root) throw new IllegalArgumentException("Tried to get the parent of the root part!! >:(");
		return Objects.requireNonNull(findParentRecursive(root, part), "Could not find any parent for " + part + "???");
	}

	@Unique private static @Nullable ModelPart findParentRecursive(ModelPart searchUnder, ModelPart searchFor) {
		for(ModelPart directChild : ((ModelPartChildrenAccessor) (Object) searchUnder).getChildren().values()) {
			if(directChild == searchFor) return searchUnder;

			@Nullable ModelPart parentUnderMe = findParentRecursive(directChild, searchFor);
			if(parentUnderMe != null) return parentUnderMe;
		}

		return null;
	}

	@Override
	public void cfa$rotateFromRoot(ModelPart part, MatrixStack matrices) {
		this.partRotatorsFromRoot.get(part).accept(matrices);
	}
}

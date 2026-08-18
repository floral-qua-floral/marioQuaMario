package com.fqf.charaformact.compat;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.equipment.RenderedEquipmentInfo;
import com.fqf.charaformact.cfadata.equipment.UpdateEquipmentRenderingCallback;
import com.fqf.charaformact.cfadata.equipment.VisibleEquipmentSlot;
import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.mojang.datafixers.util.Pair;
import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;

import java.util.HashMap;

/**
 * Used for Model Covering and also for Feature Renderer Transformation.
 */
class AccessoriesCompatUnsafe {
	public static void register() {
		UpdateEquipmentRenderingCallback.EVENT.register(player -> {
			AccessoriesCapability capability = AccessoriesCapability.get(player);
			if(capability != null) {
				for(AccessoriesContainer container : capability.getContainers().values()) {
					for(Pair<Integer, ItemStack> accessory : container.getAccessories()) {
						ItemStack stack = accessory.getSecond();
						SlotReference reference = container.createReference(accessory.getFirst());

						ItemStack cosmetic = container.getCosmeticAccessories().getStack(reference.slot());

						if(!cosmetic.isEmpty() && Accessories.config().clientOptions.showCosmeticAccessories()) stack = cosmetic;

						player.cfa$getCfaData2().getModestyData().updateRenderedEquipmentInfo(
								reference,
								container.shouldRender(accessory.getFirst()) ? stack : ItemStack.EMPTY,
								AccessoriesCompatUnsafe::fromAccessorySlot
						);
					}
				}
			}
		});
	}

	private static RenderedEquipmentInfo fromAccessorySlot(ItemStack stack, SlotReference reference) {
		return new RenderedEquipmentInfo(stack, from(reference));
	}

	private static final HashMap<SlotReference, VisibleEquipmentSlot> SLOTS_MAP = new HashMap<>();
	private static VisibleEquipmentSlot from(SlotReference reference) {
		return SLOTS_MAP.computeIfAbsent(reference, slotReference -> {
			String slotName = slotReference.slotName();
			return switch(slotName) {
				case "accessories:head" -> VisibleEquipmentSlot.VANILLA_HELMET;
				case "hat" -> VisibleEquipmentSlot.HAT;
				case "face" -> VisibleEquipmentSlot.FACE;
				case "ears" -> VisibleEquipmentSlot.EARS_SLOT;

				case "accessories:chest" -> VisibleEquipmentSlot.VANILLA_CHESTPLATE;
				case "back", "cape", "elytra", "backtank", "backpack" -> VisibleEquipmentSlot.BACK_SLOT;
				case "belt", "toolbelt" -> VisibleEquipmentSlot.BELT;

				case "hand", "glove", "gloves", "gauntlet", "gauntlets" -> VisibleEquipmentSlot.GLOVES;

				case "accessories:legs" -> VisibleEquipmentSlot.VANILLA_LEGGINGS;
				case "accessories:feet", "shoes" -> VisibleEquipmentSlot.VANILLA_BOOTS;

				default -> {
					if(containsAny(slotName, "back", "cape", "elytr"))
						yield VisibleEquipmentSlot.BACK_SLOT;

					if(containsAny(slotName, "face", "mask", "goggle", "glasses", "lens", "probe"))
						yield VisibleEquipmentSlot.FACE;

					if(containsAny(slotName, "belt", "satchel"))
						yield VisibleEquipmentSlot.BELT;

					if(containsAny(slotName, "hand", "glove", "gauntlet"))
						yield VisibleEquipmentSlot.GLOVES;

					yield VisibleEquipmentSlot.UNRECOGNIZED;
				}
			};
		});
	}
	private static boolean containsAny(String check, String... substrings) {
		for(String substring : substrings) {
			if(check.contains(substring)) return true;
		}
		return false;
	}

	public static <T> T wrap(T original) {
		if(original instanceof AccessoryRenderer originalRenderer) {
			AccessoryRenderer wrappedRenderer;
			if(originalRenderer.isEmpty()) wrappedRenderer = originalRenderer;
			else wrappedRenderer = new TransformingAccessoryRenderer(originalRenderer);
			//noinspection unchecked
			return (T) wrappedRenderer;
		}
		throw new IllegalArgumentException("Unable to wrap an AccessoryRenderer around " + original + "!");
	}

	private static class TransformingAccessoryRenderer implements AccessoryRenderer {
		private final AccessoryRenderer ORIGINAL;
		private boolean hasLogged = false;

		public TransformingAccessoryRenderer(AccessoryRenderer original) {
			this.ORIGINAL = original;
		}

		@Override
		public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
			if(ModelPartMover.instance != null) {
				EquipmentFeatureCategory category = from(reference).TRANSFORMATION_CATEGORY;
				ModelPartMover.instance.setTo(category);
				if(!this.hasLogged) {
					this.hasLogged = true;
					CharaFormAct.LOGGER.info("{} is rendering from standard slot {}...!", this, from(reference));
				}
			}
			this.ORIGINAL.render(stack, reference, matrices, model, multiBufferSource, light, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch);
		}

		@Override
		public <M extends LivingEntity> void renderOnFirstPerson(Arm arm, ItemStack stack, SlotReference reference, MatrixStack matrices, EntityModel<M> model, VertexConsumerProvider multiBufferSource, int light) {
			ClientPlayerEntity player = MinecraftClient.getInstance().player;
			if(player == null) return;

			boolean needsNewModelPartMover = ModelPartMover.instance == null;
			if(needsNewModelPartMover) {
				ModelPartMover.instance = new ModelPartMover(player.cfa$getAppearanceData().getAppearance());
			}
			ModelPartMover.instance.setTo(from(reference).TRANSFORMATION_CATEGORY);
			this.ORIGINAL.renderOnFirstPerson(arm, stack, reference, matrices, model, multiBufferSource, light);
			if(needsNewModelPartMover) ModelPartMover.instance = null;
		}

		@Override
		public boolean shouldRender(boolean isRendering) {
			return this.ORIGINAL.shouldRender(isRendering);
		}

		@Override
		public boolean shouldRenderInFirstPerson(Arm arm, ItemStack stack, SlotReference reference) {
			return this.ORIGINAL.shouldRenderInFirstPerson(arm, stack, reference);
		}

		@Override
		public String toString() {
			return "cfaWrapped[" + this.ORIGINAL + "]";
		}

		@Override
		public int hashCode() {
			return this.ORIGINAL.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return this.ORIGINAL.equals(obj instanceof TransformingAccessoryRenderer other ? other.ORIGINAL : obj);
		}
	}
}

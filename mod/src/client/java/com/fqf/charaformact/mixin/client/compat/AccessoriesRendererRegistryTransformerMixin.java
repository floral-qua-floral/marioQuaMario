package com.fqf.charaformact.mixin.client.compat;

import com.fqf.charaformact.compat.EquipmentSlotModsCompatSafe;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.function.Supplier;

@Pseudo
@Mixin(AccessoriesRendererRegistry.class)
public class AccessoriesRendererRegistryTransformerMixin {
	// The plan is to intercept Accessory Renderers as they're registered, and inject Feature Transformer behavior there,
	// rather than doing a mixin on the code where Accessories actually calls the renderer's render methods. This is
	// because, as part of the Accessories API, this should hopefully be a stable place to mixin, whereas render calls
	// in the implementation may be more likely to change?
	// Or maybe I'm just a crazy person!
	@WrapMethod(method = "registerRenderer(Lnet/minecraft/util/Identifier;Ljava/util/function/Supplier;)V")
	private static void registerTransformingRenderer(Identifier location, Supplier<AccessoryRenderer> renderer, Operation<Void> original) {
		original.call(location, (Supplier<AccessoryRenderer>) () -> EquipmentSlotModsCompatSafe.getWrappedAccessoryRenderer(renderer.get()));
	}
}

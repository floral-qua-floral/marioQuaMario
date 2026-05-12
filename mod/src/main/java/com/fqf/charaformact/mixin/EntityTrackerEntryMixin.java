package com.fqf.charaformact.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class EntityTrackerEntryMixin {
	@ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
	private Consumer<? super Entity> doNotTeleportIfRepositioningIsSuppressed(Consumer<? super Entity> action) {
		// THIS IS GROSS!!!!!!!!!!!!!!!
		return passenger -> {
			// Skip calling the original consumer if the entity being munched is a ServerPlayerEntity who is skipping dismount teleports.
			if(!(passenger instanceof ServerPlayerEntity serverPlayerPassenger) || !serverPlayerPassenger.cfa$getCfaData().isSkippingDismountRepositioning()) {
				action.accept(passenger);
			}
		};
	}

//	@WrapOperation(method="tick", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
//	private void doNotTeleportIfRepositioningIsSuppressedMyGodThisIsAnnoying(Stream<Entity> instance, Consumer<? super Entity> consumer, Operation<Void> original) {
//		Consumer<? super Entity> wrappedConsumer = passenger -> {
//
//		};
//		original.call(instance, wrappedConsumer);
//	}
}

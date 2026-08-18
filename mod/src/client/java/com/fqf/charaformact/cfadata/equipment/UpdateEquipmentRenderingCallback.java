package com.fqf.charaformact.cfadata.equipment;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.network.AbstractClientPlayerEntity;

@FunctionalInterface
public interface UpdateEquipmentRenderingCallback {
	Event<UpdateEquipmentRenderingCallback> EVENT =
			EventFactory.createArrayBacked(UpdateEquipmentRenderingCallback.class, callbacks -> player -> {
				for(UpdateEquipmentRenderingCallback callback : callbacks) {
					callback.onUpdateEquipment(player);
				}
			});

	void onUpdateEquipment(AbstractClientPlayerEntity player);
}

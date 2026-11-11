package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.Nullable;

public interface IceEncasingEvents {
	Event<PostEncasementCallback> POST_ENCASEMENT = EventFactory.createArrayBacked(PostEncasementCallback.class, callbacks -> (entity, encasement) -> {
		for(PostEncasementCallback callback : callbacks)
			callback.postEncasement(entity, encasement);
	});

	Event<PostThawCallback> POST_THAW = EventFactory.createArrayBacked(PostThawCallback.class, callbacks -> (entity, encasement, isShattering) -> {
		for(PostThawCallback callback : callbacks)
			callback.postThaw(entity, encasement, isShattering);
	});

	Event<PostShatterCallback> POST_SHATTER = EventFactory.createArrayBacked(PostShatterCallback.class, callbacks -> (entity, encasement, originalDamageSource, originalDamageAmount) -> {
		for(PostShatterCallback callback : callbacks)
			callback.postShatter(entity, encasement, originalDamageSource, originalDamageAmount);
	});

	Event<ThawConversionCallback> THAW_CONVERSION = EventFactory.createArrayBacked(ThawConversionCallback.class, callbacks -> (original, encasement) -> {
		for(ThawConversionCallback callback : callbacks) {
			Entity newEntity = callback.getConversion(original, encasement);
			if(newEntity != null && !newEntity.equals(original))
				return newEntity;
		}
		return original;
	});

	@FunctionalInterface
	interface PostEncasementCallback {
		void postEncasement(Entity entity, IceEncasementEntity encasement);
	}

	@FunctionalInterface
	interface PostThawCallback {
		void postThaw(@Nullable Entity entity, IceEncasementEntity encasement, boolean isShattering);
	}

	@FunctionalInterface
	interface PostShatterCallback {
		void postShatter(Entity entity, IceEncasementEntity encasement, DamageSource originalDamageSource, float originalDamageAmount);
	}

	@FunctionalInterface
	interface ThawConversionCallback {
		@Nullable Entity getConversion(Entity original, IceEncasementEntity encasement);
	}
}

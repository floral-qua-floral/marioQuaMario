package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.customization.CustomizablePlayerEntity;
import com.fqf.mario_qua_mario.customization.DefaultSkinTone;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Colors;
import net.minecraft.util.DyeColor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;
import java.util.UUID;

import static com.fqf.mario_qua_mario.customization.CharacterCustomizationUtil.*;

@Mixin(PlayerEntity.class)
public abstract class CustomizableToadDataTrackingMixin extends LivingEntity implements CustomizablePlayerEntity {
	protected CustomizableToadDataTrackingMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract GameProfile getGameProfile();

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void addCustomToadDataTracking(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(ALWAYS_USE_SKIN_COLOR, false);
		builder.add(SKIN_COLOR, 0xFF000000);
		builder.add(CAP_COLOR, 0xFF000000);
		builder.add(SPOTS_COLOR, 0xFF000000);
		builder.add(VEST_COLOR, 0xFF000000);
		builder.add(SHIRT_COLOR, OptionalInt.empty());
		builder.add(HAS_PIGTAILS, false);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void readCustomToadData(NbtCompound nbt, CallbackInfo ci) {
		if(nbt.contains(PERSISTENT_DATA_KEY, NbtElement.COMPOUND_TYPE)) {
			NbtCompound persistentData = nbt.getCompound(PERSISTENT_DATA_KEY);

			this.mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, persistentData.getBoolean(ALWAYS_USE_SKIN_COLOR_KEY));
			this.mqm$updateCustomizationData(SKIN_COLOR, persistentData.getInt(SKIN_COLOR_KEY));
			this.mqm$updateCustomizationData(CAP_COLOR, persistentData.getInt(CAP_COLOR_KEY));
			this.mqm$updateCustomizationData(SPOTS_COLOR, persistentData.getInt(SPOTS_COLOR_KEY));
			this.mqm$updateCustomizationData(VEST_COLOR, persistentData.getInt(VEST_COLOR_KEY));
			this.mqm$updateCustomizationData(SHIRT_COLOR, persistentData.getBoolean(HAS_SHIRT_KEY)
					? OptionalInt.of(persistentData.getInt(SHIRT_COLOR_KEY))
					: OptionalInt.empty());
			this.mqm$updateCustomizationData(HAS_PIGTAILS, persistentData.getBoolean(HAS_PIGTAILS_KEY));
		}
		else {
			// Decide defaults based on the player's UUID! Like how Minecraft assigns a default skin based on UUID too.
			this.mqm$resetCustomizationData(this.getGameProfile().getId());
		}
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeCustomToadData(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound persistentData = new NbtCompound();
		persistentData.putBoolean(ALWAYS_USE_SKIN_COLOR_KEY, this.mqm$getCustomizationData(ALWAYS_USE_SKIN_COLOR));
		persistentData.putInt(SKIN_COLOR_KEY, this.mqm$getCustomizationData(SKIN_COLOR));
		persistentData.putInt(CAP_COLOR_KEY, this.mqm$getCustomizationData(CAP_COLOR));
		persistentData.putInt(SPOTS_COLOR_KEY, this.mqm$getCustomizationData(SPOTS_COLOR));
		persistentData.putInt(VEST_COLOR_KEY, this.mqm$getCustomizationData(VEST_COLOR));
		OptionalInt shirtColor = this.mqm$getCustomizationData(SHIRT_COLOR);
		persistentData.putBoolean(HAS_SHIRT_KEY, shirtColor.isPresent());
		if(shirtColor.isPresent()) persistentData.putInt(SHIRT_COLOR_KEY, shirtColor.orElseThrow());
		persistentData.putBoolean(HAS_PIGTAILS_KEY, this.mqm$getCustomizationData(HAS_PIGTAILS));

		nbt.put(PERSISTENT_DATA_KEY, persistentData);
	}

	@Unique
	private static <T> T getArrayElement(T[] array, int numberoo) {
		return array[Math.floorMod(numberoo, array.length)];
	}

	@Override
	public <T> void mqm$updateCustomizationData(TrackedData<T> trackedData, T newValue) {
		this.dataTracker.set(trackedData, newValue);
	}

	@Override
	public <T> T mqm$getCustomizationData(TrackedData<T> trackedData) {
		return this.dataTracker.get(trackedData);
	}

	@Override
	public void mqm$resetSkinToneOnly(UUID uuid) {
		this.mqm$updateCustomizationData(SKIN_COLOR, getArrayElement(DefaultSkinTone.AVAILABLE_RANDOMLY, uuid.hashCode()).ARGB);
	}

	@Override
	public void mqm$resetCustomizationData(UUID uuid) {
		this.mqm$updateCustomizationData(ALWAYS_USE_SKIN_COLOR, false);

		this.mqm$resetSkinToneOnly(uuid);

		this.mqm$updateCustomizationData(CAP_COLOR, Colors.WHITE);

		int spotsColor = getArrayElement(DyeColor.values(), uuid.hashCode()).getEntityColor();
		this.mqm$updateCustomizationData(SPOTS_COLOR, spotsColor);

		int vestColor;
		OptionalInt shirtColor;
		if(uuid.getMostSignificantBits() % 2 == 0) {
			// Half of all Toads will have a shirt matching their spot color, and a random colored vest.
			int alternateHash = Long.hashCode((uuid.getLeastSignificantBits() + 1) ^ uuid.getMostSignificantBits());
			vestColor = getArrayElement(DyeColor.values(), alternateHash).getEntityColor();
			shirtColor = OptionalInt.of(spotsColor);
		}
		else {
			// The other half will have no shirt, and a vest matching their cap color.
			vestColor = spotsColor;
			shirtColor = OptionalInt.empty();
		}

		this.mqm$updateCustomizationData(VEST_COLOR, vestColor);
		this.mqm$updateCustomizationData(SHIRT_COLOR, shirtColor);

		this.mqm$updateCustomizationData(HAS_PIGTAILS, uuid.getLeastSignificantBits() % 3 == 0);
	}
}

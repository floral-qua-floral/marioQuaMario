package com.fqf.mario_qua_mario.mixin;

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

import static com.fqf.mario_qua_mario.util.CustomToadUtil.*;

@Mixin(PlayerEntity.class)
public abstract class CustomizableToadDataTrackingMixin extends LivingEntity implements CustomToadEntity {
	protected CustomizableToadDataTrackingMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public abstract GameProfile getGameProfile();

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void addCustomToadDataTracking(DataTracker.Builder builder, CallbackInfo ci) {
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

			this.mqm$updateToadData(SKIN_COLOR, persistentData.getInt(SKIN_COLOR_KEY));
			this.mqm$updateToadData(CAP_COLOR, persistentData.getInt(CAP_COLOR_KEY));
			this.mqm$updateToadData(SPOTS_COLOR, persistentData.getInt(SPOTS_COLOR_KEY));
			this.mqm$updateToadData(VEST_COLOR, persistentData.getInt(VEST_COLOR_KEY));
			this.mqm$updateToadData(SHIRT_COLOR, persistentData.getBoolean(HAS_SHIRT_KEY)
					? OptionalInt.of(persistentData.getInt(SHIRT_COLOR_KEY))
					: OptionalInt.empty());
			this.mqm$updateToadData(HAS_PIGTAILS, persistentData.getBoolean(HAS_PIGTAILS_KEY));
		}
		else {
			// Decide defaults based on the player's UUID! Like how Minecraft assigns a default skin based on UUID too.
			this.mqm$resetToadData(this.getGameProfile().getId());
		}
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeCustomToadData(NbtCompound nbt, CallbackInfo ci) {
		NbtCompound persistentData = new NbtCompound();
		persistentData.putInt(SKIN_COLOR_KEY, this.mqm$getToadData(SKIN_COLOR));
		persistentData.putInt(CAP_COLOR_KEY, this.mqm$getToadData(CAP_COLOR));
		persistentData.putInt(SPOTS_COLOR_KEY, this.mqm$getToadData(SPOTS_COLOR));
		persistentData.putInt(VEST_COLOR_KEY, this.mqm$getToadData(VEST_COLOR));
		OptionalInt shirtColor = this.mqm$getToadData(SHIRT_COLOR);
		persistentData.putBoolean(HAS_SHIRT_KEY, shirtColor.isPresent());
		if(shirtColor.isPresent()) persistentData.putInt(SHIRT_COLOR_KEY, shirtColor.orElseThrow());
		persistentData.putBoolean(HAS_PIGTAILS_KEY, this.mqm$getToadData(HAS_PIGTAILS));

		nbt.put(PERSISTENT_DATA_KEY, persistentData);
	}

	@Unique
	private static <T> T getArrayElement(T[] array, int numberoo) {
		return array[Math.floorMod(numberoo, array.length)];
	}

	@Override
	public <T> void mqm$updateToadData(TrackedData<T> trackedData, T newValue) {
		this.dataTracker.set(trackedData, newValue);
	}

	@Override
	public <T> T mqm$getToadData(TrackedData<T> trackedData) {
		return this.dataTracker.get(trackedData);
	}

	@Override
	public void mqm$resetToadData(UUID uuid) {
		int uuidHash = uuid.hashCode();

		// I would ideally want to programmatically create a randomized skin tone, but that seems really hard. Instead,
		// I'm selecting randomly from a hardcoded list of pre-selected individual tones. This is less fun because it
		// means there aren't a trajillion different possible randomly assigned skin tones, but it's what's within my
		// abilities.
		// These skin tones are sourced from Minecraft's default skins, since they seem to have a pretty solid racial
		// diversity. I was considering including Toadette and Mario's skin tones in there too, but I don't want to make
		// lighter skin too common relative to darker skin, which 2 extra light tones might with a pool this small. :(
		this.mqm$updateToadData(SKIN_COLOR, getArrayElement(new Integer[]{
//				0xFFEDC19F, // Toadette
//				0xFFFFDB99, // Mario
				0xFFEFDABF, // Alex
				0xFFF9A786, // Ari
				0xFFAB724C, // Efe
				0xFFDF9658, // Kai
				0xFF443528, // Makena
				0xFFB9674A, // Noor
				0xFFB3795E, // Steve
				0xFFF29F5F, // Sunny
				0xFF7E5337, // Zuri
		}, uuidHash));
		this.mqm$updateToadData(CAP_COLOR, Colors.WHITE);

		int spotsColor = getArrayElement(DyeColor.values(), uuidHash).getEntityColor();
		this.mqm$updateToadData(SPOTS_COLOR, spotsColor);

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

		this.mqm$updateToadData(VEST_COLOR, vestColor);
		this.mqm$updateToadData(SHIRT_COLOR, shirtColor);

		this.mqm$updateToadData(HAS_PIGTAILS, uuid.getLeastSignificantBits() % 3 == 0);
	}
}

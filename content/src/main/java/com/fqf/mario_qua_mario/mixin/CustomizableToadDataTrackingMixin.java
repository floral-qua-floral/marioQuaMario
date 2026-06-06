package com.fqf.mario_qua_mario.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
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
		UUID uuid = UUID.randomUUID();
		int uuidHash = uuid.hashCode();
		int spotsColor = getArrayElement(DyeColor.values(), uuidHash).getEntityColor();
		builder.add(SKIN_COLOR, getSkinColorFromUUID(uuidHash));
		builder.add(CAP_COLOR, Colors.WHITE);
		builder.add(SPOTS_COLOR, spotsColor);

		int vestColor;
		OptionalInt shirtColor;
		if(uuid.getMostSignificantBits() % 2 == 0) {
			// Half of all Toads will have a shirt matching their spot color, and a random colored vest.
			shirtColor = OptionalInt.of(spotsColor);
			int alternateHash = Long.hashCode((uuid.getLeastSignificantBits() + 1) ^ uuid.getMostSignificantBits());
			vestColor = getArrayElement(DyeColor.values(), alternateHash).getEntityColor();
		}
		else {
			// The other half will have no shirt, and a vest matching their cap color.
			shirtColor = OptionalInt.empty();
			vestColor = spotsColor;
		}
		builder.add(VEST_COLOR, vestColor);
		builder.add(SHIRT_COLOR, shirtColor);

		// Pigtails are a relatively rare feature for Toads. Using a multiple of 2 for the division here makes it so that
		// all Toads who have pigtails are also going to wear a shirt. Do I want this??
		builder.add(HAS_PIGTAILS, uuidHash % 3 == 0);
	}

	@Inject(method = "writeCustomDataToNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void writeCustomToadData(NbtCompound nbt, CallbackInfo ci) {

	}

	@Unique
	private static int getSkinColorFromUUID(int numberoo) {
		// I would ideally want to programmatically create a randomized skin tone, but that seems really hard. Instead,
		// I'm selecting randomly from a hardcoded list of pre-selected individual tones. This is less fun because it
		// means there aren't a trajillion different possible randomly assigned skin tones, but it's what's within my
		// abilities.
		// These skin tones are sourced from Minecraft's default skins, since they seem to have a pretty solid racial
		// diversity. I was considering including Toadette and Mario's skin tones in there too, but I don't want to
		// make lighter skin too common relative to darker skin.
		Integer[] skinColors = {
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
		};
		return getArrayElement(skinColors, numberoo);
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
}

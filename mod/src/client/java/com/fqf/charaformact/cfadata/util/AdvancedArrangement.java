package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import net.minecraft.client.model.ModelPart;
import org.jetbrains.annotations.Nullable;

public class AdvancedArrangement extends Arrangement {
	public static final int BUSY_ARMS_SLOT = 0;
	public static final int MIRRORING_SLOT = 1;
	public static final int HELPER_SLOT = 2;

	private final Arrangement[] STORAGE;

	public AdvancedArrangement() {
		this.STORAGE = new Arrangement[3];
	}

	public static @Nullable AdvancedArrangement of(ModelPart part) {
		if(part == null) return null;
		AdvancedArrangement build = new AdvancedArrangement();
		build.setPos(part.pivotX, part.pivotY, part.pivotZ);
		build.setAngles(part.pitch, part.yaw, part.roll);
		return build;
	}

	public void store(int slot) {
		Arrangement storeTo = new Arrangement();
		storeTo.setPos(this.x, this.y, this.z);
		storeTo.setAngles(this.pitch, this.yaw, this.roll);
		this.STORAGE[slot] = storeTo;
	}

	public void resetTo(int slot) {
		Arrangement loadFrom = this.STORAGE[slot];
		this.setPos(loadFrom.x, loadFrom.y, loadFrom.z);
		this.setAngles(loadFrom.pitch, loadFrom.yaw, loadFrom.roll);
	}

	public Arrangement getDeltas(int slot) {
		Arrangement changesFrom = this.STORAGE[slot];
		Arrangement deltas = new Arrangement();
		deltas.setPos(this.x - changesFrom.x, this.y - changesFrom.y, this.z - changesFrom.z);
		deltas.setAngles(this.pitch - changesFrom.pitch, this.yaw - changesFrom.yaw, this.roll - changesFrom.roll);
		return deltas;
	}

	public void mirrorChanges(int slot) {
		Arrangement mirrorAcross = this.STORAGE[slot];
		float deltaX = this.x - mirrorAcross.x;
		float deltaY = this.y - mirrorAcross.y;
		float deltaZ = this.z - mirrorAcross.z;
		float deltaPitch = this.pitch - mirrorAcross.pitch;
		float deltaYaw = this.yaw - mirrorAcross.yaw;
		float deltaRoll = this.roll - mirrorAcross.roll;
		this.setPos(mirrorAcross.x - deltaX, mirrorAcross.y - deltaY, mirrorAcross.z - deltaZ);
		this.setAngles(mirrorAcross.pitch - deltaPitch, mirrorAcross.yaw - deltaYaw, mirrorAcross.roll - deltaRoll);
	}
}

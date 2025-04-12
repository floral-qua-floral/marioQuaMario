package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario_api.definitions.states.PowerUpDefinition;
import net.minecraft.util.Identifier;

public class PowerHeartHelperImpl implements PowerUpDefinition.PowerHeartHelper {
	private final Identifier ID;
	public PowerHeartHelperImpl(Identifier powerUpID) {
		this.ID = powerUpID;
	}

	@Override
	public PowerUpDefinition.PowerHeart auto() {
		return this.standard(this.ID.getNamespace(), this.ID.getPath());
	}

	@Override
	public PowerUpDefinition.PowerHeart standard(String namespace, String folder) {
		return this.fromRoot(Identifier.of(namespace, "hud/power_hearts/" + folder));
	}

	@Override
	public PowerUpDefinition.PowerHeart fromRoot(Identifier root) {
		return new PowerUpDefinition.PowerHeart(
				Identifier.of(root.getNamespace(), root.getPath() + "/full"),
				Identifier.of(root.getNamespace(), root.getPath() + "/full_blinking"),

				Identifier.of(root.getNamespace(), root.getPath() + "/half"),
				Identifier.of(root.getNamespace(), root.getPath() + "/half_blinking"),

				Identifier.of(root.getNamespace(), root.getPath() + "/hardcore/full"),
				Identifier.of(root.getNamespace(), root.getPath() + "/hardcore/full_blinking"),

				Identifier.of(root.getNamespace(), root.getPath() + "/hardcore/half"),
				Identifier.of(root.getNamespace(), root.getPath() + "/hardcore/half_blinking"),

				Identifier.ofVanilla("hud/heart/container"),
				Identifier.ofVanilla("hud/heart/container_blinking")
		);
	}
}

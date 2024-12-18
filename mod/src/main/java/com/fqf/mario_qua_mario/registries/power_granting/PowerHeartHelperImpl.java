package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.definitions.states.PowerUpDefinition;
import net.minecraft.util.Identifier;

public class PowerHeartHelperImpl implements PowerUpDefinition.PowerHeartHelper {
	private final Identifier ID;
	public PowerHeartHelperImpl(Identifier powerUpID) {
		this.ID = powerUpID;
	}

	@Override
	public PowerUpDefinition.PowerHeart auto() {
		return this.standard(this.ID.getNamespace(), "hud/power_hearts/" + this.ID.getPath());
	}

	@Override
	public PowerUpDefinition.PowerHeart standard(String namespace, String folder) {
		return new PowerUpDefinition.PowerHeart(
				Identifier.of(namespace, folder + "/full"),
				Identifier.of(namespace, folder + "/full_blinking"),

				Identifier.of(namespace, folder + "/half"),
				Identifier.of(namespace, folder + "/half_blinking"),

				Identifier.of(namespace, folder + "/hardcore/full"),
				Identifier.of(namespace, folder + "/hardcore/full_blinking"),

				Identifier.of(namespace, folder + "/hardcore/half"),
				Identifier.of(namespace, folder + "/hardcore/half_blinking"),

				Identifier.ofVanilla("hud/heart/container"),
				Identifier.ofVanilla("hud/heart/container_blinking")
		);
	}

	@Override
	public PowerUpDefinition.PowerHeart fromRoot(Identifier root) {
		return this.standard(root.getNamespace(), root.getPath());
	}
}

package com.fqf.charapoweract.registries.power_granting;

import com.fqf.charapoweract_api.definitions.states.PowerFormDefinition;
import net.minecraft.util.Identifier;

public class PowerHeartHelperImpl implements PowerFormDefinition.PowerHeartHelper {
	private final Identifier ID;
	public PowerHeartHelperImpl(Identifier powerUpID) {
		this.ID = powerUpID;
	}

	@Override
	public PowerFormDefinition.PowerHeart auto() {
		return this.standard(this.ID.getNamespace(), this.ID.getPath());
	}

	@Override
	public PowerFormDefinition.PowerHeart standard(String namespace, String folder) {
		return this.fromRoot(Identifier.of(namespace, "hud/power_hearts/" + folder));
	}

	@Override
	public PowerFormDefinition.PowerHeart fromRoot(Identifier root) {
		return new PowerFormDefinition.PowerHeart(
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

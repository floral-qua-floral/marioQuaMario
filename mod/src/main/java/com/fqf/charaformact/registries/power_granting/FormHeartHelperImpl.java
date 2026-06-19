package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact_api.definitions.states.FormDefinition;
import net.minecraft.util.Identifier;

public class FormHeartHelperImpl implements FormDefinition.FormHeartHelper {
	private final Identifier ID;
	public FormHeartHelperImpl(Identifier powerUpID) {
		this.ID = powerUpID;
	}

	private static final FormDefinition.FormHeart VANILLA = new FormDefinition.FormHeart(
			Identifier.ofVanilla("hud/heart/full"),
			Identifier.ofVanilla("hud/heart/full_blinking"),
			Identifier.ofVanilla("hud/heart/half"),
			Identifier.ofVanilla("hud/heart/half_blinking"),
			Identifier.ofVanilla("hud/heart/hardcore_full"),
			Identifier.ofVanilla("hud/heart/hardcore_full_blinking"),
			Identifier.ofVanilla("hud/heart/hardcore_half"),
			Identifier.ofVanilla("hud/heart/hardcore_half_blinking"),
			Identifier.ofVanilla("hud/heart/container"),
			Identifier.ofVanilla("hud/heart/container_blinking")
	);

	@Override
	public FormDefinition.FormHeart vanilla() {
		return VANILLA;
	}

	@Override
	public FormDefinition.FormHeart auto() {
		return this.standard(this.ID.getNamespace(), this.ID.getPath());
	}

	@Override
	public FormDefinition.FormHeart standard(String namespace, String folder) {
		return this.fromRoot(Identifier.of(namespace, "hud/form_hearts/" + folder));
	}

	@Override
	public FormDefinition.FormHeart fromRoot(Identifier root) {
		return new FormDefinition.FormHeart(
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

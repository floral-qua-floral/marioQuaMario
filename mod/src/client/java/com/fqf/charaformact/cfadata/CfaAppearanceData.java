package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.AppearanceRenderer;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class CfaAppearanceData<CfaDataType extends CfaPlayerData & CfaClientDataImpl> {
	private final AbstractClientPlayerEntity PLAYER;
	private final CfaDataType DATA;

	private @Nullable ParsedClientAppearance appearance, flickerModel;
	private @Nullable PlayerEntityRenderer renderer, flickerRenderer;

	private long flickerUntil;
	private boolean flickering;

	public CfaAppearanceData(CfaDataType data) {
		PLAYER = data.getPlayer();
		DATA = data;

		this.flickerUntil = Long.MIN_VALUE;
	}

	public void tick() {
		long time = this.PLAYER.getWorld().getTime();
		if(this.flickerUntil > time) {
			long difference = this.flickerUntil - time;
			this.flickering = MathHelper.floor(difference / 3F) % 2 == 0;
		}
		else this.flickering = false;
	}

	public boolean hasAppearance() {
		return this.getAppearance() != null;
	}

	public ParsedClientAppearance getAppearance() {
		return this.getAppearance(true);
	}

	public ParsedClientAppearance getAppearance(boolean allowFlicker) {
		if(allowFlicker && this.flickering) return this.flickerModel;
		return this.appearance;
	}

	public PlayerEntityRenderer getRenderer() {
		if(this.flickering) return this.flickerRenderer;
		return this.renderer;
	}

	public void conditionallyFlicker() {
		this.flickerUntil = this.PLAYER.getWorld().getTime() + 9L;
	}

	public void update() {
		this.flickerModel = this.appearance;
		this.flickerRenderer = this.renderer;

		@Nullable Pair<ParsedClientAppearance, AppearanceRenderer> newModelAndRenderer = null;

		if(this.DATA.isEnabled()) {
			ParsedCharacter character = this.DATA.getCharacter();
			ParsedForm form = this.DATA.getForm();
			newModelAndRenderer = ClientAppearanceCollector.INSTANCE.get(
					new CharacterFormCombo(character, form));
			if(newModelAndRenderer == null) {
				CharaFormAct.LOGGER.warn("Player {} could not find a playermodel for {} in form {}!",
						this.PLAYER, character, form);
				newModelAndRenderer = ClientAppearanceCollector.INSTANCE.get(
						new CharacterFormCombo(character, character.INITIAL_FORM));
			}
		}

		if(newModelAndRenderer == null) newModelAndRenderer = new Pair<>(null, null);
		this.appearance = newModelAndRenderer.getLeft();
		this.renderer = newModelAndRenderer.getRight();

		this.conditionallyFlicker();
	}
}

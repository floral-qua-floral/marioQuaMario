package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.models.CharacterFormRenderer;
import com.fqf.charaformact.models.ParsedCharacterFormModel;
import com.fqf.charaformact.models.PlayerModelCollector;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class CfaModelData<CfaDataType extends CfaPlayerData & CfaClientDataImpl> {
	private final AbstractClientPlayerEntity PLAYER;
	private final CfaDataType DATA;

	private @Nullable ParsedCharacterFormModel model, flickerModel;
	private @Nullable CharacterFormRenderer renderer, flickerRenderer;

	private long flickerUntil;
	private boolean flickering;

	public CfaModelData(CfaDataType data) {
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

	public boolean hasCustomModel() {
		return this.getModel() != null;
	}

	public ParsedCharacterFormModel getModel() {
		if(this.flickering) return this.flickerModel;
		return this.model;
	}

	public CharacterFormRenderer getRenderer() {
		if(this.flickering) return this.flickerRenderer;
		return this.renderer;
	}

	public void conditionallyFlicker() {
		this.flickerUntil = this.PLAYER.getWorld().getTime() + 9L;
	}

	public void updateCharacterFormCombo() {
		this.flickerModel = this.model;
		this.flickerRenderer = this.renderer;

		@Nullable Pair<ParsedCharacterFormModel, CharacterFormRenderer> newModelAndRenderer = null;

		if(this.DATA.isEnabled()) {
			ParsedCharacter character = this.DATA.getCharacter();
			ParsedForm form = this.DATA.getForm();
			newModelAndRenderer = PlayerModelCollector.getModelAndRenderer(
					new CharacterFormCombo(character, form));
			if(newModelAndRenderer == null) {
				CharaFormAct.LOGGER.warn("Player {} could not find a playermodel for {} in form {}!",
						this.PLAYER, character, form);
				newModelAndRenderer = PlayerModelCollector.getModelAndRenderer(
						new CharacterFormCombo(character, character.INITIAL_FORM));
			}
		}

		if(newModelAndRenderer == null) newModelAndRenderer = new Pair<>(null, null);
		this.model = newModelAndRenderer.getLeft();
		this.renderer = newModelAndRenderer.getRight();

		this.conditionallyFlicker();
	}
}

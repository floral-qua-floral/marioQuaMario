package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.appearance.FeatureRendererWithMutableRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

/**
 * I don't really like any of what I'm doing here. But it's necessary in order to have mod-added feature renderers be
 * added to additional player renderers which are not associated with either an Entity Type or either of the vanilla
 * player models. Fabric API and NeoForge both seem to be built under the assumption that every single entity renderer
 * will be associated with one of these two things, and my system can't reasonably put its renderers into either of
 * those maps.
 * <p>
 * The best solution I could figure out was to make it so that every time a non-vanilla feature gets added to vanilla's
 * wide-armed player renderer, I manually also add that feature to every single one of my custom renderers. But I can't
 * realistically construct a new instance of each FeatureRenderer that's passed in, because addFeature only receives the
 * completed, initialized FeatureRenderer. I don't have access to its constructor, or whatever information might have
 * been put into said constructor! So I have to actually reuse the individual FeatureRenderer that was meant for the
 * vanilla wide-arm renderer, for like a billion of my own custom renderers. And that's bad because FeatureRenderers are
 * designed to be initialized fresh for a single renderer.
 * <p>
 * But lucky for me, it turns out that the only thing really enforcing that expectation is that single immutable context
 * field! And that's private, so it's only ever used in this one singular vanilla class. And so I can be absolutely sure
 * that it is literally only ever referenced in two specific spots, ever, in all of Minecraft. So if I just make those
 * two spots look somewhere else for the feature renderer context, I can basically force a single FeatureRenderer to
 * work a ton of overtime and serve as many entity renderers as I want.
 * <p>
 * I don't even want to think about how badly this might break if another mod tries to do anything remotely similar. But
 * the fact that all of this is THIS outrageously inconvenient makes me feel at least a little assured that other mods
 * are unlikely to act as insane as me and make the context field mutable.
 */
@Mixin(FeatureRenderer.class)
public class FeatureRendererMixin<T extends Entity, M extends EntityModel<T>> implements FeatureRendererWithMutableRenderer<T, M> {
	@Shadow private FeatureRendererContext<T, M> context;

	@Override
	public void cfa$replaceMutableContext(FeatureRendererContext<T, M> newContext) {
		this.context = newContext;
	}
}

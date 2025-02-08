package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.tom.cpl.math.Vec3f;
import com.tom.cpm.shared.MinecraftClientAccess;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.io.ModelFile;
import com.tom.cpm.shared.model.RootModelElement;
import com.tom.cpm.shared.model.RootModelType;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

public class PlayermodelListener implements SimpleSynchronousResourceReloadListener {
	private final String NAMESPACE;

	public PlayermodelListener(String namespace) {
		this.NAMESPACE = namespace;
	}

	@Override
	public Identifier getFabricId() {
		return Identifier.of(this.NAMESPACE, "mqm_playermodels");
	}

	@Override
	public void reload(ResourceManager manager) {
		for(ParsedCharacter character : RegistryManager.CHARACTERS) {
			if(!character.RESOURCE_ID.getNamespace().equals(this.NAMESPACE)) continue;

			character.MODELS.clear();

			String location = "playermodels/" + character.ID.getPath();
			Map<Identifier, Resource> modelResources = manager.findResources(location,
							path -> path.getPath().toLowerCase(Locale.ROOT).endsWith(".cpmmodel"));

			for (Map.Entry<Identifier, Resource> entry : modelResources.entrySet()) {
				try(InputStream stream = entry.getValue().getInputStream()) {
					String fileName = entry.getKey().getPath().substring(location.length() + 1, entry.getKey().getPath().length() - 9);

					int separationPoint = fileName.indexOf('/');

					String power_namespace = fileName.substring(0, separationPoint);
					if(power_namespace.equals("mario_qua_mario")) power_namespace = "mqm"; // this is so awful
					String power_path = fileName.substring(separationPoint + 1);

					Identifier powerUpID = Identifier.of(power_namespace, power_path);
					ParsedPowerUp powerUp = RegistryManager.POWER_UPS.get(powerUpID);

					if(powerUp != null) {
						MarioQuaMario.LOGGER.info("Found model for character {} in form {}!", character.ID, powerUpID);
						ModelFile model = ModelFile.load(stream);
//						ModelDefinition definition = MinecraftClientAccess.get().getDefinitionLoader().loadModel(model.getDataBlock(), MinecraftClientAccess.get().getClientPlayer());

//						if(definition.hasRoot(RootModelType.CAPE)) {
//							RootModelElement element = definition.getModelElementFor(RootModelType.CAPE).get();
//							MarioQuaMario.LOGGER.info("This model has a tail! {} @ {}", element, element.getPos());
//						}
//						else {
//							MarioQuaMario.LOGGER.info("This model has no CAPE, which means no tail.");
//						}

//						Vec3f i = MinecraftClientAccess.get().getDefinitionLoader().loadModel(model.getDataBlock(), MinecraftClientAccess.get().getClientPlayer()).getModelElementFor(RootModelType.CAPE).get().getPos();
//						MarioQuaMario.LOGGER.info("Cape position for this model: {}", i);

						character.MODELS.put(powerUp, model);
						if(!character.ID.getNamespace().equals(powerUp.ID.getNamespace()))
							MarioQuaMario.LOGGER.info("They have different namespaces too! Look at you, being so compatible!");
					}
					else MarioQuaMario.LOGGER.info("Ignoring model for character {} in unregistered power-up form {}.",
							character.ID, powerUpID);
				}
				catch(Exception exception) {
					MarioQuaMario.LOGGER.error(
							"""
									Error occurred while attempting to load Mario Qua Mario playermodels.
									Namespace (this mod is responsible!!): {}
									Json file: {}
									Exception: {}""",
							this.NAMESPACE, entry.getKey(), exception
					);
				}
			}
		}
	}
}

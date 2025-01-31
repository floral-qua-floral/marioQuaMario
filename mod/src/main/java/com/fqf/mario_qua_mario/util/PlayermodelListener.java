package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Objects;

public record PlayermodelListener(String namespace) implements SimpleSynchronousResourceReloadListener {
	@Override
	public Identifier getFabricId() {
		return Identifier.of(this.namespace, "playermodels");
	}

	@Override
	public void reload(ResourceManager manager) {
		for(ParsedCharacter character : RegistryManager.CHARACTERS) {
			character.MODELS.clear();
		}

		Map<Identifier, Resource> i = manager.findResources("playermodels", path -> path.getPath().endsWith(".json"));
		for (Map.Entry<Identifier, Resource> entry : i.entrySet()) {
			try(InputStream stream = entry.getValue().getInputStream()) {
				JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
				Identifier characterID = Identifier.of(json.get("character").getAsString());
				ParsedCharacter character = Objects.requireNonNull(RegistryManager.CHARACTERS.get(characterID),
						"No character with ID " + characterID + " has been registered!");
				if(!character.RESOURCE_ID.getNamespace().equals(this.namespace))
					throw new MismatchedNamespaceException(this.namespace, characterID);
				Identifier formID = Identifier.of(json.get("form").getAsString());
				ParsedPowerUp form = Objects.requireNonNull(RegistryManager.POWER_UPS.get(formID),
						"No power-up form with ID " + formID + " has been registered!");

				character.MODELS.put(form, json.get("model_string").getAsString());
			}
			catch(Exception exception) {
				MarioQuaMario.LOGGER.error(
						"""
								Error occurred while attempting to load Mario Qua Mario playermodels.
								Namespace: {}
								Json file: {}
								Exception: {}""",
						this.namespace, entry.getKey(), exception
				);
			}
		}
	}

	private static class MismatchedNamespaceException extends IllegalStateException {
		public MismatchedNamespaceException(String listenerNamespace, Identifier characterID) {
			super("A JSON file under the namespace " + listenerNamespace
					+ " is trying to assign a player model to the character " + characterID);
		}
	}
}

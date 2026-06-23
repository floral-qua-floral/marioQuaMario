package com.fqf.charaformact.registries.power_granting;

import net.minecraft.util.Identifier;

import java.util.Objects;

public class AppearanceKey {
	public final Identifier CHARACTER;
	public final Identifier FORM;

	public AppearanceKey(Identifier character, Identifier form) {
		this.CHARACTER = character;
		this.FORM = form;
	}
	public AppearanceKey(ParsedCharacter character, ParsedForm form) {
		this(character.ID, form.ID);
	}

	@Override
	public String toString() {
		if(this.CHARACTER.getNamespace().equals(this.FORM.getNamespace())) {
			String namespace = this.CHARACTER.getNamespace();
			String form = this.FORM.getPath();
			String character = this.CHARACTER.getPath();
			return "AK(" + namespace + ":[" + form + "_" + character + "])";
		}
		return "AK[" + this.FORM + " & " + this.CHARACTER + "]";
	}

	@Override
	public boolean equals(Object obj) {
		// Written under the expectation that ParsedCharacters and ParsedForms will all be effectively singletons.
		return obj == this || (
				obj instanceof AppearanceKey otherKey
				&& otherKey.CHARACTER == this.CHARACTER
				&& otherKey.FORM == this.FORM
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.CHARACTER, this.FORM);
	}

	public static class Registerable extends AppearanceKey {
		public final Identifier ID;

		public Registerable(Identifier id, Identifier character, Identifier form) {
			super(character, form);
			this.ID = id;
		}
	}
}

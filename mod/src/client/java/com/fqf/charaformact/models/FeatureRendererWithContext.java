package com.fqf.charaformact.models;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.util.TransformationContext;
import org.jetbrains.annotations.Nullable;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.fqf.charaformact.util.TransformationContext.*;

public interface FeatureRendererWithContext {
	@Nullable TransformationContext cfa$getContext();

	static TransformationContext getAssumedContext(Class<?> clazz) {
		String name = clazz.getSimpleName();
		if(
				checkContains(name, "back", true, false)
				|| checkContains(name, "glove", false, false)
				|| checkContains(name, "hat", true, true)
		)
			return SPECIAL;

		return UNKNOWN;
	}

	private static boolean checkContains(String name, String substring, boolean mustOpenWord, boolean mustTerminateWord) {
		Pattern pattern = Pattern.compile(substring, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(name);

		while(matcher.find()) {
			char firstCharacter = name.charAt(matcher.start());
			if(
					!mustOpenWord
					|| Character.isUpperCase(firstCharacter)
			) {
				int nextCharacterIndex = matcher.end();
				if (
						!mustTerminateWord
						|| nextCharacterIndex >= name.length()
						|| Character.isUpperCase(name.charAt(nextCharacterIndex))
						|| Character.isDigit(name.charAt(nextCharacterIndex))
				)
					return true;
			}
		}

		return false;
	}
}

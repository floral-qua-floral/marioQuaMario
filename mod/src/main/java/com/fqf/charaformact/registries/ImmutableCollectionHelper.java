package com.fqf.charaformact.registries;

import com.fqf.charaformact_api.CharaFormActAddon;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ImmutableCollectionHelper {
	public static <T> ImmutableSet<T> accumulateSet(Consumer<ImmutableSet.Builder<T>> accumulator) {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		accumulator.accept(builder);
		return builder.build();
	}
	public static <T> ImmutableList<T> accumulateList(Consumer<ImmutableList.Builder<T>> accumulator) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		accumulator.accept(builder);
		return builder.build();
	}
	public static <T1, T2> ImmutableMap<T1, T2> accumulateMap(Consumer<ImmutableMap.Builder<T1, T2>> accumulator) {
		ImmutableMap.Builder<T1, T2> builder = ImmutableMap.builder();
		accumulator.accept(builder);
		return builder.build();
	}

	public static <Extra, T> ImmutableSet<T> accumulateSet(
			Collection<Extra> extras, BiConsumer<Extra, ImmutableSet.Builder<T>> accumulator
	) {
		return accumulateSet(builder -> extras.forEach(extra -> accumulator.accept(extra, builder)));
	}
	public static <Extra, T> ImmutableList<T> accumulateList(
			Collection<Extra> extras, BiConsumer<Extra, ImmutableList.Builder<T>> accumulator
	) {
		return accumulateList(builder -> extras.forEach(extra -> accumulator.accept(extra, builder)));
	}
	public static <Extra, T1, T2> ImmutableMap<T1, T2> accumulateMap(
			Collection<Extra> extras, BiConsumer<Extra, ImmutableMap.Builder<T1, T2>> accumulator
	) {
		return accumulateMap(builder -> extras.forEach(extra -> accumulator.accept(extra, builder)));
	}
}

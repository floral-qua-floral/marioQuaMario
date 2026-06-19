package com.fqf.charaformact.registries;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.definitions.states.CfaStateDefinition;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ParsedCfaState extends ParsedCfaThing {
	private final @NotNull CfaStateDefinition STATE_DEFINITION;

	private @Nullable Class<?> lastCustomVarsClass;

	public ParsedCfaState(CfaStateDefinition definition) {
		super(definition.defineID());
		this.STATE_DEFINITION = definition;
	}

	public void serverTick(CfaServerPlayerData data) {
		this.STATE_DEFINITION.serverTick(data);
	}
	public void clientTick(CfaClientData data, boolean isSelf) {
		this.STATE_DEFINITION.clientTick(data, isSelf);
	}
	public Object makeCustomThing(CfaPlayerData data) {
		Object vars = this.STATE_DEFINITION.provideStateData(data);
		if(vars != null) this.lastCustomVarsClass = vars.getClass();
		return vars;
	}
	public @Nullable Class<?> getLastCustomVarsClass() {
		return this.lastCustomVarsClass;
	}

	public void onEnter(CfaPlayerData data) {
		this.STATE_DEFINITION.onEnter(data);
	}
	public void onExit(CfaPlayerData data) {
		this.STATE_DEFINITION.onExit(data);
	}

	public static <T> Set<T> accumulateSet(Consumer<ImmutableSet.Builder<T>> accumulator) {
		ImmutableSet.Builder<T> builder = ImmutableSet.builder();
		accumulator.accept(builder);
		return builder.build();
	}
	public static <T> List<T> accumulateList(Consumer<ImmutableList.Builder<T>> accumulator) {
		ImmutableList.Builder<T> builder = ImmutableList.builder();
		accumulator.accept(builder);
		return builder.build();
	}
}

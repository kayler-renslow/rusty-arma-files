package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 @author K
 @since 01/08/2019 */
public class ConfigQueryPatternBuilder {

	private final ClassItem parent;
	private ConfigQueryPatternBuilder childBuilder;

	private ConfigQueryPatternBuilder(@NotNull ClassItem parent) {
		this.parent = parent;
	}

	@NotNull
	public static ConfigQueryPatternBuilder matchClass(@NotNull String name) {
		ClassItem root = new ClassItem("");
		ConfigQueryPatternBuilder builder = new ConfigQueryPatternBuilder(root);
		root.children.put(name, new ClassItem(name));
		return builder;
	}

	@NotNull
	public static ConfigQueryPatternBuilder matchClasses(@NotNull String... names) {
		ClassItem root = new ClassItem("");
		ConfigQueryPatternBuilder builder = new ConfigQueryPatternBuilder(root);
		for (String name : names) {
			root.children.put(name, new ClassItem(name));
		}
		return builder;
	}

	@NotNull
	public ConfigQueryPatternBuilder matchClassInside(@NotNull String name) {
		parent.children.put(name, new ClassItem(name));
		return this;
	}

	@NotNull
	public ConfigQueryPatternBuilder matchClassInside(@NotNull String... names) {
		for (String name : names) {
			parent.children.put(name, new ClassItem(name));
		}
		return this;
	}

	@NotNull
	public ConfigQueryPatternBuilder matchClassInsideAndEnter(@NotNull String name) {
		ClassItem child = new ClassItem(name);
		parent.children.put(name, child);
		this.childBuilder = new ConfigQueryPatternBuilder(child);
		return this.childBuilder;
	}

	@NotNull
	public ConfigQueryPatternBuilder matchAssignment(@NotNull String name) {
		parent.assignments.add(name);
		return this;
	}

	@NotNull
	public ConfigQueryPatternBuilder matchAssignments(@NotNull String... names) {
		for (String name : names) {
			parent.assignments.add(name);
		}
		return this;
	}

	@NotNull


	private static class ClassItem {
		private final String name;
		private final List<String> assignments = new ArrayList<>();
		private final Map<String, ClassItem> children = new HashMap<>();

		public ClassItem(@NotNull String name) {
			this.name = name;
		}
	}

}

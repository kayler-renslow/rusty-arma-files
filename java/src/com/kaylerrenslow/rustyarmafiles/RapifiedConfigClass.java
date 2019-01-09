package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigClass implements ConfigClass {
	private String name;

	public RapifiedConfigClass(@NotNull String name) {
		this.name = name;
	}

	@Override
	@NotNull
	public String getClassName() {
		return name;
	}

	@Override
	public int getEmbeddedClassesCount() {
		return 0;
	}

	@Override
	public int getAssignmentCount() {
		return 0;
	}

	@Override
	@NotNull
	public ConfigStream newStream(){
		return new RapifiedConfigClassStream(this);
	}

	@Override
	@Nullable
	public native ConfigClass getClass(@NotNull String className);

	@Override
	@Nullable
	public native ConfigFieldValue getAssignmentValue(@NotNull String key);
}

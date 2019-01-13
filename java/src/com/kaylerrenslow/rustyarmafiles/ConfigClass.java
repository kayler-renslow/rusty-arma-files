package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigClass extends ConfigEntry {
	@NotNull String getClassName();

	@NotNull ConfigStream newStream();

	@Nullable ConfigClass getClass(@NotNull String className);

	@Nullable ConfigFieldValue getFieldValue(@NotNull String key);

	@Nullable ConfigEntry getEntry(@NotNull String name);

	@Override
	default boolean isField() {
		return false;
	}

	@Override
	default boolean isClass() {
		return true;
	}

	@Override
	@NotNull
	default String getName() {
		return getClassName();
	}
}

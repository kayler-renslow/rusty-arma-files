package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigField extends ConfigEntry {
	@NotNull String getKey();

	@NotNull ConfigFieldValue getValue();

	@Override
	default boolean isField() {
		return true;
	}

	@Override
	default boolean isClass() {
		return false;
	}

	@Override
	@NotNull
	default String getName() {
		return getKey();
	}
}

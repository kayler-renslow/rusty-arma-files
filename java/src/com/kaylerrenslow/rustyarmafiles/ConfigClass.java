package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigClass {
	@NotNull String getClassName();
	@NotNull ConfigStream newStream();

	@Nullable ConfigClass getClass(@NotNull String className);
	@Nullable ConfigFieldValue getAssignmentValue(@NotNull String key);
}

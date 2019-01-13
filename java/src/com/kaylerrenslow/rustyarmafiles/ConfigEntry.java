package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/13/2019 */
public interface ConfigEntry {
	boolean isField();
	boolean isClass();
	@NotNull String getName();
}

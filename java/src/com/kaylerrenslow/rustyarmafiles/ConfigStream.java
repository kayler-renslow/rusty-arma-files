package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 @author K
 @since 01/08/2019 */
public class ConfigStream {
	public ConfigStream(@NotNull File binarizedConfigFile) {
	}

	@NotNull
	public native ConfigStreamItem advance() throws IllegalStateException;

	public native boolean hasNext();
}

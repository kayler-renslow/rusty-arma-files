package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 @author K
 @since 01/08/2019 */
public class BinarizedConfigStream implements ConfigStream {
	public BinarizedConfigStream(@NotNull File binarizedConfigFile) {
	}
	@Override
	@NotNull
	public native ConfigStreamItem advance() throws IllegalStateException;

	@Override
	public native boolean hasNext();
}

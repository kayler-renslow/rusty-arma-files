package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public class RapifiedConfigFileStream implements ConfigStream {
	private final RapifiedConfigFile file;

	public RapifiedConfigFileStream(@NotNull RapifiedConfigFile file) {
		this.file = file;
	}

	@Override
	@NotNull
	public native ConfigStreamItem next() throws IllegalStateException;

	@Override
	public native boolean hasNext();

	@Override
	public native void skipCurrentClass();
}

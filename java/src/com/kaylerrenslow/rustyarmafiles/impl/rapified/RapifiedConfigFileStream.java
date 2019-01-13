package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigFileStream;
import com.kaylerrenslow.rustyarmafiles.ConfigStreamException;
import com.kaylerrenslow.rustyarmafiles.ConfigStreamItem;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public class RapifiedConfigFileStream extends ConfigFileStream {
	private int currentEntryOffset = -1;

	public RapifiedConfigFileStream(@NotNull RapifiedConfigFile file) {
		super(file);
	}

	@Override
	@NotNull
	public native ConfigStreamItem next() throws ConfigStreamException;

	@Override
	public native boolean hasNext();

	@Override
	public native void skipCurrentClass();
}

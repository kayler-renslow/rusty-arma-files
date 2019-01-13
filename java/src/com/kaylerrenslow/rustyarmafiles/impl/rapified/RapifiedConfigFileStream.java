package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigStream;
import com.kaylerrenslow.rustyarmafiles.ConfigStreamException;
import com.kaylerrenslow.rustyarmafiles.ConfigStreamItem;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public class RapifiedConfigFileStream implements ConfigStream {
	private final RapifiedConfigFile file;
	private int currentOffset = -1;

	public RapifiedConfigFileStream(@NotNull RapifiedConfigFile file) {
		this.file = file;
	}

	@Override
	@NotNull
	public native ConfigStreamItem next() throws ConfigStreamException;

	@Override
	public native boolean hasNext();

	@Override
	public native void skipCurrentClass();
}

package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigStream;
import com.kaylerrenslow.rustyarmafiles.ConfigStreamItem;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigClassStream implements ConfigStream {
	private final RapifiedConfigClass configClass;

	public RapifiedConfigClassStream(@NotNull RapifiedConfigClass configClass) {
		this.configClass = configClass;
	}

	@Override
	@NotNull
	public native ConfigStreamItem next() throws IllegalStateException;

	@Override
	public native boolean hasNext();

	@Override
	public native void skipCurrentClass();
}

package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigEntry;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/13/2019 */
public abstract class RapifiedConfigEntry implements ConfigEntry {
	protected final RapifiedConfigFileStream fileStream;
	protected final int dataOffset;

	public RapifiedConfigEntry(@NotNull RapifiedConfigFileStream fileStream, int dataOffset) {
		this.fileStream = fileStream;
		this.dataOffset = dataOffset;
	}
}

package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 @author K
 @since 01/09/2019 */
public interface ConfigFile {
	@NotNull File getFile();
	@NotNull ConfigStream newStream();
	boolean isRapified();
}

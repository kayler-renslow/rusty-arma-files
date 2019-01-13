package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 @author K
 @since 01/09/2019 */
public interface ConfigFile {
	@NotNull ConfigClass getRoot();
	@NotNull File getFile();
	void parse() throws IOException;
	boolean isParsed();
	@NotNull ConfigStream newStream();
	boolean isRapified();
}

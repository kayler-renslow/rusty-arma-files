package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigFile implements ConfigFile {
	private final File configFile;
	private boolean isParsed = false;
	private ConfigClass root;

	public RapifiedConfigFile(@NotNull File configFile) {
		this.configFile = configFile;
	}

	@Override
	@NotNull
	public ConfigClass getRoot() {
		if (!isParsed) {
			throw new IllegalStateException();
		}
		return root;
	}

	@Override
	@NotNull
	public File getFile() {
		return configFile;
	}

	@Override
	public native void parse() throws IOException;

	@Override
	public boolean isParsed() {
		return isParsed;
	}

	@Override
	@NotNull
	public ConfigStream newStream() {
		if (!isParsed) {
			throw new IllegalStateException();
		}
		return new RapifiedConfigFileStream(this);
	}
}

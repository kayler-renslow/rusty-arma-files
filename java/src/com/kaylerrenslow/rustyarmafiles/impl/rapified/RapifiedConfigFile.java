package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigClass;
import com.kaylerrenslow.rustyarmafiles.ConfigFile;
import com.kaylerrenslow.rustyarmafiles.ConfigStream;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigFile implements ConfigFile {
	private final File configFile;
	private boolean isParsed;
	private RapifiedConfigClass configClass;

	public RapifiedConfigFile(@NotNull File configFile) {
		this.configFile = configFile;
	}

	@Override
	@NotNull
	public ConfigClass getRoot() {
		if (!isParsed) {
			throw new IllegalStateException();
		}
		return configClass;
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
		if (isParsed) {
			return configClass.newStream();
		}
		return new RapifiedConfigFileStream(this);
	}

	@Override
	public boolean isRapified() {
		return true;
	}
}

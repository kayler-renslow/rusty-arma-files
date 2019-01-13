package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigFile;
import com.kaylerrenslow.rustyarmafiles.ConfigStream;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigFile implements ConfigFile {
	private final File configFile;

	public RapifiedConfigFile(@NotNull File configFile) {
		this.configFile = configFile;
	}

	@Override
	@NotNull
	public File getFile() {
		return configFile;
	}

	@Override
	@NotNull
	public ConfigStream newStream() {
		return new RapifiedConfigFileStream(this);
	}

	@Override
	public boolean isRapified() {
		return true;
	}
}

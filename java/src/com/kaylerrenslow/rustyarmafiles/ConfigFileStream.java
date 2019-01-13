package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/13/2019 */
public abstract class ConfigFileStream implements ConfigStream {
	protected final ConfigFile configFile;

	public ConfigFileStream(@NotNull ConfigFile configFile) {
		this.configFile = configFile;
	}
}

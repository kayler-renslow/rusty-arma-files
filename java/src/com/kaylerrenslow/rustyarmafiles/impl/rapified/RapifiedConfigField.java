package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.ConfigField;
import com.kaylerrenslow.rustyarmafiles.ConfigFieldValue;
import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/13/2019 */
public class RapifiedConfigField extends RapifiedConfigEntry implements ConfigField {

	private final String key;

	public RapifiedConfigField(@NotNull RapifiedConfigFileStream fileStream, int dataOffset, @NotNull String key) {
		super(fileStream, dataOffset);
		this.key = key;
	}

	@Override
	@NotNull
	public String getKey() {
		return key;
	}

	@Override
	@NotNull
	public native ConfigFieldValue getValue();
}

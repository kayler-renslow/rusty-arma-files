package com.kaylerrenslow.rustyarmafiles.impl.rapified;

import com.kaylerrenslow.rustyarmafiles.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 @author K
 @since 01/09/2019 */
public class RapifiedConfigClass extends RapifiedConfigEntry implements ConfigClass {
	private String className;
	private Entry[] fieldEntries;
	private Entry[] classEntries;
	private boolean parsed;

	public RapifiedConfigClass(@NotNull RapifiedConfigFileStream fileStream, int bodyOffset, @NotNull String className) {
		super(fileStream, bodyOffset);
		this.className = className;
		this.parsed = false;
	}

	@Override
	@NotNull
	public String getClassName() {
		return className;
	}

	@Override
	@NotNull
	public ConfigStream newStream() {
		return new RapifiedConfigClassStream(this);
	}

	@Override
	@Nullable
	public ConfigClass getClass(@NotNull String className) {
		if (!parsed) {
			parse();
		}
		for(Entry entry : classEntries) {
			if (entry.getName().equals(className)) {
				return (ConfigClass) entry.getAsConfigEntry();
			}
		}
		return null;
	}

	@Override
	@Nullable
	public ConfigFieldValue getFieldValue(@NotNull String key) {
		if (!parsed) {
			parse();
		}
		for(Entry entry : fieldEntries) {
			if (entry.getName().equals(key)) {
				return ((ConfigField) entry.getAsConfigEntry()).getValue();
			}
		}
		return null;
	}

	private native void parse();

	@Override
	@Nullable
	public ConfigEntry getEntry(@NotNull String name) {
		if (!parsed) {
			parse();
		}
		for (Entry entry : classEntries) {
			if (entry.getName().equals(name)) {
				return entry.getAsConfigEntry();
			}
		}
		for (Entry entry : fieldEntries) {
			if (entry.getName().equals(name)) {
				return entry.getAsConfigEntry();
			}
		}
		return null;
	}

	private class Entry {
		private boolean isClass;
		private int nameOffset;
		private int dataOffset;

		public Entry(boolean isClass, int dataOffset, int nameOffset) {
			this.isClass = isClass;
			this.dataOffset = dataOffset;
			this.nameOffset = nameOffset;
		}

		@NotNull
		public ConfigEntry getAsConfigEntry() {
			if (isClass) {
				return new RapifiedConfigClass(fileStream, dataOffset, getName());
			}
			return new RapifiedConfigField(fileStream, dataOffset, getName());
		}

		@NotNull
		public native String getName();
	}
}

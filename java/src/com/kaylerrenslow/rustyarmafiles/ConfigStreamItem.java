package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigStreamItem {
	enum Type {
		Class, EndClass, Field, EndStream
	}

	@NotNull Type getType();

	class EndStreamItem implements ConfigStreamItem {

		public static final EndStreamItem INSTANCE = new EndStreamItem();

		private EndStreamItem() {
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.EndStream;
		}
	}

	class ClassItem implements ConfigStreamItem {

		private final String className;

		public ClassItem(@NotNull String className) {
			this.className = className;
		}

		@NotNull
		public String getClassName() {
			return className;
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.Class;
		}
	}

	class EndClassItem implements ConfigStreamItem {
		public static final EndClassItem INSTANCE = new EndClassItem();

		private EndClassItem() {
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.EndClass;
		}
	}

	class FieldItem implements ConfigStreamItem {
		private final String key;
		private final ConfigFieldValue value;

		public FieldItem(@NotNull String key, @NotNull ConfigFieldValue value) {
			this.key = key;
			this.value = value;
		}

		@NotNull
		public String getKey() {
			return key;
		}

		@NotNull
		public ConfigFieldValue getValue() {
			return value;
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.Field;
		}
	}
}

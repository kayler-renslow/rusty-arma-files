package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigStreamItem {
	enum Type {
		Class, SubClass, EndSubClass, Assignment
	}

	@NotNull Type getType();

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

	class SubClassItem implements ConfigStreamItem {
		private final String className;

		public SubClassItem(@NotNull String className) {
			this.className = className;
		}

		@NotNull
		public String getClassName() {
			return className;
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.SubClass;
		}
	}

	class EndSubClassMarker implements ConfigStreamItem {
		public static final EndSubClassMarker INSTANCE = new EndSubClassMarker();

		private EndSubClassMarker() {
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.EndSubClass;
		}
	}

	class AssignmentItem implements ConfigStreamItem {
		private final String key;
		private final String value;

		public AssignmentItem(@NotNull String key, @NotNull String value) {
			this.key = key;
			this.value = value;
		}

		@NotNull
		public String getKey() {
			return key;
		}

		@NotNull
		public String getValue() {
			return value;
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.Assignment;
		}
	}
}

package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigStreamItem {
	enum Type {
		Class, EndClass, Assignment, ClassSkipDone
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

	class ClassSkipDoneMarker implements ConfigStreamItem {
		public static final ClassSkipDoneMarker INSTANCE = new ClassSkipDoneMarker();

		private ClassSkipDoneMarker() {
		}

		@Override
		@NotNull
		public Type getType() {
			return Type.ClassSkipDone;
		}
	}

	class AssignmentItem implements ConfigStreamItem {
		private final String key;
		private final ConfigFieldValue value;

		public AssignmentItem(@NotNull String key, @NotNull ConfigFieldValue value) {
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
			return Type.Assignment;
		}
	}
}

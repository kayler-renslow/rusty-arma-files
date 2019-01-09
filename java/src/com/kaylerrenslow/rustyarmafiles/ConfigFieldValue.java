package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigFieldValue {
	class Scalar implements ConfigFieldValue {
		private final double value;

		public Scalar(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}

	class Array implements ConfigFieldValue {
		private final List<ConfigFieldValue> values;

		public Array(@NotNull List<ConfigFieldValue> values) {
			this.values = values;
		}

		@NotNull
		public List<ConfigFieldValue> getValues() {
			return values;
		}
	}
}

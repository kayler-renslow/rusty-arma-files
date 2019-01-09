package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigValue {
	class Scalar implements ConfigValue {
		private final double value;

		public Scalar(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}

	class Array implements ConfigValue {
		private final List<ConfigValue> values;

		public Array(@NotNull List<ConfigValue> values) {
			this.values = values;
		}

		@NotNull
		public List<ConfigValue> getValues() {
			return values;
		}
	}
}

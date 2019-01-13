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
		private final Object[] values;

		public Array(@NotNull List<ConfigFieldValue> values) {
			this.values = new Object[values.size()];
			int i = 0;
			for (ConfigFieldValue v : values) {
				this.values[i++] = v;
			}
		}

		public int getLength() {
			return values.length;
		}

		@NotNull
		public ConfigFieldValue getValue(int i) {
			if (i < 0 || i >= values.length) {
				throw new IndexOutOfBoundsException(i);
			}
			return (ConfigFieldValue) values[i];
		}
	}
}

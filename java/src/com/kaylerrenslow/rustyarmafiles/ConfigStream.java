package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigStream {
	@NotNull
	ConfigStreamItem next() throws IllegalStateException;

	boolean hasNext();

	void skipCurrentClass();
}

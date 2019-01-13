package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

/**
 @author K
 @since 01/08/2019 */
public interface ConfigStream {
	@NotNull
	ConfigStreamItem next() throws ConfigStreamException;

	boolean hasNext();

	void skipCurrentClass();
}

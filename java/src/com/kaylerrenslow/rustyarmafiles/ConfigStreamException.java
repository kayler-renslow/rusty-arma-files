package com.kaylerrenslow.rustyarmafiles;

/**
 @see ConfigStream
 @author K
 @since 01/13/2019 */
public class ConfigStreamException extends Exception {
	public ConfigStreamException() {
	}

	public ConfigStreamException(String message) {
		super(message);
	}

	public ConfigStreamException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigStreamException(Throwable cause) {
		super(cause);
	}

	public ConfigStreamException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}

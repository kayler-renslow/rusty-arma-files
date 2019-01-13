package com.kaylerrenslow.rustyarmafiles;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 @author K
 @since 01/09/2019 */
public interface ConfigFile {
	/**
	 Gets the root {@link ConfigClass} of the file.
	 The root {@link ConfigClass} has no class name, thus {@link ConfigClass#getClassName()} will return "".
	 The root is really just a container for file level entries.

	 @return the root
	 @throws IllegalStateException when {@link #parse()} hasn't been invoked.
	 */
	@NotNull ConfigClass getRoot();

	/** @return {@link File} instance for the {@link ConfigFile} */
	@NotNull File getFile();

	/**
	 Parses the file and sets {@link #getRoot()}.
	 This method will also set the flag so that {@link #isParsed()} returns true if no exception was thrown;

	 @throws IOException    when a generic IO exception occurs
	 @throws ParseException when a parsing error happened
	 */
	void parse() throws IOException, ParseException;

	/**
	 @return true if {@link #parse()} has been invoked and no exception occurred in the parsing process,
	 false otherwise
	 */
	boolean isParsed();

	/**
	 Returns a {@link ConfigStream} on {@link #getRoot()}, or a {@link ConfigFileStream} if {@link #isParsed()}
	 returns false.

	 @return a new stream
	 */
	@NotNull ConfigStream newStream();

	/** @return true if the config is binarized/rapified, false if it is plain text */
	boolean isRapified();
}

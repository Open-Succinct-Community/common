package com.venky.core.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class InfoFileHandler extends FileHandler{

	public InfoFileHandler() throws IOException, SecurityException {
		super();
		setLevel(Level.INFO);
	}

	public InfoFileHandler(String pattern, boolean append) throws IOException, SecurityException {
		super(pattern, append);
		setLevel(Level.INFO);
	}

	public InfoFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
		super(pattern, limit, count, append);
		setLevel(Level.INFO);
	}

	public InfoFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
		super(pattern, limit, count);
		setLevel(Level.INFO);
	}

	public InfoFileHandler(String pattern) throws IOException, SecurityException {
		super(pattern);
		setLevel(Level.INFO);
	}

}

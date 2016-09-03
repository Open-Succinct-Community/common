package com.venky.core.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;

public class WarningFileHandler  extends FileHandler{

	public WarningFileHandler() throws IOException, SecurityException {
		super();
		setLevel(Level.WARNING);
	}

	public WarningFileHandler(String pattern, boolean append) throws IOException, SecurityException {
		super(pattern, append);
		setLevel(Level.WARNING);
	}

	public WarningFileHandler(String pattern, int limit, int count, boolean append) throws IOException, SecurityException {
		super(pattern, limit, count, append);
		setLevel(Level.WARNING);
	}

	public WarningFileHandler(String pattern, int limit, int count) throws IOException, SecurityException {
		super(pattern, limit, count);
		setLevel(Level.WARNING);
	}

	public WarningFileHandler(String pattern) throws IOException, SecurityException {
		super(pattern);
		setLevel(Level.WARNING);
	}
	
}

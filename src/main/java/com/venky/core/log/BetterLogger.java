package com.venky.core.log;

import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.venky.core.log.TimerStatistics.Timer;

public class BetterLogger extends Logger {

	private final Logger logger;

	
	public BetterLogger(Logger logger) {
		super(logger.getName(), logger.getResourceBundleName());
		this.logger = logger;
	}
	
	public ResourceBundle getResourceBundle() {
		return logger.getResourceBundle();
	}
	public String getResourceBundleName() {
		return logger.getResourceBundleName();
	}
	public void setFilter(Filter newFilter) throws SecurityException {
		logger.setFilter(newFilter);
	}
	public Filter getFilter() {
		return logger.getFilter();
	}
	public void log(LogRecord record) {
		logger.log(record);
	}
	public void log(Level level, String msg) {
		logger.log(level, msg);
	}
	public void log(Level level, Supplier<String> msgSupplier) {
		logger.log(level, msgSupplier);
	}
	public void log(Level level, String msg, Object param1) {
		logger.log(level, msg, param1);
	}
	public void log(Level level, String msg, Object[] params) {
		logger.log(level, msg, params);
	}
	public void log(Level level, String msg, Throwable thrown) {
		logger.log(level, msg, thrown);
	}
	public void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
		logger.log(level, thrown, msgSupplier);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, String msg) {
		logger.logp(level, sourceClass, sourceMethod, msg);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, Supplier<String> msgSupplier) {
		logger.logp(level, sourceClass, sourceMethod, msgSupplier);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object param1) {
		logger.logp(level, sourceClass, sourceMethod, msg, param1);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Object[] params) {
		logger.logp(level, sourceClass, sourceMethod, msg, params);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, String msg, Throwable thrown) {
		logger.logp(level, sourceClass, sourceMethod, msg, thrown);
	}
	public void logp(Level level, String sourceClass, String sourceMethod, Throwable thrown,
			Supplier<String> msgSupplier) {
		logger.logp(level, sourceClass, sourceMethod, thrown, msgSupplier);
	}
	@Deprecated
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg) {
		logger.logrb(level, sourceClass, sourceMethod, bundleName, msg);
	}
	@Deprecated
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
			Object param1) {
		logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, param1);
	}
	@Deprecated
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
			Object[] params) {
		logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, params);
	}
	
	public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
			Object... params) {
		logger.logrb(level, sourceClass, sourceMethod, bundle, msg, params);
	}
	
	@Deprecated
	public void logrb(Level level, String sourceClass, String sourceMethod, String bundleName, String msg,
			Throwable thrown) {
		logger.logrb(level, sourceClass, sourceMethod, bundleName, msg, thrown);
	}
	public void logrb(Level level, String sourceClass, String sourceMethod, ResourceBundle bundle, String msg,
			Throwable thrown) {
		logger.logrb(level, sourceClass, sourceMethod, bundle, msg, thrown);
	}
	public void entering(String sourceClass, String sourceMethod) {
		logger.entering(sourceClass, sourceMethod);
	}
	public void entering(String sourceClass, String sourceMethod, Object param1) {
		logger.entering(sourceClass, sourceMethod, param1);
	}
	public void entering(String sourceClass, String sourceMethod, Object[] params) {
		logger.entering(sourceClass, sourceMethod, params);
	}
	public void exiting(String sourceClass, String sourceMethod) {
		logger.exiting(sourceClass, sourceMethod);
	}
	public void exiting(String sourceClass, String sourceMethod, Object result) {
		logger.exiting(sourceClass, sourceMethod, result);
	}
	public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
		logger.throwing(sourceClass, sourceMethod, thrown);
	}
	public void severe(String msg) {
		logger.severe(msg);
	}
	public void warning(String msg) {
		logger.warning(msg);
	}
	public void info(String msg) {
		logger.info(msg);
	}
	public void config(String msg) {
		logger.config(msg);
	}
	public void fine(String msg) {
		logger.fine(msg);
	}
	public void finer(String msg) {
		logger.finer(msg);
	}
	public void finest(String msg) {
		logger.finest(msg);
	}
	public void severe(Supplier<String> msgSupplier) {
		logger.severe(msgSupplier);
	}
	public void warning(Supplier<String> msgSupplier) {
		logger.warning(msgSupplier);
	}
	public void info(Supplier<String> msgSupplier) {
		logger.info(msgSupplier);
	}
	public void config(Supplier<String> msgSupplier) {
		logger.config(msgSupplier);
	}
	public void fine(Supplier<String> msgSupplier) {
		logger.fine(msgSupplier);
	}
	public void finer(Supplier<String> msgSupplier) {
		logger.finer(msgSupplier);
	}
	public void finest(Supplier<String> msgSupplier) {
		logger.finest(msgSupplier);
	}
	public void setLevel(Level newLevel) throws SecurityException {
		logger.setLevel(newLevel);
	}
	public Level getLevel() {
		return logger.getLevel();
	}
	public boolean isLoggable(Level level) {
		return logger.isLoggable(level);
	}
	public String getName() {
		return logger.getName();
	}
	public void addHandler(Handler handler) throws SecurityException {
		logger.addHandler(handler);
	}
	public void removeHandler(Handler handler) throws SecurityException {
		logger.removeHandler(handler);
	}
	public Handler[] getHandlers() {
		return logger.getHandlers();
	}
	public void setUseParentHandlers(boolean useParentHandlers) {
		logger.setUseParentHandlers(useParentHandlers);
	}
	public boolean getUseParentHandlers() {
		return logger.getUseParentHandlers();
	}
	public void setResourceBundle(ResourceBundle bundle) {
		logger.setResourceBundle(bundle);
	}
	public Logger getParent() {
		return logger.getParent();
	}
	public void setParent(Logger parent) {
		logger.setParent(parent);
	}

	public Timer startTimer(String ctx, boolean additive){ 
		if (isLoggable(ExtendedLevel.TIMER)){ 
			return Timer.startTimer(ctx,additive);
		}else {
			return Timer.DUMMY;
		}
	}
	
	 
}

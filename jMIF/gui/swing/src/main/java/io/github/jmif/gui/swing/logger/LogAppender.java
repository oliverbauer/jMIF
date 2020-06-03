package io.github.jmif.gui.swing.logger;

import java.awt.EventQueue;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.Status;
import io.github.jmif.config.Configuration;

public class LogAppender implements Appender<LoggingEvent> {
	public static LogView mifLogView;
	
	@Override
	public void doAppend(LoggingEvent event) {
		
	    EventQueue.invokeLater(() -> {
	    	String loggername = event.getLoggerName();
	    	String formattedMessage = event.getFormattedMessage();
	    	Level level = event.getLevel();
	    		
	    	if (Configuration.disableDebug && level == Level.DEBUG) {
	    		return;
	    	}
	    		
	    	String thread = event.getThreadName();
	    		
	    	long timestamp = event.getTimeStamp();
	    		
	    	if (mifLogView != null) {
	    		mifLogView.newMessage(timestamp, thread, level, loggername, formattedMessage);
	    	} else {
	    		System.out.println("MIGLogView not yet set...: "+event);
	        }
	    });
	}
	
	@Override
	public void start() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public boolean isStarted() {
		return false;
	}

	@Override
	public void setContext(Context context) {
	}

	@Override
	public Context getContext() {
		return null;
	}

	@Override
	public void addStatus(Status status) {
	}

	@Override
	public void addInfo(String msg) {
	}

	@Override
	public void addInfo(String msg, Throwable ex) {
	}

	@Override
	public void addWarn(String msg) {
	}

	@Override
	public void addWarn(String msg, Throwable ex) {
	}

	@Override
	public void addError(String msg) {
	}

	@Override
	public void addError(String msg, Throwable ex) {
	}

	@Override
	public void addFilter(Filter<LoggingEvent> newFilter) {
	}

	@Override
	public void clearAllFilters() {
	}

	@Override
	public List<Filter<LoggingEvent>> getCopyOfAttachedFiltersList() {
		return null;
	}

	@Override
	public FilterReply getFilterChainDecision(LoggingEvent event) {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public void setName(String name) {
	}
	
}

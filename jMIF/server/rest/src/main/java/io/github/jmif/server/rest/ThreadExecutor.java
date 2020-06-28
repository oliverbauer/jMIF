/**
 * 
 */
package io.github.jmif.server.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.github.jmif.core.MIFException;

/**
 * @author thebrunner
 *
 */
public class ThreadExecutor {

	private final static int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);
	
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(MAX_THREADS);
	
	private final Map<Long, Future<?>> executables = new HashMap<Long, Future<?>>(MAX_THREADS);
	
	private long identifier = Long.MIN_VALUE;
	
	public synchronized long doIt(Callable<?> callable) {
		var id = inkrement();
		executables.put(id, executor.submit(callable));
		return id;
	}
	
	public synchronized Future<?> get(long id) throws MIFException {
		var result = executables.get(id);
		if (result.isDone() || result.isCancelled()) {
			executables.remove(id);
		}
		return result;
	}
	
	private synchronized long inkrement() {
		var result = identifier++;
		if (result == Long.MAX_VALUE) {
			identifier = Long.MIN_VALUE;
		}
		return result;
	}
}

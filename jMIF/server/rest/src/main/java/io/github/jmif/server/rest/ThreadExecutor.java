/**
 * 
 */
package io.github.jmif.server.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import io.github.jmif.MIFException;

/**
 * @author thebrunner
 *
 */
public class ThreadExecutor {

	private final static int MAX_THREADS = 100;
	
	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(MAX_THREADS);
	
	private final Map<Long, Future<?>> executables = new HashMap<Long, Future<?>>(MAX_THREADS);
	
	private long identifier = Long.MIN_VALUE;
	
	public synchronized long doIt(Callable<?> callable) {
		var id = inkrement();
		executables.put(id, executor.submit(callable));
		return id;
	}
	
	public synchronized Optional<?> get(long id) throws MIFException {
		var result = executables.get(id);
		if (result.isDone()) {
			executables.remove(id);
			try {
				return Optional.of(result.get());
			} catch (InterruptedException | ExecutionException e) {
				throw new MIFException(e);
			}
		}
		return Optional.empty();
	}
	
	private synchronized long inkrement() {
		var result = identifier++;
		if (result == Long.MAX_VALUE) {
			identifier = Long.MIN_VALUE;
		}
		return result;
	}
}

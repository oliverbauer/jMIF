package io.github.jmif.util;

import org.apache.commons.lang3.time.DurationFormatUtils;

public class TimeUtil {
	private TimeUtil() {
		// Utility class
	}
	
	public static String getMessage(long startTime) {
		long millis = System.currentTimeMillis()-startTime;
		return DurationFormatUtils.formatDuration(millis, "HH:mm:ss,SSS")+"[HH:mm:ss,SSS]";
	}
}

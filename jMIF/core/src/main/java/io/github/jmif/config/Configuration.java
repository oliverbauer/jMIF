package io.github.jmif.config;

import java.util.Arrays;
import java.util.List;

public class Configuration {
	public static final List<String> allowedImageTypes = Arrays.asList("jpg", "JPG", "png", "PNG");
	public static final List<String> allowedVideoTypes = Arrays.asList("mp4", "MP4", "mpg", "avi");
	
	public static int pixelwidth_per_second = 25;
	public static int timelineentryHeight = 15;
}

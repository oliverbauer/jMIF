package io.github.jmif.config;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class Configuration {
	public static boolean showSplashscreen = true;
	public static final int splashSleepTime = 10;
	
	public static final List<String> allowedImageTypes = Arrays.asList("jpg", "JPG");
	public static final List<String> allowedVideoTypes = Arrays.asList("mp4", "MP4", "mpg", "avi");
	
	public static int timelineentryHeight = 20;
	
	public static int transperencyOffset = 10; // 10
	
	public static Color bgColor = new Color(1.0f, 1.0f, 1.0f, 1.0f); // 1.0f
//	public static Color bgColor = new Color(0f, 0f, 0f, 1.0f); // 1.0f
	
	public static final int fps = 25;
	
	public static boolean useBorders = false;
	
	public static boolean disableDebug = true;
	
	// TODO Config: Show new or old menubar
	public static boolean showNewMenubar = true;
	public static Color menubarBackground = new Color(135, 206, 250);
}

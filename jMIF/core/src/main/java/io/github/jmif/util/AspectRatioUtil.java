package io.github.jmif.util;

public class AspectRatioUtil {
	/**
	 * An image of size 5184x3888 which is 4:3 will result for a preferred height of 200 in ... of width.
	 * A video of size 1920x1080 which is 16:9 will result for a preferred height of 200 in ... of width.
	 * 
	 * @param origWidth
	 * @param origHeight
	 * @param preferredHeight
	 * @return
	 */
	public double getWidth(int origWidth, int origHeight, int preferredHeight) {
		return (preferredHeight * origWidth) / origHeight;
	}
	
	/**
	 * An image of size 5184x3888 which is 4:3 will result for a preferred width of 200 in ... of height.
	 * A video of size 1920x1080 which is 16:9 will result for a preferred width of 200 in ... of height.
	 * 
	 * @param origWidth
	 * @param origHeight
	 * @param preferredWidth
	 * @return
	 */
	public double getHeight(int origWidth, int origHeight, int preferredWidth) {
	    return (preferredWidth * origHeight) / origWidth;
	}
}

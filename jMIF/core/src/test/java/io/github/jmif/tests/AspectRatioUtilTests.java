package io.github.jmif.tests;

import org.junit.Assert;
import org.junit.Test;

import io.github.jmif.util.AspectRatioUtil;

public class AspectRatioUtilTests {
	@Test
	public void standardImageWidthTest() {
		AspectRatioUtil util = new AspectRatioUtil();
		double width = util.getWidth(5184, 3888, 324); // height/16
		
		Assert.assertTrue("New width is wrong: "+width, 432.0 == width);
		double aspectRatioOriginal = 5184d / 3888d;
		double aspectRatioResize = width / 324d;
		
		Assert.assertTrue("Aspect ratio is not correct ("+aspectRatioOriginal+" vs "+aspectRatioResize+")", aspectRatioOriginal == aspectRatioResize);
	}
	
	@Test
	public void standardImageWidthTest2() {
		AspectRatioUtil util = new AspectRatioUtil();
		double width = util.getWidth(5184, 3888, 1944);
		
		Assert.assertTrue("New width is wrong: "+width, 2592.0 == width);
		double aspectRatioOriginal = 5184d / 3888d;
		double aspectRatioResize = width / 1944d;
		
		Assert.assertTrue("Aspect ratio is not correct ("+aspectRatioOriginal+" vs "+aspectRatioResize+")", aspectRatioOriginal == aspectRatioResize);
	}
	
	@Test
	public void standardImageHeightTest() {
		AspectRatioUtil util = new AspectRatioUtil();
		double height = util.getHeight(5184, 3888, 200); // 150.0
		
		Assert.assertTrue("New height is wrong: "+height, 150.0 == height);
		double aspectRatioOriginal = 5184d / 3888d;
		double aspectRatioResize = 200d / height;
		
		Assert.assertTrue("Aspect ratio is not correct ("+aspectRatioOriginal+" vs "+aspectRatioResize+")", aspectRatioOriginal == aspectRatioResize);
	}
	
	@Test
	public void standardImageHeightTest2() {
		AspectRatioUtil util = new AspectRatioUtil();
		double height = util.getHeight(5184, 3888, 2592);
		
		Assert.assertTrue("New height is wrong: "+height, 1944.0 == height);
		double aspectRatioOriginal = 5184d / 3888d;
		double aspectRatioResize = 2592d / height;
		
		Assert.assertTrue("Aspect ratio is not correct ("+aspectRatioOriginal+" vs "+aspectRatioResize+")", aspectRatioOriginal == aspectRatioResize);
	}
}

package io.github.jmif.ui.menu.util;

import java.awt.image.BufferedImage;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

public class SVGUtil extends ImageTranscoder {
	private BufferedImage image = null;

	public BufferedImage createImage(int w, int h) {
		image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		return image;
	}

	public void writeImage(BufferedImage img, TranscoderOutput out) {
	}

	public BufferedImage getImage() {
		return image;
	}
}

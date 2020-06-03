package io.github.jmif.gui.swing.menu.util;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscodingHints;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.util.SVGConstants;

public class SVGLoader {
	public static ImageIcon getImage(Float width, Float height, URL uri) {
		try {
			SVGUtil transcoder = new SVGUtil();
	
			TranscodingHints hints = new TranscodingHints();
			hints.put(ImageTranscoder.KEY_WIDTH, width); // e.g. width=new Float(300)
			hints.put(ImageTranscoder.KEY_HEIGHT, height);// e.g. height=new Float(75)
			hints.put(ImageTranscoder.KEY_DOM_IMPLEMENTATION, SVG12DOMImplementation.getDOMImplementation());
			hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
			hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT_NAMESPACE_URI, SVGConstants.SVG_NAMESPACE_URI);
			hints.put(ImageTranscoder.KEY_DOCUMENT_ELEMENT, SVGConstants.SVG_SVG_TAG);
			hints.put(ImageTranscoder.KEY_XML_PARSER_VALIDATING, false);
	
			transcoder.setTranscodingHints(hints);
	
			TranscoderInput ti = new TranscoderInput(uri.getFile());
			transcoder.transcode(ti, null);
			BufferedImage image = transcoder.getImage();
			return new ImageIcon(image);
		} catch (TranscoderException e) {
			return null;
		}
	}
}

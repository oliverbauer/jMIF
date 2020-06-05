package io.github.jmif.gui.swing.selection.video;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.gui.swing.GraphWrapper;

public class VideoDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(VideoDetailsView.class);
	
	private Box panel;
	private MIFVideo mifVideo;
	
	private JLabel filename = new JLabel();
	private JTextField displayName = new JTextField();
	private JTextField framelengthToDisplay = new JTextField();
	private JTextField overlay = new JTextField();

	private JLabel vCodec = new JLabel();
	private JLabel vBitrate = new JLabel();
	private JLabel vDimension = new JLabel();
	private JLabel vFps = new JLabel();
	private JLabel vAspectRatio = new JLabel();
	private JLabel aCodec = new JLabel();
	private JLabel aBitrate = new JLabel();

	private JLabel labelFile = new JLabel("File");
	private JLabel labelDisplayname = new JLabel("Displayname");
	private JLabel labelFrames = new JLabel("Frames");
	private JLabel labelOVerlay = new JLabel("Overlay");
	private JLabel labelFPS = new JLabel("V.fps");
	private JLabel labelVideoCodec = new JLabel("V.codec");
	private JLabel labelVideoAR = new JLabel("V.aspect ratio");
	private JLabel labelVideoBitrate = new JLabel("V.bitrate");
	private JLabel labelDimension = new JLabel("V.dimension (wxh)");
	private JLabel labelAudioCodec = new JLabel("A.codec");
	private JLabel labelAudioBitrate = new JLabel("A.bitrate");

	public VideoDetailsView(GraphWrapper mifProject) {
		Box box = Box.createVerticalBox();
		
	    wrap(box, labelFile, filename);
	    wrap(box, labelDisplayname, displayName);
	    wrap(box, labelFrames, framelengthToDisplay);
	    wrap(box, labelOVerlay, overlay);

	    wrap(box, labelVideoCodec, vCodec);
	    wrap(box, labelVideoBitrate, vBitrate);
	    wrap(box, labelDimension, vDimension);
	    wrap(box, labelVideoAR, vAspectRatio);
	    wrap(box, labelFPS, vFps);

	    wrap(box, labelAudioCodec, aCodec);
	    wrap(box, labelAudioBitrate, aBitrate);
	    
	    framelengthToDisplay.addActionListener(e -> {
	    	String input = framelengthToDisplay.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		if (i<25) {
	    			throw new IllegalArgumentException("Must be >= 25 frames");
	    		}
	    		if (i>250) {
	    			throw new IllegalArgumentException("Must be <= 250 frames");
	    		}
	    		
	    		mifVideo.setFramelength(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    

	    overlay.addActionListener(e -> {
	    	String input = overlay.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		// TODO Video: Overlay: Check if prev. meltfile is longer:-)
	    		if (i < 0) {
	    			throw new IllegalArgumentException("Overlay must be positive");
	    		}
	    		
	    		mifVideo.setOverlayToPrevious(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    
		box.add(Box.createRigidArea(new Dimension(0, 10)));
		
		panel = Box.createHorizontalBox();
		panel.add(box);
		panel.setBackground(Configuration.bgColor);
		panel.add(Box.createHorizontalGlue());
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2, true));
		}
		
		Dimension dim = new Dimension(5200, 200);
		panel.setPreferredSize(dim);
		panel.setMinimumSize(dim);
		panel.setMaximumSize(dim);
	}
	
	private void wrap(Box box, JComponent c1, JComponent c2) {
	    Box boxFilename = Box.createHorizontalBox();
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    c1.setMinimumSize(new Dimension(140, 20));
	    c1.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(c1);
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFilename.add(c2);
	    boxFilename.add(Box.createHorizontalGlue());
	    
	    box.add(boxFilename);
	}
	
	public Box getBox() {
		return panel;
	}
	
	public void setDetails(MIFVideo mifVideo) {
		this.mifVideo = mifVideo;
		this.filename.setText(mifVideo.getFile());
		this.displayName.setText(mifVideo.getDisplayName());
		this.framelengthToDisplay.setText(String.valueOf(mifVideo.getFramelength()));
		this.vFps.setText(String.valueOf(mifVideo.getFps()));
		this.vCodec.setText(mifVideo.getVideoCodec());
		this.aBitrate.setText(String.valueOf(mifVideo.getAudioBitrate())+" kbps");
		this.aCodec.setText(mifVideo.getAudioCodec());
		this.overlay.setText(String.valueOf(mifVideo.getOverlayToPrevious()));
		this.vAspectRatio.setText(mifVideo.getAr());
		this.vBitrate.setText(String.valueOf(mifVideo.getVideoBitrate())+" kbps");
		this.vDimension.setText(mifVideo.getWidth()+"x"+mifVideo.getHeight());
		this.panel.updateUI();
	}
}

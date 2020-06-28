package io.github.jmif.gui.swing.selection.video;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFVideoWrapper;

public class VideoDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(VideoDetailsView.class);
	
	private Box box;
	private MIFVideoWrapper mifVideo;
	
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

	public VideoDetailsView(GraphWrapper mifProject) {
		Box vBox = Box.createVerticalBox();
		
	    wrap(vBox, new JLabel("File"), filename);
	    wrap(vBox, new JLabel("Displayname"), displayName);
	    wrap(vBox, new JLabel("Frames [ms]"), framelengthToDisplay);
	    wrap(vBox, new JLabel("Overlay [ms]"), overlay);

	    wrap(vBox, new JLabel("V.codec"), vCodec);
	    wrap(vBox, new JLabel("V.bitrate"), vBitrate);
	    wrap(vBox, new JLabel("V.dimension (wxh)"), vDimension);
	    wrap(vBox, new JLabel("V.aspect ratio"), vAspectRatio);
	    wrap(vBox, new JLabel("V.fps"), vFps);

	    wrap(vBox, new JLabel("A.codec"), aCodec);
	    wrap(vBox, new JLabel("A.bitrate"), aBitrate);
	    
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
	    		
	    		mifVideo.setDuration(i);
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
	    
		vBox.add(Box.createRigidArea(new Dimension(0, 10)));
		
		box = Box.createHorizontalBox();
		box.add(vBox);
		box.add(Box.createHorizontalGlue());
		Dimension dim = new Dimension(5200, 200);
		box.setPreferredSize(dim);
		box.setMinimumSize(dim);
		box.setMaximumSize(dim);
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
		return box;
	}
	
	public void setDetails(MIFVideoWrapper mifVideo) {
		this.mifVideo = mifVideo;
		this.filename.setText(mifVideo.getFile().getName());
		this.displayName.setText(mifVideo.getDisplayName());
		this.framelengthToDisplay.setText(String.valueOf(mifVideo.getDuration()));
		this.vFps.setText(String.valueOf(mifVideo.getFps()));
		this.vCodec.setText(mifVideo.getVideoCodec());
		this.aBitrate.setText(String.valueOf(mifVideo.getAudioBitrate())+" kbps");
		this.aCodec.setText(mifVideo.getAudioCodec());
		this.overlay.setText(String.valueOf(mifVideo.getOverlayToPrevious()));
		this.vAspectRatio.setText(mifVideo.getAr());
		this.vBitrate.setText(String.valueOf(mifVideo.getVideoBitrate())+" kbps");
		this.vDimension.setText(mifVideo.getWidth()+"x"+mifVideo.getHeight());
		this.box.updateUI();
	}
}

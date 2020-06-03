package io.github.jmif.gui.swing.selection.video;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.data.GraphWrapper;
import io.github.jmif.entities.MIFFile;

public class VideoDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(VideoDetailsView.class);
	
	private Box panel;
	private MIFFile meltFile;
	
	private JLabel filename = new JLabel();
	private JTextField displayName = new JTextField();
	private JTextField framelengthToDisplay = new JTextField();
	private JTextField overlay = new JTextField();
	private JLabel dimensionLabel = new JLabel();

	private JLabel labelFile = new JLabel("File");
	private JLabel labelDisplayname = new JLabel("Displayname");
	private JLabel labelFrames = new JLabel("Frames");
	private JLabel labelDimension = new JLabel("Dimension (wxh)");
	private JLabel labelOVerlay = new JLabel("Overlay");

	// TODO Video: Show FPS of originalvideo
	// TODO Video: Show informations about audiotrack
	public VideoDetailsView(GraphWrapper mifProject) {
		Box box = Box.createVerticalBox();
		
	    Box boxFilename = Box.createHorizontalBox();
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    labelFile.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(labelFile);
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFilename.add(filename);
	    boxFilename.add(Box.createHorizontalGlue());
	    box.add(boxFilename);

	    Box boxDisplayname = Box.createHorizontalBox();
	    boxDisplayname.add(Box.createRigidArea(new Dimension(10, 0)));
	    labelDisplayname.setPreferredSize(new Dimension(140, 20));
	    boxDisplayname.add(labelDisplayname);
	    boxDisplayname.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxDisplayname.add(displayName);
	    boxDisplayname.add(Box.createHorizontalGlue());
	    box.add(boxDisplayname);
		
	    Box boxFramelength = Box.createHorizontalBox();
	    boxFramelength.add(Box.createRigidArea(new Dimension(10, 0)));
	    labelFrames.setPreferredSize(new Dimension(140, 20));
	    boxFramelength.add(labelFrames);
	    boxFramelength.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFramelength.add(framelengthToDisplay);
	    boxFramelength.add(Box.createHorizontalGlue());
	    box.add(boxFramelength);
	    framelengthToDisplay.addActionListener(e -> {
	    	String input = framelengthToDisplay.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		if (i<25) {
	    			throw new IllegalArgumentException("Muss >= 25 Frames sein");
	    		}
	    		if (i>250) {
	    			throw new IllegalArgumentException("Muss <= 250 Frames sein");
	    		}
	    		
	    		meltFile.setFramelength(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t.getMessage());
	    	}
	    });
	    
	    Box boxDimension = Box.createHorizontalBox();
	    boxDimension.add(Box.createRigidArea(new Dimension(10, 0)));
	    labelDimension.setPreferredSize(new Dimension(140, 20));
	    boxDimension.add(labelDimension);
	    boxDimension.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxDimension.add(dimensionLabel);
	    boxDimension.add(Box.createHorizontalGlue());
	    box.add(boxDimension);
	    
	    Box boxOverlay = Box.createHorizontalBox();
	    boxOverlay.add(Box.createRigidArea(new Dimension(10, 0)));
	    labelOVerlay.setPreferredSize(new Dimension(140, 20));
	    boxOverlay.add(labelOVerlay);
	    boxOverlay.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxOverlay.add(overlay);
	    boxOverlay.add(Box.createHorizontalGlue());
	    box.add(boxOverlay);
	    overlay.addActionListener(e -> {
	    	String input = overlay.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		// TODO Video: Overlay: Check if prev. meltfile is longer:-)
	    		if (i < 0) {
	    			throw new IllegalArgumentException("Overlay must be positive");
	    		}
	    		
	    		meltFile.setOverlayToPrevious(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    
	    box.add(Box.createRigidArea(new Dimension(0,10)));
		Box boxUpdate = Box.createHorizontalBox();
		boxUpdate.add(Box.createHorizontalGlue());
		box.add(boxUpdate);
		
		box.add(Box.createRigidArea(new Dimension(0,10)));
		
		panel = Box.createHorizontalBox();
		panel.add(box);
		panel.setBackground(Configuration.bgColor);
		panel.add(Box.createHorizontalGlue());
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2, true));
		}
		
		Dimension dim = new Dimension(5200, 100);
		panel.setPreferredSize(dim);
		panel.setMinimumSize(dim);
		panel.setMaximumSize(dim);
		
	}
	public Box getBox() {
		return panel;
	}
	
	public void setDetails(MIFFile meltFile) {
		this.meltFile = meltFile;
		filename.setText(meltFile.getFile());
		displayName.setText(meltFile.getDisplayName());
		framelengthToDisplay.setText(String.valueOf(meltFile.getFramelength()));
		overlay.setText(String.valueOf(meltFile.getOverlayToPrevious()));
		dimensionLabel.setText(meltFile.getWidth()+"x"+meltFile.getHeight());
		panel.updateUI();
	}
}

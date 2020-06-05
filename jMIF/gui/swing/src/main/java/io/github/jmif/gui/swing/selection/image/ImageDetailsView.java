package io.github.jmif.gui.swing.selection.image;

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
import io.github.jmif.entities.MIFFile;
import io.github.jmif.gui.swing.GraphWrapper;

public class ImageDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(ImageDetailsView.class);
	
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

	private MIFFile meltFile;
	private Box panel;
	
	public ImageDetailsView(GraphWrapper mifProject) {
		Box box = Box.createVerticalBox();
		
	    wrap(box, labelFile, filename);
	    wrap(box, labelDisplayname, displayName);
	    wrap(box, labelFrames, framelengthToDisplay);
	    wrap(box, labelDimension, dimensionLabel);
	    wrap(box, labelOVerlay, overlay);
	    
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
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    

	    overlay.addActionListener(e -> {
	    	String input = overlay.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		// TODO Image: Overlay: Check if prev. meltfile is longer:-)

	    		if (i<0) {
	    			throw new IllegalArgumentException("Overlay is not allowed to be negative");
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
		
		clearDetails();
		
		Dimension dim = new Dimension(5200, 100);
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
	    c2.setPreferredSize(new Dimension(5000, 20));
	    c2.setMaximumSize(new Dimension(5000, 20));
	    boxFilename.add(Box.createHorizontalGlue());
	    box.add(boxFilename);
	}
	
	public Box getBox() {
		return panel;
	}
	
	public void clearDetails() {
		this.meltFile = null;
		filename.setVisible(false);
		displayName.setVisible(false);
		framelengthToDisplay.setVisible(false);
		overlay.setVisible(false);
		dimensionLabel.setVisible(false);
		
		labelFile.setVisible(false);
		labelDisplayname.setVisible(false);
		labelFrames.setVisible(false);
		labelDimension.setVisible(false);
		labelOVerlay.setVisible(false);
		
		panel.updateUI();
	}

	public void setDetails(MIFFile meltFile) {
		this.meltFile = meltFile;
		filename.setText(meltFile.getFile());
		displayName.setText(meltFile.getDisplayName());
		framelengthToDisplay.setText(String.valueOf(meltFile.getFramelength()));
		overlay.setText(String.valueOf(meltFile.getOverlayToPrevious()));
		dimensionLabel.setText(meltFile.getWidth()+"x"+meltFile.getHeight());
		
		filename.setVisible(true);
		displayName.setVisible(true);
		framelengthToDisplay.setVisible(true);
		overlay.setVisible(true);
		dimensionLabel.setVisible(true);
		
		labelFile.setVisible(true);
		labelDisplayname.setVisible(true);
		labelFrames.setVisible(true);
		labelDimension.setVisible(true);
		labelOVerlay.setVisible(true);
		
		panel.updateUI();
	}
}

package io.github.jmif.gui.swing.selection.image;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFFileWrapper;

public class ImageDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(ImageDetailsView.class);
	
	private JLabel filename = new JLabel();
	private JTextField displayName = new JTextField();
	private JTextField duration = new JTextField();
	private JTextField overlay = new JTextField();
	private JLabel dimensionLabel = new JLabel();

	private JLabel labelFile = new JLabel("File");
	private JLabel labelDisplayname = new JLabel("Displayname");
	private JLabel labelDuration = new JLabel("Duration [ms]");
	private JLabel labelDimension = new JLabel("Dimension (wxh)");
	private JLabel labelOVerlay = new JLabel("Overlay [ms]");

	private MIFFileWrapper<?> meltFile;
	private Box box;
	
	public ImageDetailsView(GraphWrapper mifProject) {
		box = Box.createVerticalBox();
		
	    wrap(box, labelFile, filename);
	    wrap(box, labelDisplayname, displayName);
	    wrap(box, labelDuration, duration);
	    wrap(box, labelDimension, dimensionLabel);
	    wrap(box, labelOVerlay, overlay);
	    
	    duration.addActionListener(e -> {
	    	String input = duration.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
	    		if (i<25) {
	    			throw new IllegalArgumentException("Muss >= 25 Frames sein");
	    		}
	    		if (i>250) {
	    			throw new IllegalArgumentException("Muss <= 250 Frames sein");
	    		}
	    		
	    		meltFile.setDuration(i);
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
	}

	private void wrap(Box box, JComponent c1, JComponent c2) {
	    Box boxFilename = Box.createHorizontalBox();
	    c1.setMinimumSize(new Dimension(140, 20));
	    c1.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(c1);
	    boxFilename.add(c2);
	    boxFilename.add(Box.createHorizontalGlue());
	    box.add(boxFilename);
	}
	
	public Box getBox() {
		return box;
	}
	
	public void clearDetails() {
		this.meltFile = null;
		filename.setVisible(false);
		displayName.setVisible(false);
		duration.setVisible(false);
		overlay.setVisible(false);
		dimensionLabel.setVisible(false);
		
		labelFile.setVisible(false);
		labelDisplayname.setVisible(false);
		labelDuration.setVisible(false);
		labelDimension.setVisible(false);
		labelOVerlay.setVisible(false);
		
		box.updateUI();
	}

	public void setDetails(MIFFileWrapper<?> meltFile) {
		this.meltFile = meltFile;
		filename.setText(meltFile.getFile().getName());
		displayName.setText(meltFile.getDisplayName());
		duration.setText(String.valueOf(meltFile.getDuration()));
		overlay.setText(String.valueOf(meltFile.getOverlayToPrevious()));
		dimensionLabel.setText(meltFile.getWidth()+"x"+meltFile.getHeight());
		
		filename.setVisible(true);
		displayName.setVisible(true);
		duration.setVisible(true);
		overlay.setVisible(true);
		dimensionLabel.setVisible(true);
		
		labelFile.setVisible(true);
		labelDisplayname.setVisible(true);
		labelDuration.setVisible(true);
		labelDimension.setVisible(true);
		labelOVerlay.setVisible(true);
		
		box.updateUI();
	}
}

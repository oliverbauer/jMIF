package io.github.jmif.gui.swing.selection.image;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.gui.swing.GraphWrapper;

public class ImageDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(ImageDetailsView.class);
	
	private JLabel filename = new JLabel();
	private JTextField displayName = new JTextField();
	private JTextField duration = new JTextField();
	private JTextField overlay = new JTextField();
	private JLabel dimensionLabel = new JLabel();

	private MIFFile meltFile;
	private Box box;
	
	public ImageDetailsView(GraphWrapper mifProject) {
		box = Box.createVerticalBox();
		
	    wrap(box, new JLabel("File"), filename);
	    wrap(box, new JLabel("Displayname"), displayName);
	    wrap(box, new JLabel("Duration [ms]"), duration);
	    wrap(box, new JLabel("Dimension (wxh)"), dimensionLabel);
	    wrap(box, new JLabel("Overlay [ms]"), overlay);
	    
	    duration.addActionListener(e -> {
	    	var input = duration.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		
	    		if (i<1000) {
	    			throw new IllegalArgumentException("Must be at least 1 sec = 1000 ms");
	    		}
	    		if (i>15000) {
	    			throw new IllegalArgumentException("Not allowed to be more that 15 sec = 15000 ms");
	    		}
	    		
	    		meltFile.setDuration(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    

	    overlay.addActionListener(e -> {
	    	var input = overlay.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		
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
	    var boxFilename = Box.createHorizontalBox();
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
	
	public void setDetails(MIFFile meltFile) {
		this.meltFile = meltFile;
		filename.setText(meltFile.getFile().getName());
		displayName.setText(meltFile.getDisplayName());
		duration.setText(String.valueOf(meltFile.getDuration()));
		overlay.setText(String.valueOf(meltFile.getOverlayToPrevious()));
		dimensionLabel.setText(meltFile.getWidth()+"x"+meltFile.getHeight());
		
		box.updateUI();
	}
}

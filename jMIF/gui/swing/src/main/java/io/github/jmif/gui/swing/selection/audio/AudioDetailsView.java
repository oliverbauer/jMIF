package io.github.jmif.gui.swing.selection.audio;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFAudioFileWrapper;

public class AudioDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(AudioDetailsView.class);
	
	private MIFAudioFileWrapper audioFile;

	private Box box;
	
	private JLabel file = new JLabel();
	private JLabel length = new JLabel();
	private JLabel bitrate = new JLabel();
	private JTextField encodeStart = new JTextField();
	private JTextField encodeEnd = new JTextField();
	private JLabel encodeLength = new JLabel();
	private JCheckBox normalize = new JCheckBox();
	private JTextField fadeIn = new JTextField();
	private JTextField fadeOut = new JTextField();

	public AudioDetailsView(GraphWrapper mifProject) {
		var vBox = Box.createVerticalBox();
		
	    wrap(vBox, new JLabel("File"), file);
	    wrap(vBox, new JLabel("Length [ms]"), length);
	    wrap(vBox, new JLabel("Bitrate"), bitrate);
	    wrap(vBox, new JLabel("Encode from [ms]"), encodeStart);
	    wrap(vBox, new JLabel("Encode to [ms]"), encodeEnd);
	    wrap(vBox, new JLabel("Encode length [ms]"), encodeLength);
	    wrap(vBox, new JLabel("Normalize"), normalize);
	    wrap(vBox, new JLabel("FadeIn"), fadeIn);
	    wrap(vBox, new JLabel("FadeOut"), fadeOut);
	    
	    normalize.addActionListener(e -> audioFile.setNormalize(normalize.isSelected()));
	    
	    encodeStart.addActionListener(e -> {
	    	var input = encodeEnd.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		if (i<0) {
	    			throw new IllegalArgumentException("Darf nicht kleiner 0 sein");
	    		}
	    		if (i >= audioFile.getLengthOfInput()-1) {
	    			throw new IllegalArgumentException("Darf nicht kleiner der Länge des Audiofiles sein");
	    		}
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    
	    encodeEnd.addActionListener(e -> {
	    	var input = encodeEnd.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		
	    		if (i<1) {
	    			throw new IllegalArgumentException("Muss mind. 1 sekunde sein");
	    		}
	    		if (i>audioFile.getLengthOfInput()) {
	    			throw new IllegalArgumentException("Darf nicht länger als input sein");
	    		}
	    		
	    		audioFile.setEncodeEnde(i);
	    		mifProject.redrawGraph();
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    fadeIn.addActionListener(e -> {
	    	var input = fadeIn.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		if (i<0) {
	    			throw new IllegalArgumentException("Not allowed to be negative");
	    		}
	    		if (i>audioFile.getLengthOfInput()) {
	    			throw new IllegalArgumentException("Not allowed to be longer than the input file");
	    		}
	    		audioFile.setFadeIn(i);
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    fadeOut.addActionListener(e -> {
	    	var input = fadeOut.getText();
	    	try {
	    		var i = Integer.parseInt(input);
	    		if (i<0) {
	    			throw new IllegalArgumentException("Not allowed to be negative");
	    		}
	    		if (i>audioFile.getLengthOfInput()) {
	    			throw new IllegalArgumentException("Not allowed to be longer than the input file");
	    		}
	    		audioFile.setFadeOut(i);
	    	} catch (Exception t) {
	    		logger.warn("Not allowed: ", t);
	    	}
	    });
	    
	    vBox.add(Box.createRigidArea(new Dimension(0,10)));
		vBox.add(Box.createHorizontalGlue());
		
		box = Box.createHorizontalBox();
		box.add(vBox);
		box.add(Box.createHorizontalGlue());
	}
	
	private void wrap(Box box, JComponent c1, JComponent c2) {
	    var boxFilename = Box.createHorizontalBox();
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

	public void setDetails(MIFAudioFileWrapper audioFile) {
		this.audioFile = audioFile;
		
		file.setText(audioFile.getAudiofile());
		length.setText(String.valueOf(audioFile.getLengthOfInput()));
		bitrate.setText(String.valueOf(audioFile.getBitrate()/1000)+" kb/s");
		encodeStart.setText(String.valueOf(audioFile.getEncodeStart()));
		encodeEnd.setText(String.valueOf(audioFile.getEncodeEnde()));
		encodeLength.setText("TODO compute end-start");
		fadeIn.setText(String.valueOf(audioFile.getFadeIn()));
		fadeOut.setText(String.valueOf(audioFile.getFadeOut()));
		normalize.setSelected(audioFile.isNormalize());
		
		box.updateUI();
	}
	
	public Box getBox() {
		return box;
	}
}

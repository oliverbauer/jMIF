package io.github.jmif.ui.selection.audio;

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
import io.github.jmif.data.GraphWrapper;
import io.github.jmif.entities.MIFAudioFile;

public class AudioDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(AudioDetailsView.class);
	
	private MIFAudioFile audioFile;

	private Box panel;
	
	private JLabel file = new JLabel();
	private JLabel length = new JLabel();
	private JLabel bitrate = new JLabel();
	private JTextField encodeStart = new JTextField();
	private JTextField encodeEnd = new JTextField();
	private JLabel encodeLength = new JLabel();
	private JTextField fadeIn = new JTextField();
	private JTextField fadeOut = new JTextField();

	private JLabel labelFile = new JLabel("File");
	private JLabel labelLength = new JLabel("Length");
	private JLabel labelBitrate = new JLabel("Bitrate");
	private JLabel labelEncodeStart = new JLabel("Encode from");
	private JLabel labelEncodeEnd = new JLabel("Encode to");
	private JLabel labelEncodeLength = new JLabel("Encode length");
	private JLabel labelFadeIn = new JLabel("FadeIn");
	private JLabel labelFadeOut = new JLabel("FadeOut");

	public AudioDetailsView(GraphWrapper mifProject) {
		Box box = Box.createVerticalBox();
		
	    wrap(box, labelFile, file);
	    wrap(box, labelLength, length);
	    wrap(box, labelBitrate, bitrate);
	    wrap(box, labelEncodeStart, encodeStart);
	    wrap(box, labelEncodeEnd, encodeEnd);
	    wrap(box, labelEncodeLength, encodeLength);   
	    wrap(box, labelFadeIn, fadeIn);
	    wrap(box, labelFadeOut, fadeOut);
	    
	    encodeStart.addActionListener(e -> {
	    	String input = encodeEnd.getText();
	    	try {
	    		int i = Integer.parseInt(input);
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
	    	String input = encodeEnd.getText();
	    	try {
	    		int i = Integer.parseInt(input);
	    		
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
	    
	    box.add(Box.createRigidArea(new Dimension(0,10)));
		box.add(Box.createHorizontalGlue());
		
		panel = Box.createHorizontalBox();
		panel.add(box);
		panel.setBackground(Configuration.bgColor);
		panel.add(Box.createHorizontalGlue());
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2, true));
		}
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
	

	public void setDetails(MIFAudioFile audioFile) {
		this.audioFile = audioFile;
		
		file.setText(audioFile.getAudiofile());
		length.setText(String.valueOf(audioFile.getLengthOfInput())+"sec.");
		bitrate.setText(String.valueOf(audioFile.getBitrate()/1000)+" kb/s");
		encodeStart.setText(String.valueOf(audioFile.getEncodeStart()));
		encodeEnd.setText(String.valueOf(audioFile.getEncodeEnde()));
		encodeLength.setText("TODO compute end-start");
		fadeIn.setText(String.valueOf(audioFile.getFadeIn()));
		fadeOut.setText(String.valueOf(audioFile.getFadeOut()));
		
		panel.updateUI();
	}
	
	public Box getBox() {
		return panel;
	}
}

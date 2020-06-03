package io.github.jmif.gui.swing.selection.audio;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.data.GraphWrapper;
import io.github.jmif.entities.MIFAudioFile;

public class AudioView {
	private static final Logger logger = LoggerFactory.getLogger(AudioView.class);
	
	private Box box;
	
	public AudioView() {
		box = Box.createHorizontalBox();
		box.add(new JLabel("TODO...AudioPanel"));
		if (Configuration.useBorders) {
			box.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2, true));
		}
	}
	
	public Box getBox() {
		return this.box;
	}

	public void setMIFAudioFile(MIFAudioFile audioFile, GraphWrapper project) {
		logger.info("TODO process {} {} - not yet implemented", audioFile, project);
	}
}

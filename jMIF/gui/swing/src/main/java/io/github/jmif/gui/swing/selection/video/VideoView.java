package io.github.jmif.gui.swing.selection.video;

import java.awt.Color;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VideoView {
	private static final Logger logger = LoggerFactory.getLogger(VideoView.class);
	private JLabel[] imgVideoLabel;
	private JPanel videoPanel = new JPanel();

	public VideoView() {
		JScrollPane videoScrollPane = new JScrollPane(videoPanel);
        
		videoPanel.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		
	    imgVideoLabel = new JLabel[10];
	    for (int i=0; i<=9; i++) {
	    	imgVideoLabel[i] = new JLabel();
	    	videoPanel.add(imgVideoLabel[i]);
	    }
	    videoScrollPane.add(videoPanel);
	}
	
	public void clearIcons() {
		for (int i=0; i<=9; i++) {
			imgVideoLabel[i].setIcon(null);
			imgVideoLabel[i].setVisible(false);
		}
		videoPanel.updateUI();
	}
	
	public void setIcons(String p[]) {
		for (int i=0; i<=9; i++) {
			if (!new File(p[i]).exists()) {
				logger.warn("File {} does not exists", p[i]);
				return;
			}
		}
		
		for (int i=0; i<=9; i++) {
			imgVideoLabel[i].setIcon(new ImageIcon(p[i]));
			imgVideoLabel[i].setVisible(true);
		}
		videoPanel.updateUI();
	}
	
	public JPanel getPanel() {
		return videoPanel;
	}
}

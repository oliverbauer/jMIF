package io.github.jmif.gui.swing.selection.frame;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.gui.swing.listener.SingleFrameCreatedListener;

public class FrameView implements SingleFrameCreatedListener {
	private static final Logger logger = LoggerFactory.getLogger(FrameView.class);
	private Box box;
	private JLabel picture;
	
	public FrameView() {
		box = Box.createVerticalBox();
		picture = new JLabel("Please drag and drop the slider");
		var scrollPane = new JScrollPane(picture);
		box.add(scrollPane);
	}
	
	public Box getBox() {
		return box;
	}

	// TODO Use 'initial' image for 'initial' position (only works after first drag and drop)
	@Override
	public void created(String file) {
		logger.info("Updating icon to {}", file);
		picture.setIcon(new ImageIcon(file));
	}
}

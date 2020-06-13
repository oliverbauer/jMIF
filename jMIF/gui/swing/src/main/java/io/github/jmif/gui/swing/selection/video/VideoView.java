package io.github.jmif.gui.swing.selection.video;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.Collection;
import java.util.Objects;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class VideoView {
	private JLabel[] imgVideoLabel;
	private JPanel videoPanel;

	public VideoView() {
		videoPanel = new JPanel(new GridLayout(1, 10));
		//		videoPanel.setMaximumSize(new Dimension(2000, 200)); // width does not matter

		JScrollPane videoScrollPane = new JScrollPane(videoPanel);

		videoPanel.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

		imgVideoLabel = new JLabel[10];
		for (int i=0; i<=9; i++) {
			imgVideoLabel[i] = new JLabel();
			videoPanel.add(imgVideoLabel[i]);
		}
		videoScrollPane.add(videoPanel);
		//	    videoScrollPane.setMaximumSize(new Dimension(2000, 200)); // width does not matter
	}

	public void setIcons(Collection<Image> p) {
		if (!p.isEmpty()) {
			var it = p.iterator();
			for (int i=0; it.hasNext() && i<=9; i++) {
				final var next = it.next();
				if (Objects.nonNull(next)) {
					imgVideoLabel[i].setIcon(new ImageIcon(next));
					imgVideoLabel[i].setVisible(true);
					imgVideoLabel[i].setMaximumSize(new Dimension(200, 200));
				}
			}
			videoPanel.updateUI();
		}
	}

	public JPanel getPanel() {
		return videoPanel;
	}
}

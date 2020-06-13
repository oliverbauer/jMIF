package io.github.jmif.gui.swing.selection.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.MIFException;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFImage.ImageResizeStyle;
import io.github.jmif.gui.swing.GraphWrapper;

public class ImageView {
	private static final Logger logger = LoggerFactory.getLogger(ImageView.class);

	private final GraphWrapper graphWrapper;

	private JLabel[] imgPicture;

	private JLabel resizeStyleLabel;
	private JComboBox<ImageResizeStyle> resizeStyle;
	private JLabel resizeStyleDetailsLabel;
	private JComboBox<String> resizeStyleDetails;
	private JButton manualExtraction = new JButton("manual");

	private JPanel panel;

	private mxCell selectedCell;
	private MIFImage selectedMeltFile;

	public ImageView(final GraphWrapper graphWrapper) throws MIFException {

		this.graphWrapper = graphWrapper;

		// Left part is the original image
		Box leftBox = Box.createVerticalBox();
		imgPicture = new JLabel[2];
		imgPicture[0] = new JLabel();
		imgPicture[0].setBorder(BorderFactory.createLineBorder(Color.black));
		imgPicture[0].setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		imgPicture[0].setForeground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		leftBox.add(imgPicture[0]);

		// Right side (center)
		Box rightBox = Box.createVerticalBox();
		rightBox.add(getResizeStyleBox());
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getPreviewImage());
		rightBox.add(Box.createVerticalGlue());

		Box box = Box.createVerticalBox();
		box.add(Box.createVerticalStrut(10));

		JPanel panel2 = new JPanel(new BorderLayout());
		panel2.add(leftBox, BorderLayout.WEST);
		Box h = Box.createHorizontalBox();
		h.add(Box.createHorizontalStrut(10));
		h.add(rightBox);
		panel2.add(h, BorderLayout.CENTER);

		box.add(panel2);
		panel = new JPanel(new BorderLayout());
		panel.add(box, BorderLayout.CENTER);
	}

	public JPanel getJPanel() {
		return panel;
	}

	private Box getPreviewImage() {
		Box b = Box.createHorizontalBox();
		imgPicture[1] = new JLabel();
		imgPicture[1].setBorder(BorderFactory.createLineBorder(Color.black));
		imgPicture[1].setBackground(Color.BLACK);
		imgPicture[1].setForeground(Color.BLACK);
		b.add(imgPicture[1]);
		b.add(Box.createHorizontalGlue());

		return b;
	}

	private Box getResizeStyleBox() {
		Box dropDownBoxesBox = Box.createHorizontalBox();
		resizeStyleLabel = new JLabel("Resizestyle: ");
		dropDownBoxesBox.add(resizeStyleLabel);
		resizeStyle = new JComboBox<>(ImageResizeStyle.values());
		resizeStyle.addItemListener(getItemListener());
		resizeStyle.setSelectedItem(ImageResizeStyle.CROP);
		resizeStyle.setPreferredSize(new Dimension(80, 25));
		resizeStyle.setMaximumSize(new Dimension(80, 25));
		dropDownBoxesBox.add(resizeStyle);
		dropDownBoxesBox.add(Box.createHorizontalStrut(10));
		resizeStyleDetailsLabel = new JLabel("Crop from where: ");
		dropDownBoxesBox.add(resizeStyleDetailsLabel);
		String[] detailConfig = Arrays.asList("Half/Half", "Top", "Bottom").toArray(new String[3]);
		resizeStyleDetails = new JComboBox<>(detailConfig);
		resizeStyleDetails.setPreferredSize(new Dimension(100, 25));
		resizeStyleDetails.setMaximumSize(new Dimension(100, 25));
		dropDownBoxesBox.add(resizeStyleDetails);
		dropDownBoxesBox.add(Box.createHorizontalStrut(10));
		manualExtraction.addActionListener(e -> {
			ManualSize manualSize = new ManualSize();
			manualSize.showFrame(graphWrapper, (MIFImage)selectedMeltFile, this);
		});
		dropDownBoxesBox.add(manualExtraction);

		dropDownBoxesBox.add(Box.createHorizontalGlue()); // Alignment of content starts left
		if (Configuration.useBorders) {
			dropDownBoxesBox.setBorder(BorderFactory.createLineBorder(Color.red));
		}

		return dropDownBoxesBox;
	}

	public void refreshFromManualSize() {
		update(selectedCell, selectedMeltFile);
	}

	public void update(mxCell cell, MIFImage meltFile) {
		this.selectedMeltFile = meltFile;
		this.selectedCell = cell;

		ImageResizeStyle selectedItem = meltFile.getStyle();
		resizeStyle.setSelectedItem(selectedItem);
		switch (selectedItem) {
		case CROP:
			final var previewCrop = meltFile.getPreviewCrop();
			if (Objects.nonNull(previewCrop)) {
				setSelectedPicture(previewCrop);
			}
			break;
		case FILL:
			final var previewFillWColor = meltFile.getPreviewFillWColor();
			if (Objects.nonNull(previewFillWColor)) {
				setSelectedPicture(previewFillWColor);
			}
			break;
		case HARD:
			final var previewHardResize = meltFile.getPreviewHardResize();
			if (Objects.nonNull(previewHardResize)) {
				setSelectedPicture(previewHardResize);
			}
			break;
		case MANUAL:
			final var previewManual = meltFile.getPreviewManual();
			if (Objects.nonNull(previewManual)) {
				setSelectedPicture(previewManual);
			}
			break;
		}
	}

	private ItemListener getItemListener() {
		return e -> {
			if (e.getStateChange() == ItemEvent.SELECTED && selectedMeltFile != null) {
				ImageResizeStyle item = (ImageResizeStyle) e.getItem();
				((MIFImage) selectedMeltFile).setStyle(item);

				logger.info("Switching over '{}'", item);
				var mifImage = MIFImage.class.cast(selectedMeltFile);
				final var previewCrop = mifImage.getPreviewCrop();
				switch (item) {
				case HARD:
					final var previewHardResize = mifImage.getPreviewHardResize();
					if (Objects.nonNull(previewHardResize)) {
						imgPicture[1].setIcon(new ImageIcon(previewHardResize));
						resizeStyleDetails.setVisible(false);
						resizeStyleDetailsLabel.setVisible(false);
					}
					break;
				case FILL:
					final var previewFillWColor = mifImage.getPreviewFillWColor();
					if (Objects.nonNull(previewFillWColor)) {
						imgPicture[1].setIcon(new ImageIcon(previewFillWColor));
						resizeStyleDetails.setVisible(true);
						resizeStyleDetailsLabel.setVisible(true);
						resizeStyleDetailsLabel.setText("Fill color:");
					}
					break;
				case CROP:
					if (Objects.nonNull(previewCrop)) {
						imgPicture[1].setIcon(new ImageIcon(previewCrop));
						resizeStyleDetails.setVisible(true);
						resizeStyleDetailsLabel.setVisible(true);
						resizeStyleDetailsLabel.setText("Crop from where: ");
					}
					break;
				case MANUAL:
					final var previewManual = mifImage.getPreviewManual();
					if (Objects.nonNull(previewManual)) {
						imgPicture[1].setIcon(new ImageIcon(previewManual));
						resizeStyleDetails.setVisible(false);
						resizeStyleDetailsLabel.setVisible(false);
					}
					break;
				default:
					logger.error("Unknown style '{}' for image. Using 'CROP", item);
					if (Objects.nonNull(previewCrop)) {
						imgPicture[1].setIcon(new ImageIcon(previewCrop));
						resizeStyleDetails.setVisible(true);
						resizeStyleDetailsLabel.setVisible(true);
						resizeStyleDetailsLabel.setText("Crop from where: ");
					}
				}

				panel.updateUI();
			}
		};
	}

	public void setPreviewPicture(Image imagePreview) {
		imgPicture[0].setVisible(true);
		imgPicture[1].setVisible(true);
		manualExtraction.setVisible(true);

		imgPicture[0].setIcon(new ImageIcon(imagePreview));

		resizeStyleLabel.setVisible(true);
		resizeStyle.setVisible(true);
		resizeStyleDetailsLabel.setVisible(true);
		resizeStyleDetails.setVisible(true);

		panel.updateUI();
	}

	public void setSelectedPicture(Image previewCrop) {
		imgPicture[1].setIcon(new ImageIcon(previewCrop));

		panel.updateUI();
	}
}

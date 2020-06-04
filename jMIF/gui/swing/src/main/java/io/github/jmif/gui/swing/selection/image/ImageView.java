package io.github.jmif.gui.swing.selection.image;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.JMIFException;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;

public class ImageView {
	private static final Logger logger = LoggerFactory.getLogger(ImageView.class);

	private JLabel[] imgPicture;

	private JLabel resizeStyleLabel;
	private JComboBox<String> resizeStyle;
	private JLabel resizeStyleDetailsLabel;
	private JComboBox<String> resizeStyleDetails;
	private JButton manualExtraction = new JButton("manual");

	private Box box;

	public ImageView() {
		box = Box.createHorizontalBox();

		// Left part is the original image

		box.add(Box.createHorizontalStrut(10));

		Box pic1Box = Box.createVerticalBox();
		imgPicture = new JLabel[2];
		imgPicture[0] = new JLabel();
		imgPicture[0].setBorder(BorderFactory.createLineBorder(Color.black));
		imgPicture[0].setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		imgPicture[0].setForeground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		pic1Box.add(imgPicture[0]);
		pic1Box.add(Box.createVerticalGlue());

		box.add(pic1Box);
		box.add(Box.createHorizontalStrut(10));

		// Right side

		Box previewBox = Box.createVerticalBox();
		previewBox.add(Box.createVerticalStrut(5));
		previewBox.add(dropdownBoxes());
		previewBox.add(Box.createVerticalStrut(5));
		previewBox.add(getImage2());
		previewBox.add(Box.createVerticalGlue());
		if (Configuration.useBorders) {
			previewBox.setBorder(BorderFactory.createLineBorder(Color.black));
		}

		imgPicture[0].setVisible(false);
		imgPicture[1].setVisible(false);
		manualExtraction.setVisible(false);

		Box dropBoxPlusPreview = Box.createVerticalBox();
		dropBoxPlusPreview.add(previewBox);
		dropBoxPlusPreview.add(Box.createVerticalGlue());
		box.add(dropBoxPlusPreview);

		box.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));

		resizeStyleLabel.setVisible(false);
		resizeStyle.setVisible(false);
		resizeStyleDetailsLabel.setVisible(false);
		resizeStyleDetails.setVisible(false);
	}

	public Box getBox() {
		return box;
	}

	private Box getImage2() {
		Box b = Box.createHorizontalBox();
		imgPicture[1] = new JLabel();
		imgPicture[1].setBorder(BorderFactory.createLineBorder(Color.black));
		imgPicture[1].setBackground(Color.BLACK);
		imgPicture[1].setForeground(Color.BLACK);
		b.add(imgPicture[1]);
		b.add(Box.createHorizontalGlue());

		return b;
	}

	private Box dropdownBoxes() {
		Box dropDownBoxesBox = Box.createHorizontalBox();
		dropDownBoxesBox.add(Box.createHorizontalStrut(10));
		resizeStyleLabel = new JLabel("Resizestyle: ");
		dropDownBoxesBox.add(resizeStyleLabel);
		String[] styleItems = Arrays.asList("HARD", "FILL", "CROP", "MANUAL").toArray(new String[4]);
		resizeStyle = new JComboBox<>(styleItems);
		resizeStyle.addItemListener(getItemListener());
		resizeStyle.setSelectedItem("CROP");
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
			manualSize.showFrame((MIFImage)selectedMeltFile, resizeStyle);
		});
		dropDownBoxesBox.add(manualExtraction);

		dropDownBoxesBox.add(Box.createHorizontalGlue());
		if (Configuration.useBorders) {
			dropDownBoxesBox.setBorder(BorderFactory.createLineBorder(Color.red));
		}
		Dimension dim = new Dimension(2500, 50);
		dropDownBoxesBox.setMinimumSize(dim);
		dropDownBoxesBox.setPreferredSize(dim);
		dropDownBoxesBox.setMaximumSize(dim);
		
		// TODO Image: Use Filter JCombobox with at least NONE,GRAYSCALE as an example: https://github.com/oliverbauer/jMIF/issues/3
		
		return dropDownBoxesBox;
	}

	@SuppressWarnings("unused")
	private mxCell selectedCell;
	private MIFFile selectedMeltFile;

	public void update(mxCell cell, MIFFile meltFile) {
		this.selectedMeltFile = meltFile;
		this.selectedCell = cell;

		String selectedItem = ((MIFImage) meltFile).getStyle();
		resizeStyle.setSelectedItem(selectedItem);
		// TODO Image: Enum for style
		if (selectedItem.equals("CROP")) {
			setSelectedPicture(((MIFImage) meltFile).getPreviewCrop());
		} else if (selectedItem.contentEquals("FILL")) {
			setSelectedPicture(((MIFImage) meltFile).getPreviewFillWColor());
		} else if (selectedItem.equals("HARD")) {
			setSelectedPicture(((MIFImage) meltFile).getPreviewHardResize());
		} else if (selectedItem.equals("MANUAL")) {
			setSelectedPicture(((MIFImage) meltFile).getPreviewManual());
		}
	}

	private ItemListener getItemListener() {
		return e -> {
			if (e.getStateChange() == ItemEvent.SELECTED && selectedMeltFile != null) {
				String item = (String) e.getItem();
				((MIFImage) selectedMeltFile).setStyle(item);
				
				logger.info("Switching over '{}'", item);
				switch (item) {
				case "HARD":
					imgPicture[1].setIcon(new ImageIcon(((MIFImage) selectedMeltFile).getPreviewHardResize()));
					resizeStyleDetails.setVisible(false);
					resizeStyleDetailsLabel.setVisible(false);
					break;
				case "FILL":
					imgPicture[1].setIcon(new ImageIcon(((MIFImage) selectedMeltFile).getPreviewFillWColor()));
					resizeStyleDetails.setVisible(true);
					resizeStyleDetailsLabel.setVisible(true);
					resizeStyleDetailsLabel.setText("Fill color:");
					break;
				case "CROP":
					imgPicture[1].setIcon(new ImageIcon(((MIFImage) selectedMeltFile).getPreviewCrop()));
					resizeStyleDetails.setVisible(true);
					resizeStyleDetailsLabel.setVisible(true);
					resizeStyleDetailsLabel.setText("Crop from where: ");
					break;
				case "MANUAL":
					imgPicture[1].setIcon(new ImageIcon(((MIFImage) selectedMeltFile).getPreviewManual()));
					resizeStyleDetails.setVisible(false);
					resizeStyleDetailsLabel.setVisible(false);
					break;
				default:
					logger.error("Unknown style '{}' for image. Using 'CROP", item);
					
					imgPicture[1].setIcon(new ImageIcon(((MIFImage) selectedMeltFile).getPreviewCrop()));
					resizeStyleDetails.setVisible(true);
					resizeStyleDetailsLabel.setVisible(true);
					resizeStyleDetailsLabel.setText("Crop from where: ");
				}
				
				box.updateUI();
			}
		};
	}

	public void clearIcons() {
		this.selectedMeltFile = null;
		this.selectedCell = null;

		imgPicture[0].setVisible(false);
		imgPicture[1].setVisible(false);
		manualExtraction.setVisible(false);

		imgPicture[0].setIcon(null);
		imgPicture[1].setIcon(null);

		resizeStyleLabel.setVisible(false);
		resizeStyle.setVisible(false);
		resizeStyleDetailsLabel.setVisible(false);
		resizeStyleDetails.setVisible(false);

		box.updateUI();
	}

	public void setPreviewPicture(String imagePreview) {
		if (!new File(imagePreview).exists()) {
			logger.warn("File does not exists: {}", imagePreview);
			return;
		}

		imgPicture[0].setVisible(true);
		imgPicture[1].setVisible(true);
		manualExtraction.setVisible(true);

		imgPicture[0].setIcon(new ImageIcon(imagePreview));

		resizeStyleLabel.setVisible(true);
		resizeStyle.setVisible(true);
		resizeStyleDetailsLabel.setVisible(true);
		resizeStyleDetails.setVisible(true);

		box.updateUI();
	}

	public void setSelectedPicture(String previewCrop) {
		if (!new File(previewCrop).exists()) {
			logger.warn("File does not exists: {}", previewCrop);
			return;
		}
		imgPicture[1].setIcon(new ImageIcon(previewCrop));

		box.updateUI();
	}
}

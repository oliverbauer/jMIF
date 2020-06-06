package io.github.jmif.gui.swing.selection.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.MIFException;
import io.github.jmif.Service;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MeltFilter;

public class ImageView {
	private static final Logger logger = LoggerFactory.getLogger(ImageView.class);

	private JLabel[] imgPicture;

	private JLabel resizeStyleLabel;
	private JComboBox<String> resizeStyle;
	private JLabel resizeStyleDetailsLabel;
	private JComboBox<String> resizeStyleDetails;
	private JButton manualExtraction = new JButton("manual");
	private JLabel lblCurrentlyAppliedFilters;
	private JComboBox<String> selectedFilter;
	private JButton addFilter;
	
	private JPanel panel;

	public ImageView() {

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
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getCurrentlyAppliedFilters());
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getAvailableFiltersBox());
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getSelectedFilterConfiguration());
		rightBox.add(Box.createVerticalGlue());
		
		selectedFilter.setSelectedItem("oldfilm");
		
		clearIcons();
		
		panel = new JPanel(new BorderLayout());
		panel.add(leftBox, BorderLayout.WEST);
		Box h = Box.createHorizontalBox();
		h.add(Box.createHorizontalStrut(10));
		h.add(rightBox);
		panel.add(h, BorderLayout.CENTER);
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

		dropDownBoxesBox.add(Box.createHorizontalGlue()); // Alignment of content starts left
		if (Configuration.useBorders) {
			dropDownBoxesBox.setBorder(BorderFactory.createLineBorder(Color.red));
		}
		Dimension dim = new Dimension(2500, 25); // max height = 50
		dropDownBoxesBox.setMinimumSize(dim);
		dropDownBoxesBox.setPreferredSize(dim);
		dropDownBoxesBox.setMaximumSize(dim);
		
		return dropDownBoxesBox;
	}

	private Box filterConfigurationContent;
	private JScrollPane scrollPane;
	private JScrollPane getSelectedFilterConfiguration() {
		filterConfigurationContent = Box.createVerticalBox();

		scrollPane = new JScrollPane(filterConfigurationContent);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		return scrollPane;
	}
	
	private Box getCurrentlyAppliedFilters() {
		Box box = Box.createHorizontalBox();
		
		lblCurrentlyAppliedFilters = new JLabel("Currently applied filters: TODO");
		box.add(lblCurrentlyAppliedFilters);
		
		Dimension dim = new Dimension(2500, 25); // max height = 25
		box.setMinimumSize(dim);
		box.setPreferredSize(dim);
		box.setMaximumSize(dim);
		
		return box;
	}
	
	private void updateCurrentlyAppliedFilters() {
		// TODO after each filter there should be a small remove-icon
		lblCurrentlyAppliedFilters.setText("Currently applied filters: "+
				selectedMeltFile.getFilters().stream().map(MeltFilter::getFiltername).collect(Collectors.joining(", ")));
	}
	
	private MeltFilter currentlySelectedFilter = null;
	private Box getAvailableFiltersBox() {
		List<String> filters = new ArrayList<>();
		filters.add("avfilter.deflicker");
		filters.add("frei0r.alphagrad");
		filters.add("greyscale");
		filters.add("grayscale");
		filters.add("oldfilm");
//		try {
//			filters = new Service().getFilters();
//				
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		selectedFilter = new JComboBox<>(filters.toArray(new String[filters.size()]));
		selectedFilter.addItemListener(e -> {
			if (e.getStateChange() == ItemEvent.DESELECTED) {
				return;
			}
			
			String filter = (String)selectedFilter.getSelectedItem();

			
			try {
				List<String> details = new Service().getFilterDetails(filter);
				
				// E.g. oldfilm has 8 parameter...
				
				MeltFilter meltFilter = new MeltFilter(filter);
				
				boolean parametersStarted = false;
				String currentParameter = null;
				for (String d : details) {
					if (d.contains("parameters:")) {
						parametersStarted = true;
					} else if (d.contains("identifier:") && parametersStarted) {
						currentParameter = d.substring(d.indexOf("identifier: ")+"identifier: ".length());
						meltFilter.appendConfigurationParameter(currentParameter); // z.B. delta, every, brightnessdelta_up, ...
						
					} else if (parametersStarted) {
						// Extract parameter details...
						d = d.trim();
						if (d.contains(":")) {
							String key = d.substring(0, d.indexOf(":")).trim();
							String description = d.substring(d.indexOf(":")+1).trim();

							meltFilter.appendConfigurationDetail(currentParameter, key, description);
						} else {
							// End of output...
						}
					}
				}
				
				showFilterConfiguration(meltFilter);
				
				currentlySelectedFilter = meltFilter;
				
			} catch (MIFException e1) {
				logger.error("Unable to get filter-details for '"+filter+"'", e1);
			}
		});
		selectedFilter.setPreferredSize(new Dimension(240, 25));
		selectedFilter.setMaximumSize(new Dimension(240, 25));
				
		Box dropDownBoxesBox = Box.createHorizontalBox();
		dropDownBoxesBox.add(selectedFilter);
		dropDownBoxesBox.add(Box.createHorizontalStrut(10));
		
		addFilter = new JButton("add");
		addFilter.addActionListener(e -> {
			selectedMeltFile.addFilter(currentlySelectedFilter);
			// TODO Filters: The image should be updated!
			updateCurrentlyAppliedFilters();
		});
		dropDownBoxesBox.add(addFilter);
		dropDownBoxesBox.add(Box.createHorizontalGlue());
		
		Dimension dim = new Dimension(2500, 25); // max height = 25
		dropDownBoxesBox.setMinimumSize(dim);
		dropDownBoxesBox.setPreferredSize(dim);
		dropDownBoxesBox.setMaximumSize(dim);
		
		return dropDownBoxesBox;
	}
	
	private void showFilterConfiguration(MeltFilter meltFilter) {
		filterConfigurationContent.removeAll();

		Map<String, Map<String, String>> configuration = meltFilter.getConfiguration();
		for (Entry<String, Map<String, String>> entry : configuration.entrySet()) {
			String param = entry.getKey();
			
			Map<String, String> keyValue = entry.getValue();

			Box horizontal = Box.createHorizontalBox();
			JLabel firstLabel = new JLabel(param);
			firstLabel.setMinimumSize(new Dimension(250, 20));
			firstLabel.setPreferredSize(new Dimension(250, 20));
			firstLabel.setMaximumSize(new Dimension(250, 20));
			horizontal.add(firstLabel);
			
			horizontal.add(Box.createHorizontalStrut(10));
			JTextField textField = new JTextField(keyValue.get("default"));
			textField.setMinimumSize(new Dimension(100, 20));
			textField.setPreferredSize(new Dimension(100, 20));
			textField.setMaximumSize(new Dimension(100, 20));
			horizontal.add(textField);
			horizontal.add(Box.createHorizontalGlue());
			filterConfigurationContent.add(horizontal);
		}
		if (configuration.entrySet().isEmpty()) {
			filterConfigurationContent.setVisible(false);
			scrollPane.setVisible(false);
		}
		if (!configuration.entrySet().isEmpty()) {
			filterConfigurationContent.setVisible(true);
			scrollPane.setVisible(true);
			filterConfigurationContent.add(Box.createVerticalGlue());
		}
		
		filterConfigurationContent.updateUI();
		if (panel != null) {
			panel.updateUI();
		}
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
				
				panel.updateUI();
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

		selectedFilter.setVisible(false);
		addFilter.setVisible(false);
		lblCurrentlyAppliedFilters.setVisible(false);
		filterConfigurationContent.setVisible(false);
		scrollPane.setVisible(false);
		
		if (panel != null) {
			panel.updateUI();
		}
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
		
		selectedFilter.setVisible(true);
		addFilter.setVisible(true);
		lblCurrentlyAppliedFilters.setVisible(true);
		filterConfigurationContent.setVisible(true);
		scrollPane.setVisible(true);
		
		panel.updateUI();
	}

	public void setSelectedPicture(String previewCrop) {
		if (!new File(previewCrop).exists()) {
			logger.warn("File does not exists: {}", previewCrop);
			return;
		}
		imgPicture[1].setIcon(new ImageIcon(previewCrop));

		panel.updateUI();
	}
}

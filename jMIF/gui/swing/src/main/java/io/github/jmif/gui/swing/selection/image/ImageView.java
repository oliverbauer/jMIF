package io.github.jmif.gui.swing.selection.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFImage.ImageResizeStyle;
import io.github.jmif.entities.MeltFilter;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.melt.Melt;
import io.github.jmif.melt.MeltFilterDetails;

public class ImageView {
	private Melt melt = new Melt();

	private static final Logger logger = LoggerFactory.getLogger(ImageView.class);

	private final GraphWrapper graphWrapper;

	private MeltFilter currentlySelectedFilter = null;
	private JLabel[] imgPicture;

	private JLabel resizeStyleLabel;
	private JComboBox<ImageResizeStyle> resizeStyle;
	private JLabel resizeStyleDetailsLabel;
	private JComboBox<String> resizeStyleDetails;
	private JButton manualExtraction = new JButton("manual");

	private Box currentlyAppliedFilters;
	private JLabel lblCurrentlyAppliedFilters;
	private JComboBox<String> selectedFilter;
	private JButton addFilter;
	private JButton previewFilter;
	private Box filterConfigurationContent;
	private JScrollPane scrollPane;	
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
		rightBox.add(Box.createVerticalStrut(5));

		currentlyAppliedFilters = getCurrentlyAppliedFilters();
		rightBox.add(currentlyAppliedFilters);
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getAvailableFiltersBox());
		rightBox.add(Box.createVerticalStrut(5));
		rightBox.add(getSelectedFilterConfiguration());
		rightBox.add(Box.createVerticalGlue());

		selectedFilter.setSelectedItem("oldfilm");

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
		Dimension dim = new Dimension(2500, 25); // max height = 50
		dropDownBoxesBox.setMinimumSize(dim);
		dropDownBoxesBox.setPreferredSize(dim);
		dropDownBoxesBox.setMaximumSize(dim);

		return dropDownBoxesBox;
	}

	private JScrollPane getSelectedFilterConfiguration() {
		filterConfigurationContent = Box.createVerticalBox();

		scrollPane = new JScrollPane(filterConfigurationContent);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		return scrollPane;
	}

	private Box getCurrentlyAppliedFilters() {
		Box box = Box.createHorizontalBox();

		lblCurrentlyAppliedFilters = new JLabel("Currently applied filters: -");
		box.add(lblCurrentlyAppliedFilters);

		Dimension dim = new Dimension(2500, 25); // max height = 25
		box.setMinimumSize(dim);
		box.setPreferredSize(dim);
		box.setMaximumSize(dim);

		return box;
	}

	private void updateCurrentlyAppliedFilters() {
		// TODO Filter: Each filter should be clickable: select from dropdown and set values as selected
		if (selectedMeltFile != null) {
			currentlyAppliedFilters.removeAll();

			lblCurrentlyAppliedFilters.setText("Currently applied filters: ");
			currentlyAppliedFilters.add(lblCurrentlyAppliedFilters);

			List<MeltFilter> filters = selectedMeltFile.getFilters();
			for (int i=0; i<=filters.size()-1; i++) {
				String filterName = filters.get(i).getFiltername();
				JLabel filterNameLabel = new JLabel(filterName);

				currentlyAppliedFilters.add(filterNameLabel);

				JLabel removeFilter = new JLabel(new ImageIcon(ImageView.class.getClassLoader().getResource("images/png/removeFilter.png")));
				removeFilter.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						removeFilter(filterName);
					}
				});
				currentlyAppliedFilters.add(removeFilter);

				if (i != filters.size() - 1) {
					currentlyAppliedFilters.add(new JLabel(", "));
				}
			}

			currentlyAppliedFilters.updateUI();
			panel.updateUI();
		}
	}

	private void removeFilter(String filterName) {
		MeltFilter filter = selectedMeltFile.getFilters().stream().filter(f -> f.getFiltername().equals(filterName)).findAny().get();
		selectedMeltFile.getFilters().remove(filter);

		if (filterName.equals((String)selectedFilter.getSelectedItem())) {
			addFilter.setEnabled(true);
		}

		updateCurrentlyAppliedFilters();
	}

	private Box getAvailableFiltersBox() throws MIFException {
		List<String> filters = new ArrayList<>();

		for (MeltFilterDetails meltFilterDetails : graphWrapper.getService().getMeltVideoFilterDetails(melt)) {
			filters.add(meltFilterDetails.getFiltername());
		}

		selectedFilter = new JComboBox<>(filters.toArray(new String[filters.size()]));
		selectedFilter.addItemListener(e -> {
			try {
				if (e.getStateChange() == ItemEvent.DESELECTED) {
					return;
				}

				String filter = (String)selectedFilter.getSelectedItem();

				if (selectedMeltFile != null) {
					// do not add a filter twice
					if (selectedMeltFile.getFilters().stream().map(MeltFilter::getFiltername).collect(Collectors.toSet()).contains(filter)) {
						addFilter.setEnabled(false);
					} else {
						addFilter.setEnabled(true);
					}
				}

				MeltFilter meltFilter = new MeltFilter(filter);
				showFilterConfiguration(meltFilter);
				currentlySelectedFilter = meltFilter;
			} catch (MIFException e1) {
				logger.error("", e);
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
			// TODO Filter: The image should be updated! Needs new creation and execution of melt file!
			updateCurrentlyAppliedFilters();

			addFilter.setEnabled(false); // since now added... do not add twice...
		});
		previewFilter = new JButton("preview");
		previewFilter.addActionListener(e -> {
			try {
				graphWrapper.getService().applyFilter(graphWrapper.getPr(), selectedMeltFile, currentlySelectedFilter);
			} catch (MIFException e1) {
				logger.error("", e1);
			}			
		});

		dropDownBoxesBox.add(addFilter);
		dropDownBoxesBox.add(previewFilter);
		dropDownBoxesBox.add(Box.createHorizontalGlue());

		Dimension dim = new Dimension(2500, 25); // max height = 25
		dropDownBoxesBox.setMinimumSize(dim);
		dropDownBoxesBox.setPreferredSize(dim);
		dropDownBoxesBox.setMaximumSize(dim);

		return dropDownBoxesBox;
	}

	private void showFilterConfiguration(MeltFilter meltFilter) throws MIFException {
		filterConfigurationContent.removeAll();

		MeltFilterDetails meltFilterDetails = 
				graphWrapper.getService().getMeltFilterDetailsFor(melt, meltFilter);

		Map<String, Integer> configIndex = meltFilterDetails.getConfigIndex(); // parameter to integer
		List<Map<String, String>> configuration = meltFilterDetails.getConfiguration();

		for (String param : configIndex.keySet()) {

			Map<String, String> keyValue = configuration.get(configIndex.get(param));

			Box horizontal = Box.createHorizontalBox();
			JLabel firstLabel = new JLabel(param);
			firstLabel.setMinimumSize(new Dimension(250, 20));
			firstLabel.setPreferredSize(new Dimension(250, 20));
			firstLabel.setMaximumSize(new Dimension(250, 20));
			horizontal.add(firstLabel);

			horizontal.add(Box.createHorizontalStrut(10));
			JTextField textField = new JTextField(keyValue.get("default"));
			textField.addActionListener(e -> {
				String enteredText = textField.getText();
				// TODO Filter: validate input?
				meltFilter.getFilterUsage().put(param, enteredText);

			});
			textField.setMinimumSize(new Dimension(100, 20));
			textField.setPreferredSize(new Dimension(100, 20));
			textField.setMaximumSize(new Dimension(100, 20));
			horizontal.add(textField);

			// description + minimum +maximum daneben...
			JLabel description = new JLabel(keyValue.get("description"));
			description.setMinimumSize(new Dimension(400, 20));
			description.setPreferredSize(new Dimension(400, 20));
			description.setMaximumSize(new Dimension(400, 20));
			horizontal.add(Box.createHorizontalStrut(10));
			horizontal.add(description);

			String details = null;
			if (keyValue.get("values") != null) {
				details = "values="+keyValue.get("values");
			} else if (keyValue.get("minimum") != null) {
				details = "min="+keyValue.get("minimum")+",max="+keyValue.get("maximum");
			} else {
				details = "type="+keyValue.get("type");
			}
			JLabel minMax = new JLabel(details);
			horizontal.add(Box.createHorizontalStrut(10));
			horizontal.add(minMax);

			horizontal.add(Box.createHorizontalGlue());
			filterConfigurationContent.add(horizontal);
		}
		if (configuration.isEmpty()) {
			filterConfigurationContent.setVisible(false);
			scrollPane.setVisible(false);
		}
		if (!configuration.isEmpty()) {
			filterConfigurationContent.setVisible(true);
			scrollPane.setVisible(true);
			filterConfigurationContent.add(Box.createVerticalGlue());
		}

		filterConfigurationContent.updateUI();
		if (panel != null) {
			panel.updateUI();
		}
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

		updateCurrentlyAppliedFilters();
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

		selectedFilter.setVisible(true);
		addFilter.setVisible(true);
		previewFilter.setVisible(true);
		lblCurrentlyAppliedFilters.setVisible(true);
		filterConfigurationContent.setVisible(true);
		scrollPane.setVisible(true);

		addFilter.setEnabled(true);

		updateCurrentlyAppliedFilters();

		panel.updateUI();
	}

	public void setSelectedPicture(Image previewCrop) {
		imgPicture[1].setIcon(new ImageIcon(previewCrop));

		panel.updateUI();
	}
}

package io.github.jmif.gui.swing.selection;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MeltFilter;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.selection.image.ImageView;
import io.github.jmif.melt.Melt;
import io.github.jmif.melt.MeltFilterDetails;

public class FilterView {
	private static final Logger logger = LoggerFactory.getLogger(FilterView.class);
	private Melt melt = new Melt();
	
	private MeltFilter currentlySelectedFilter = null;
	
	private final GraphWrapper graphWrapper;
	private final List<MeltFilterDetails> filters;
	
	private Box currentlyAppliedFilters;
	private JLabel lblCurrentlyAppliedFilters;
	private JComboBox<String> selectedFilter;
	private JButton addFilter;
	private JButton previewFilter;
	private Box filterConfigurationContent;
	private JScrollPane scrollPane;	
	
	private JPanel panel;
	
	private mxCell selectedCell;
	private MIFFile selectedMeltFile;
	
	public FilterView(final GraphWrapper graphWrapper, List<MeltFilterDetails> filters) throws MIFException {
		this.graphWrapper = graphWrapper;
		this.filters = filters;
		
		Box box = Box.createVerticalBox();

		currentlyAppliedFilters = getCurrentlyAppliedFilters();
		
		Box hBox = Box.createHorizontalBox();
		hBox.add(currentlyAppliedFilters);
		hBox.add(Box.createHorizontalGlue());
		box.add(hBox);
		box.add(Box.createVerticalStrut(5));
		
		Box hBox2 = Box.createHorizontalBox();
		hBox2.add(getAvailableFiltersBox());
		hBox2.add(Box.createHorizontalGlue());
		box.add(hBox2);
		box.add(Box.createVerticalStrut(5));
		
		
		box.add(getSelectedFilterConfiguration());
		box.add(Box.createVerticalGlue());

		selectedFilter.setSelectedItem("oldfilm");

		panel = new JPanel(new BorderLayout());
		panel.add(box, BorderLayout.CENTER);
	}
	
	public JPanel getJPanel() {
		return panel;
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

		for (MeltFilterDetails meltFilterDetails : this.filters) {
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
//		previewFilter.addActionListener(e -> {
//			try {
//				graphWrapper.getService().applyFilter(graphWrapper.getPr(), selectedMeltFile, currentlySelectedFilter);
//			} catch (MIFException e1) {
//				logger.error("", e1);
//			}			
//		});

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

	public void setDetails(MIFFile mifVideo) {
		this.selectedMeltFile = mifVideo;
		updateCurrentlyAppliedFilters();
		this.panel.updateUI();
	}
}
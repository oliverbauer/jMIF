package io.github.jmif.gui.swing.graph;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.config.Configuration;
import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFFileWrapper;
import io.github.jmif.gui.swing.selection.SelectionView;

public class GraphView {
	private static final Logger logger = LoggerFactory.getLogger(GraphView.class);
	
	private JFrame frame;
	private GraphWrapper graphWrapper;
	private SelectionView selectionView;
	
	private Box graphPanel;
	
	public GraphView(JFrame frame, GraphWrapper graphWrapper, SelectionView mifPanel) {
		this.frame = frame;
		this.graphWrapper = graphWrapper;
		this.selectionView = mifPanel;
	}
	
	public void init() {
		JLabel zoom = new JLabel("Zoom: ");
		JLabel plus = new JLabel("+");
		JLabel minus = new JLabel("-");
		Box horizontalZoomBox = Box.createHorizontalBox();
		horizontalZoomBox.add(Box.createHorizontalGlue());
		horizontalZoomBox.add(Box.createRigidArea(new Dimension(10, 0)));
		horizontalZoomBox.add(zoom);
		horizontalZoomBox.add(plus);
		horizontalZoomBox.add(minus);
		horizontalZoomBox.add(Box.createRigidArea(new Dimension(10, 0)));
		
		graphPanel = Box.createVerticalBox();
		graphPanel.setBackground(Configuration.bgColor);
		graphPanel.add(horizontalZoomBox);
		Box addRemoveForTracksBox = Box.createHorizontalBox();
		Box verticalAddRemove = Box.createVerticalBox();
		verticalAddRemove.add(Box.createVerticalStrut(20));
		
		verticalAddRemove.add(createAddFileLabel(frame));
		verticalAddRemove.add(createRemoveFileLabel());
		verticalAddRemove.add(Box.createVerticalStrut(20));
		
		verticalAddRemove.add(createAddText());
		verticalAddRemove.add(createRemoveText());
		verticalAddRemove.add(Box.createVerticalStrut(20));
		
		verticalAddRemove.add(createAddAudioLabel(frame));
		verticalAddRemove.add(createRemoveAudioLabel());
		verticalAddRemove.add(Box.createVerticalStrut(20));
		
		verticalAddRemove.setMaximumSize(new Dimension(20, 200));
		if (Configuration.useBorders) {
			verticalAddRemove.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		}
		
		addRemoveForTracksBox.add(Box.createHorizontalStrut(5));
		addRemoveForTracksBox.add(verticalAddRemove);
		addRemoveForTracksBox.add(Box.createHorizontalStrut(5));
		
		addRemoveForTracksBox.add(graphWrapper.getGraphComponent());
		addRemoveForTracksBox.add(Box.createHorizontalStrut(10));
		graphPanel.add(addRemoveForTracksBox);
	}
	
	private Component createRemoveText() {
		JLabel removeText = new JLabel("-");
		removeText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectionView.getCurrentAudioFile() != null) {
					MIFTextFile currentMeltFile = selectionView.getCurrentTextFile();
					mxCell cell = selectionView.getCell();
					
					remove(cell, currentMeltFile);
				}
			}
		});

		return removeText;
	}

	private Component createAddText() {
		JLabel appenText = new JLabel("+");
		appenText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					graphWrapper.createMIFTextfile();
				} catch (MIFException e1) {
					e1.printStackTrace();
				}
				graphWrapper.redrawGraph();
			}
		});
		
		return appenText;
	}

	public Box getGraphPanel() {
		return graphPanel;
	}
	
	private JLabel createAddAudioLabel(JFrame frame) {
		JLabel appendAudio = new JLabel("+");
		appendAudio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					JFileChooser c = new JFileChooser();
					
					int returnVal = c.showOpenDialog(frame);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						graphWrapper.createMIFAudioFile(c.getSelectedFile());
						graphWrapper.redrawGraph();
					}
				} catch (HeadlessException | MIFException e1) {
					e1.printStackTrace();
				}
			}
		});
		return appendAudio;
	}
	
	private JLabel createAddFileLabel(JFrame frame) {
		JLabel appendFile = new JLabel("+");
		appendFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser c = new JFileChooser();
				c.setMultiSelectionEnabled(true);
				c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				
				int returnVal = c.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {

					File[] filesToAdd = c.getSelectedFiles();
					
					List<File> selectedFiles = new ArrayList<>();
					for (File fileToAdd : filesToAdd) {
						if (fileToAdd.isDirectory()) {
							for (File f : fileToAdd.listFiles()) {
								// No sub dires.
								if (!f.isDirectory()) {
									selectedFiles.add(f);
								}
							}
						} else {
							selectedFiles.add(fileToAdd);
						}
					}
					List<MIFFileWrapper<?>> added = new ArrayList<>();
					for (File fileToAdd : selectedFiles) {
						MIFFileWrapper<?> f;
						try {
							f = graphWrapper.createMIFFile(fileToAdd);
							added.add(f);
						} catch (MIFException | IOException | InterruptedException e1) {
							logger.error("Unable to create file", e1);
						}
					}
					graphWrapper.redrawGraph();
					
					// Exec background threads...
					ExecutorService executor = Executors.newWorkStealingPool();
					for (MIFFileWrapper<?> f : added) {
						executor.submit(() -> {
							try {
								// TODO übergeben
								graphWrapper.getService().createPreview(f.toMIFFile(), graphWrapper.getPr().getWorkingDir());
							} catch (Exception ex) {
								logger.error("", e);
							}
						});
					}
				}
			}
		});
		return appendFile;
	}
	
	private JLabel createRemoveFileLabel() {
		JLabel removeFile = new JLabel("-");
		removeFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectionView.getCurrentMeltFile() != null) {
					MIFFileWrapper<?> currentMeltFile = selectionView.getCurrentMeltFile();
					mxCell cell = selectionView.getCell();
					remove(cell, currentMeltFile);
				}
			}
		});

		return removeFile;
	}

	private JLabel createRemoveAudioLabel() {
		JLabel removeFile = new JLabel("-");
		removeFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (selectionView.getCurrentAudioFile() != null) {
					var currentMeltFile = selectionView.getCurrentAudioFile();
					mxCell cell = selectionView.getCell();
					
					remove(cell, currentMeltFile);
				}
			}
		});

		return removeFile;
	}
	
	public void remove(mxCell cell, MIFTextFile meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFFileWrapper<?> meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFAudioFile meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
}

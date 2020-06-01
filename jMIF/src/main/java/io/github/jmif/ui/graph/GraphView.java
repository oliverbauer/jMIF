package io.github.jmif.ui.graph;

import java.awt.Color;
import java.awt.Dimension;
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

import com.mxgraph.model.mxCell;

import io.github.jmif.config.Configuration;
import io.github.jmif.data.GraphWrapper;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.ui.selection.SelectionView;

public class GraphView {
	private JFrame frame;
	private GraphWrapper mifProject;
	private SelectionView mifPanel;
	
	private Box graphPanel;
	
	public GraphView(JFrame frame, GraphWrapper mifProject, SelectionView mifPanel) {
		this.frame = frame;
		this.mifProject = mifProject;
		this.mifPanel = mifPanel;
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
		verticalAddRemove.add(Box.createVerticalGlue());
		verticalAddRemove.add(createAddAudioLabel(frame));
		verticalAddRemove.add(createRemoveAudioLabel());
		verticalAddRemove.add(Box.createVerticalStrut(30));
		verticalAddRemove.setMaximumSize(new Dimension(20, 200));
		if (Configuration.useBorders) {
			verticalAddRemove.setBorder(BorderFactory.createLineBorder(Color.BLUE));
		}
		
		addRemoveForTracksBox.add(Box.createHorizontalStrut(5));
		addRemoveForTracksBox.add(verticalAddRemove);
		addRemoveForTracksBox.add(Box.createHorizontalStrut(5));
		
		addRemoveForTracksBox.add(mifProject.getGraphComponent());
		addRemoveForTracksBox.add(Box.createHorizontalStrut(10));
		graphPanel.add(addRemoveForTracksBox);
	}
	
	public Box getGraphPanel() {
		return graphPanel;
	}
	
	private JLabel createAddAudioLabel(JFrame frame) {
		JLabel appendAudio = new JLabel("+");
		appendAudio.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser c = new JFileChooser();
				
				int returnVal = c.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					mifProject.createMIFAudioFile(c.getSelectedFile());
					mifProject.redrawGraph();
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
					List<MIFFile> added = new ArrayList<>();
					for (File fileToAdd : selectedFiles) {
						MIFFile f;
						try {
							f = mifProject.createMIFFile(fileToAdd);
							added.add(f);
						} catch (IOException | InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					mifProject.redrawGraph();
					
					// Exec background threads...
					ExecutorService executor = Executors.newWorkStealingPool();
					for (MIFFile f : added) {
						Runnable r = f.getBackgroundRunnable(mifProject.getPr().getWorkingDir());
						executor.submit(r);
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
				if (mifPanel.getCurrentMeltFile() != null) {
					MIFFile currentMeltFile = mifPanel.getCurrentMeltFile();
					mxCell cell = mifPanel.getCell();
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
				if (mifPanel.getCurrentAudioFile() != null) {
					MIFAudioFile currentMeltFile = mifPanel.getCurrentAudioFile();
					mxCell cell = mifPanel.getCell();
					
					remove(cell, currentMeltFile);
				}
			}
		});

		return removeFile;
	}
	
	public void remove(mxCell cell, MIFFile meltfile) {
		mifProject.remove(meltfile, cell);
		mifProject.remove(cell);

		mifPanel.clearSelection();
		// Timeline etc.
		mifProject.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFAudioFile meltfile) {
		mifProject.remove(meltfile, cell);
		mifProject.remove(cell);

		mifPanel.clearSelection();
		// Timeline etc.
		mifProject.redrawGraph();
	}
}

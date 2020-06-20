package io.github.jmif.gui.swing.graph;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.config.Configuration;
import io.github.jmif.core.MIFException;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFAudioFileWrapper;
import io.github.jmif.gui.swing.entities.MIFFileWrapper;
import io.github.jmif.gui.swing.entities.MIFTextFileWrapper;
import io.github.jmif.gui.swing.selection.SelectionView;

public class GraphView implements AddRemoveListener {
	private static final Logger logger = LoggerFactory.getLogger(GraphView.class);
	
	private JFrame frame;
	private GraphWrapper graphWrapper;
	private SelectionView selectionView;
	
	private Box graphPanel;
	
	public GraphView(JFrame frame, GraphWrapper graphWrapper, SelectionView mifPanel) {
		this.frame = frame;
		this.graphWrapper = graphWrapper;
		this.selectionView = mifPanel;
		
		this.graphWrapper.setAddRemoveActionListener(this);
	}
	
	public void init() {
		graphPanel = Box.createVerticalBox();
		graphPanel.add(graphWrapper.getGraphComponent());
	}
	
	public Box getGraphPanel() {
		return graphPanel;
	}
	
	public void remove(mxCell cell, MIFFileWrapper<?> meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFAudioFileWrapper meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}

	@Override
	public void onRemoveFile() {
		if (selectionView.getCurrentMeltFile() != null) {
			MIFFileWrapper<?> currentMeltFile = selectionView.getCurrentMeltFile();
			var cell = selectionView.getCell();
			remove(cell, currentMeltFile);
		}
	}

	@Override
	public void onRemoveAudio() {
		if (selectionView.getCurrentAudioFile() != null) {
			var currentMeltFile = selectionView.getCurrentAudioFile();
			var cell = selectionView.getCell();
			
			remove(cell, currentMeltFile);
		}
	}

	@Override
	public void onRemoveText() {
		if (selectionView.getCurrentAudioFile() != null) {
			var currentMeltFile = selectionView.getCurrentTextFile();
			var cell = selectionView.getCell();
			
			graphWrapper.remove(currentMeltFile, cell);
			graphWrapper.remove(cell);

			selectionView.clearSelection();
			// Timeline etc.
			graphWrapper.redrawGraph();
		}
	}

	@Override
	public void onAddFile() {
		var c = new JFileChooser();
		c.setMultiSelectionEnabled(true);
		c.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		var returnVal = c.showOpenDialog(frame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {

			var filesToAdd = c.getSelectedFiles();
			
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
			var executor = Executors.newWorkStealingPool();
			for (MIFFileWrapper<?> f : added) {
				executor.submit(() -> {
					try {
						// TODO übergeben
						graphWrapper.getService().createPreview(f, graphWrapper.getPr().getWorkingDir());
					} catch (Exception ex) {
						logger.error("", ex);
					}
				});
			}
		}
	}

	@Override
	public void onAddAudio() {
		try {
			var c = new JFileChooser();
			
			var returnVal = c.showOpenDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				graphWrapper.createMIFAudioFile(c.getSelectedFile());
				graphWrapper.redrawGraph();
			}
		} catch (HeadlessException | MIFException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onAddText() {
		try {
			graphWrapper.createMIFTextfile();
		} catch (MIFException e1) {
			e1.printStackTrace();
		}
		graphWrapper.redrawGraph();
	}
}

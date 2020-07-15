package io.github.jmif.gui.swing.graph;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFAudio;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.config.UserConfig;
import io.github.jmif.gui.swing.listener.AddRemoveListener;
import io.github.jmif.gui.swing.selection.SelectionView;

public class GraphView implements AddRemoveListener {
	@Inject
	private UserConfig userConfig;
	
	private static final Logger logger = LoggerFactory.getLogger(GraphView.class);
	
	private JFrame frame;
	private GraphWrapper graphWrapper;
	private SelectionView selectionView;
	
	private Box graphPanel;
	
	public void init(JFrame frame, GraphWrapper graphWrapper, SelectionView mifPanel) {
		this.frame = frame;
		this.graphWrapper = graphWrapper;
		this.selectionView = mifPanel;
		
		this.graphWrapper.setAddRemoveActionListener(this);

		graphPanel = Box.createVerticalBox();
		graphPanel.add(graphWrapper.getGraphComponent());
	}
	
	public Box getGraphPanel() {
		return graphPanel;
	}
	
	public void remove(mxCell cell, MIFFile meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFAudio meltfile) {
		graphWrapper.remove(meltfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}
	
	public void remove(mxCell cell, MIFTextFile textfile) {
		graphWrapper.remove(textfile, cell);
		graphWrapper.remove(cell);

		selectionView.clearSelection();
		// Timeline etc.
		graphWrapper.redrawGraph();
	}

	@Override
	public void onRemoveFile() {
		if (selectionView.getCurrentFile() != null) {
			MIFFile file = selectionView.getCurrentFile();
			var cell = selectionView.getCell();
			remove(cell, file);
		}
	}

	@Override
	public void onRemoveAudio() {
		if (selectionView.getCurrentAudio() != null) {
			var file = selectionView.getCurrentAudio();
			var cell = selectionView.getCell();
			
			remove(cell, file);
		}
	}

	@Override
	public void onRemoveText() {
		if (selectionView.getCurrentText() != null) {
			var file = selectionView.getCurrentText();
			var cell = selectionView.getCell();
			
			graphWrapper.remove(file, cell);
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
			List<MIFFile> added = new ArrayList<>();
			for (File fileToAdd : selectedFiles) {
				MIFFile f;
				try {
					f = graphWrapper.createMIFFile(fileToAdd);
					if (f instanceof MIFImage) {
						f.setOverlayToPrevious(userConfig.getIMAGE_OVERLAY());
						f.setDuration(userConfig.getIMAGE_DURATION());
						
					} else if (f instanceof MIFVideo) {
						f.setOverlayToPrevious(userConfig.getVIDEO_OVERLAY());
					}
					
					added.add(f);
				} catch (MIFException | IOException | InterruptedException e1) {
					logger.error("Unable to create file", e1);
				}
			}
			
			graphWrapper.redrawGraph();
			
			// Exec background threads...
			var executor = Executors.newWorkStealingPool();
			for (MIFFile f : added) {
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
				MIFAudio audioFile = graphWrapper.createMIFAudioFile(c.getSelectedFile());
				audioFile.setFadeIn(userConfig.getAUDIO_FADE_IN());
				audioFile.setFadeOut(userConfig.getAUDIO_FADE_OUT());
				audioFile.setNormalize(userConfig.isAUDIO_NORMALIZE());
				
				graphWrapper.redrawGraph();
			}
		} catch (HeadlessException | MIFException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void onAddText() {
		try {
			MIFTextFile textfile = graphWrapper.createMIFTextfile();
			textfile.setLength(userConfig.getTEXT_DURATION());
			textfile.setBgcolour(userConfig.getTEXT_BG());
			textfile.setFgcolour(userConfig.getTEXT_FG());
			textfile.setOlcolour(userConfig.getTEXT_OL());
			
			// Select the new created cell, it is always last on list 
			List<mxCell> textCells = graphWrapper.getTextCells();
			graphWrapper.getGraph().getSelectionModel().setCell(textCells.get(textCells.size()-1));
			graphWrapper.redrawGraph();
		} catch (MIFException e1) {
			e1.printStackTrace();
		}
	}
}

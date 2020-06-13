package io.github.jmif.gui.swing.selection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.MIFException;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.selection.audio.AudioDetailsView;
import io.github.jmif.gui.swing.selection.audio.AudioView;
import io.github.jmif.gui.swing.selection.frame.FrameView;
import io.github.jmif.gui.swing.selection.image.ImageDetailsView;
import io.github.jmif.gui.swing.selection.image.ImageView;
import io.github.jmif.gui.swing.selection.imageLibrary.ImageLibraryView;
import io.github.jmif.gui.swing.selection.video.VideoDetailsView;
import io.github.jmif.gui.swing.selection.video.VideoView;
import io.github.jmif.melt.Melt;
import io.github.jmif.melt.MeltFilterDetails;

public class SelectionView {
	private static final Logger logger = LoggerFactory.getLogger(SelectionView.class);
	
	private Box panel;
	
	private MIFFile selectedMeltFile = null;
	private MIFAudioFile selectedAudioFile = null;
	private mxCell selectedCell;

	private JTabbedPane tabPane;
	
	private ImageView imageView;
	private ImageDetailsView imageDetailsView;
	private FilterView filterViewImage;
	
	private VideoView videoView;
	private VideoDetailsView videoDetailsView;
	private FilterView filterViewVideo;
	
	private AudioView audioView;
	private AudioDetailsView audioDetailsView;
	private FilterView filterViewAudio;
	
	private FrameView singleFrameView;

    public SelectionView(GraphWrapper graphWrapper) throws MIFException {
		panel = Box.createVerticalBox();
		tabPane = new JTabbedPane();
		imageDetailsView = new ImageDetailsView(graphWrapper);
		videoDetailsView = new VideoDetailsView(graphWrapper);
		audioDetailsView = new AudioDetailsView(graphWrapper);
		
		Melt melt = new Melt();
		List<MeltFilterDetails> audioFilters = graphWrapper.getService().getMeltAudioFilterDetails(melt);
		List<MeltFilterDetails> imageAndVideoFilters = graphWrapper.getService().getMeltVideoFilterDetails(melt);
		filterViewVideo = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewImage = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewAudio = new FilterView(graphWrapper, audioFilters);
		
		int w = 5500;
		imageView = new ImageView(graphWrapper);
		int h = 550;
		
		videoView = new VideoView();
		JPanel videoPanel = videoView.getPanel();
		if (Configuration.useBorders) {
			videoPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2, true));
		}
		
		audioView = new AudioView();
		Box audioPanel = audioView.getBox();
		audioPanel.setMinimumSize(new Dimension(w, h));
		audioPanel.setPreferredSize(new Dimension(w, h));
		audioPanel.setMaximumSize(new Dimension(w, h));
		if (Configuration.useBorders) {
			audioPanel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2, true));
		}
		
		singleFrameView = new FrameView();
		graphWrapper.addSingleFrameCreatedListener(singleFrameView);
		Box singleFrameBox = singleFrameView.getBox();
		singleFrameBox.setMinimumSize(new Dimension(w, h));
		singleFrameBox.setPreferredSize(new Dimension(w, h));
		singleFrameBox.setMaximumSize(new Dimension(w, h));
		if (Configuration.useBorders) {
			singleFrameBox.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2, true));
		}
		
		tabPane.addTab("ImageView", wrap(imageDetailsView.getBox(), imageView.getJPanel(), filterViewImage.getJPanel()));
		tabPane.addTab("VideoView", wrap(videoDetailsView.getBox(), videoPanel, filterViewVideo.getJPanel()));
		tabPane.addTab("AudioView", wrap(audioDetailsView.getBox(), filterViewAudio.getJPanel(), null));
		tabPane.addTab("AudioView", wrap(null,            singleFrameBox, null));
		tabPane.addTab("ImageLibrary", wrap(new ImageLibraryView(graphWrapper).getBox(), null, null));
		tabPane.setSelectedIndex(4);
		tabPane.setEnabledAt(0, false);
		tabPane.setEnabledAt(1, false);
		tabPane.setEnabledAt(2, false);
		tabPane.setEnabledAt(3, false);
		
		panel.add(tabPane);
		
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2, true));
		}
	}
    
    private JPanel wrap(JComponent c1, JComponent c2, JComponent c3) {
		JPanel panel = new JPanel(new BorderLayout());
		if (c1 != null) {
			panel.add(c1, BorderLayout.NORTH);
		}
		if (c2 != null && c3 == null) {
			panel.add(c2, BorderLayout.CENTER);
		} else if (c2 != null && c3 != null) {
			
			JPanel p = new JPanel(new BorderLayout());
			p.add(c2, BorderLayout.NORTH);
			p.add(c3, BorderLayout.CENTER);
			
			JScrollPane scrollBar = new JScrollPane(p);
			panel.add(scrollBar, BorderLayout.CENTER);
			
		}
		return panel;
    }
	
	public void clearSelection() {
		this.selectedMeltFile = null;
		this.selectedCell = null;
		this.selectedAudioFile = null;
	}
	
	public void setSingleFrameView() {
		tabPane.setSelectedIndex(3);
		tabPane.setEnabledAt(0, false);
		tabPane.setEnabledAt(1, false);
		tabPane.setEnabledAt(2, false);
		tabPane.setEnabledAt(3, true);
		
		clearSelection();
	}
	
	public void updateAudio(mxCell cell, MIFAudioFile audioFile, GraphWrapper project) {
		clearSelection();

		this.selectedAudioFile = audioFile;
		this.selectedCell = cell;
		
		tabPane.setSelectedIndex(2);
		tabPane.setEnabledAt(0, false);
		tabPane.setEnabledAt(1, false);
		tabPane.setEnabledAt(2, true);
		tabPane.setEnabledAt(3, false);
		
		
		audioDetailsView.setDetails(audioFile);
		audioView.setMIFAudioFile(audioFile, project);
		filterViewAudio.setDetails(audioFile);
		
		panel.validate();
		panel.updateUI();
	}
	
	public void updateAudioOrVideo(mxCell cell, MIFFile meltFile) {
		clearSelection();
		
		this.selectedMeltFile = meltFile;
		this.selectedCell = cell;		
		logger.trace("Update {}", meltFile.getClass());
		
		if (meltFile instanceof MIFImage) {
			tabPane.setSelectedIndex(0);
			tabPane.setEnabledAt(0, true);
			tabPane.setEnabledAt(1, false);
			tabPane.setEnabledAt(2, false);
			tabPane.setEnabledAt(3, false);
			
			imageView.setPreviewPicture(((MIFImage)meltFile).getImagePreview());
			imageView.update(cell, (MIFImage)meltFile);
			imageDetailsView.setDetails(meltFile);
			filterViewImage.setDetails(meltFile);
		} else if (meltFile instanceof MIFVideo) {
			tabPane.setSelectedIndex(1);
			tabPane.setEnabledAt(0, false);
			tabPane.setEnabledAt(1, true);
			tabPane.setEnabledAt(2, false);
			tabPane.setEnabledAt(3, false);

			
			videoView.setIcons(((MIFVideo)meltFile).getPreviewImages());
			videoDetailsView.setDetails( ((MIFVideo)meltFile));
			filterViewVideo.setDetails(meltFile);
		} else {
			logger.warn("Not supported yet...");
		}

		panel.validate();
		panel.updateUI();
	}

	public Box getPanel() {
		return panel;
	}
	
	public MIFFile getCurrentMeltFile() {
		return selectedMeltFile;
	}

	public MIFAudioFile getCurrentAudioFile() {
		return selectedAudioFile;
	}
	
	public mxCell getCell() {
		return selectedCell;
	}
}

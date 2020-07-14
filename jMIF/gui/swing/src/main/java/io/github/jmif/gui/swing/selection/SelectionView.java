package io.github.jmif.gui.swing.selection;

import java.awt.BorderLayout;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFAudio;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.UISingleton;
import io.github.jmif.gui.swing.selection.audio.AudioDetailsView;
import io.github.jmif.gui.swing.selection.frame.FrameView;
import io.github.jmif.gui.swing.selection.image.ImageDetailsView;
import io.github.jmif.gui.swing.selection.image.ImageView;
import io.github.jmif.gui.swing.selection.imageLibrary.ImageLibraryView;
import io.github.jmif.gui.swing.selection.text.TextDetailsView;
import io.github.jmif.gui.swing.selection.video.VideoDetailsView;
import io.github.jmif.gui.swing.selection.video.VideoView;

public class SelectionView {
	private static final Logger logger = LoggerFactory.getLogger(SelectionView.class);
	
	private Box panel;
	
	private MIFFile selectedMeltFile = null;
	private MIFAudio selectedAudioFile = null;
	private MIFTextFile selectedTextFile = null;
	private mxCell selectedCell;

	private JTabbedPane tabPane;
	
	@Inject
	private ImageLibraryView imageLibraryView;
	
	@Inject
	private ImageView imageView;
	private ImageDetailsView imageDetailsView;
	private FilterView filterViewImage;
	
	private VideoView videoView;
	private VideoDetailsView videoDetailsView;
	private FilterView filterViewVideo;
	
	private AudioDetailsView audioDetailsView;
	private FilterView filterViewAudio;
	
	private TextDetailsView textDetailsView;
	
	private FrameView singleFrameView;

    public void init(GraphWrapper graphWrapper) throws MIFException {
		panel = Box.createVerticalBox();
		tabPane = new JTabbedPane();
		imageDetailsView = new ImageDetailsView(graphWrapper);
		videoDetailsView = new VideoDetailsView(graphWrapper);
		audioDetailsView = new AudioDetailsView(graphWrapper);
		textDetailsView = new TextDetailsView(graphWrapper);
		
		UISingleton.get().setImageDetailsView(imageDetailsView);
		
		var melt = new Melt();
		var audioFilters = graphWrapper.getService().getMeltAudioFilterDetails(melt);
		var imageAndVideoFilters = graphWrapper.getService().getMeltVideoFilterDetails(melt);
		filterViewVideo = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewImage = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewAudio = new FilterView(graphWrapper, audioFilters);
		
		imageView.init(graphWrapper);
		UISingleton.get().setImageView(imageView);
		
		videoView = new VideoView();
		var videoPanel = videoView.getPanel();
		
		singleFrameView = new FrameView();
		graphWrapper.addSingleFrameCreatedListener(singleFrameView);
		var singleFrameBox = singleFrameView.getBox();
		
		imageLibraryView.init(graphWrapper);
		
		tabPane.addTab("ImageView", wrap(imageDetailsView.getBox(), imageView.getJPanel(),       filterViewImage.getJPanel()));
		tabPane.addTab("VideoView", wrap(videoDetailsView.getBox(), videoPanel,                  filterViewVideo.getJPanel()));
		tabPane.addTab("TextView",  wrap(textDetailsView.getBox(),  null,                   null));
		tabPane.addTab("AudioView", wrap(audioDetailsView.getBox(), filterViewAudio.getJPanel(), null));
		tabPane.addTab("FrameView", wrap(null,                      singleFrameBox,              null));
		tabPane.addTab("ImageLibrary", wrap(imageLibraryView.getBox(), null, null));
		tabPane.setSelectedIndex(5);
		tabPane.setEnabledAt(0, false); // Image
		tabPane.setEnabledAt(1, false); // Video
		tabPane.setEnabledAt(2, false); // Text
		tabPane.setEnabledAt(3, false); // Audio
		tabPane.setEnabledAt(4, false); // Frame
		tabPane.setEnabledAt(5, true);  // ImageLibrary
		
		panel.add(tabPane);
	}
    
    private JPanel wrap(JComponent c1, JComponent c2, JComponent c3) {
		var panel = new JPanel(new BorderLayout());
		if (c1 != null) {
			panel.add(c1, BorderLayout.NORTH);
		}
		if (c2 != null && c3 == null) {
			panel.add(c2, BorderLayout.CENTER);
		} else if (c2 != null && c3 != null) {
			
			var p = new JPanel(new BorderLayout());
			p.add(new JScrollPane(c2), BorderLayout.NORTH);
			p.add(c3, BorderLayout.CENTER);

			panel.add(p, BorderLayout.CENTER);
			
		}
		return panel;
    }
	
	public void clearSelection() {
		this.selectedMeltFile = null;
		this.selectedCell = null;
		this.selectedAudioFile = null;
	}
	
	public void setSingleFrameView() {
		tabPane.setSelectedIndex(4);
		tabPane.setEnabledAt(0, false); // Image
		tabPane.setEnabledAt(1, false); // Video
		tabPane.setEnabledAt(2, false); // Text
		tabPane.setEnabledAt(3, false); // Audio
		tabPane.setEnabledAt(4, true); // Frame
		tabPane.setEnabledAt(5, true);  // ImageLibrary
		
		clearSelection();
	}
	
	public void updateAudio(mxCell cell, MIFAudio audioFile) {
		clearSelection();

		this.selectedAudioFile = audioFile;
		this.selectedCell = cell;
		
		tabPane.setSelectedIndex(3);
		tabPane.setEnabledAt(0, false); // Image
		tabPane.setEnabledAt(1, false); // Video
		tabPane.setEnabledAt(2, false); // Text
		tabPane.setEnabledAt(3, true); // Audio
		tabPane.setEnabledAt(3, false); // Frame
		tabPane.setEnabledAt(5, true);  // ImageLibrary
		
		audioDetailsView.setDetails(audioFile);
		filterViewAudio.update(audioFile);
		
		panel.validate();
		panel.updateUI();
	}
	
	public void updateText(mxCell cell, MIFTextFile meltFile) {
		clearSelection();
		
		this.selectedTextFile = meltFile;
		this.selectedCell = cell;

		tabPane.setSelectedIndex(2);
		tabPane.setEnabledAt(0, false); // Image
		tabPane.setEnabledAt(1, false); // Video
		tabPane.setEnabledAt(2, true); // Text
		tabPane.setEnabledAt(3, false); // Audio
		tabPane.setEnabledAt(3, false); // Frame
		tabPane.setEnabledAt(5, true);  // ImageLibrary
		
		textDetailsView.setDetails(selectedTextFile);
		
		panel.validate();
		panel.updateUI();
	}
	
	public void updateImageOrVideo(mxCell cell, MIFFile meltFile) {
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
			filterViewImage.update(meltFile);
		} else if (meltFile instanceof MIFVideo) {
			tabPane.setSelectedIndex(1);
			tabPane.setEnabledAt(0, false);
			tabPane.setEnabledAt(1, true);
			tabPane.setEnabledAt(2, false);
			tabPane.setEnabledAt(3, false);

			
			videoView.setIcons(((MIFVideo)meltFile).getPreviewImages());
			videoDetailsView.setDetails( ((MIFVideo)meltFile));
			filterViewVideo.update(meltFile);
		} else {
			logger.warn("Not supported yet...");
		}

		panel.validate();
		panel.updateUI();
	}

	public Box getPanel() {
		return panel;
	}
	
	public MIFFile getCurrentFile() {
		return selectedMeltFile;
	}

	public MIFAudio getCurrentAudio() {
		return selectedAudioFile;
	}
	
	public mxCell getCell() {
		return selectedCell;
	}

	public MIFTextFile getCurrentText() {
		return selectedTextFile;
	}
}

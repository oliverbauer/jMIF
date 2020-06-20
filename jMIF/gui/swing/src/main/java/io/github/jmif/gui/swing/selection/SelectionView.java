package io.github.jmif.gui.swing.selection;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.core.MIFException;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilterDetails;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFAudioFileWrapper;
import io.github.jmif.gui.swing.entities.MIFFileWrapper;
import io.github.jmif.gui.swing.entities.MIFImageWrapper;
import io.github.jmif.gui.swing.entities.MIFTextFileWrapper;
import io.github.jmif.gui.swing.entities.MIFVideoWrapper;
import io.github.jmif.gui.swing.selection.audio.AudioDetailsView;
import io.github.jmif.gui.swing.selection.frame.FrameView;
import io.github.jmif.gui.swing.selection.image.ImageDetailsView;
import io.github.jmif.gui.swing.selection.image.ImageView;
import io.github.jmif.gui.swing.selection.imageLibrary.ImageLibraryView;
import io.github.jmif.gui.swing.selection.text.TextDetailsView;
import io.github.jmif.gui.swing.selection.text.TextView;
import io.github.jmif.gui.swing.selection.video.VideoDetailsView;
import io.github.jmif.gui.swing.selection.video.VideoView;

public class SelectionView {
	private static final Logger logger = LoggerFactory.getLogger(SelectionView.class);
	
	private Box panel;
	
	private MIFFileWrapper<?> selectedMeltFile = null;
	private MIFAudioFileWrapper selectedAudioFile = null;
	private MIFTextFileWrapper selectedTextFile = null;
	private mxCell selectedCell;

	private JTabbedPane tabPane;
	
	private ImageView imageView;
	private ImageDetailsView imageDetailsView;
	private FilterView filterViewImage;
	
	private VideoView videoView;
	private VideoDetailsView videoDetailsView;
	private FilterView filterViewVideo;
	
	private AudioDetailsView audioDetailsView;
	private FilterView filterViewAudio;
	
	private TextView textView;
	private TextDetailsView textDetailsView;
	
	private FrameView singleFrameView;

    public SelectionView(GraphWrapper graphWrapper) throws MIFException {
		panel = Box.createVerticalBox();
		tabPane = new JTabbedPane();
		imageDetailsView = new ImageDetailsView(graphWrapper);
		videoDetailsView = new VideoDetailsView(graphWrapper);
		audioDetailsView = new AudioDetailsView(graphWrapper);
		textDetailsView = new TextDetailsView(graphWrapper);
		
		Melt melt = new Melt();
		List<MeltFilterDetails> audioFilters = graphWrapper.getService().getMeltAudioFilterDetails(melt);
		List<MeltFilterDetails> imageAndVideoFilters = graphWrapper.getService().getMeltVideoFilterDetails(melt);
		filterViewVideo = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewImage = new FilterView(graphWrapper, imageAndVideoFilters);
		filterViewAudio = new FilterView(graphWrapper, audioFilters);
		
		imageView = new ImageView(graphWrapper);
		
		videoView = new VideoView();
		JPanel videoPanel = videoView.getPanel();
		
		singleFrameView = new FrameView();
		graphWrapper.addSingleFrameCreatedListener(singleFrameView);
		Box singleFrameBox = singleFrameView.getBox();
		
		textView = new TextView();
		JPanel textPanel = textView.getPanel();
		
		tabPane.addTab("ImageView", wrap(imageDetailsView.getBox(), imageView.getJPanel(),       filterViewImage.getJPanel()));
		tabPane.addTab("VideoView", wrap(videoDetailsView.getBox(), videoPanel,                  filterViewVideo.getJPanel()));
		tabPane.addTab("TextView",  wrap(textDetailsView.getBox(),  textPanel,                   null));
		tabPane.addTab("AudioView", wrap(audioDetailsView.getBox(), filterViewAudio.getJPanel(), null));
		tabPane.addTab("FrameView", wrap(null,                      singleFrameBox,              null));
		tabPane.addTab("ImageLibrary", wrap(new ImageLibraryView(graphWrapper).getBox(), null, null));
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
		tabPane.setSelectedIndex(4);
		tabPane.setEnabledAt(0, false); // Image
		tabPane.setEnabledAt(1, false); // Video
		tabPane.setEnabledAt(2, false); // Text
		tabPane.setEnabledAt(3, false); // Audio
		tabPane.setEnabledAt(4, true); // Frame
		tabPane.setEnabledAt(5, true);  // ImageLibrary
		
		clearSelection();
	}
	
	public void updateAudio(mxCell cell, MIFAudioFileWrapper audioFile, GraphWrapper project) {
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
		filterViewAudio.setDetails(audioFile);
		
		panel.validate();
		panel.updateUI();
	}
	
	public void updateText(mxCell cell, MIFTextFileWrapper meltFile) {
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
	
	public void updateImageOrVideo(mxCell cell, MIFFileWrapper<?> meltFile) {
		clearSelection();
		
		this.selectedMeltFile = meltFile;
		this.selectedCell = cell;		
		logger.trace("Update {}", meltFile.getClass());
		
		if (meltFile instanceof MIFImageWrapper) {
			tabPane.setSelectedIndex(0);
			tabPane.setEnabledAt(0, true);
			tabPane.setEnabledAt(1, false);
			tabPane.setEnabledAt(2, false);
			tabPane.setEnabledAt(3, false);
			
			imageView.setPreviewPicture(((MIFImageWrapper)meltFile).getImagePreview());
			imageView.update(cell, (MIFImageWrapper)meltFile);
			imageDetailsView.setDetails(meltFile);
			filterViewImage.setDetails(meltFile);
		} else if (meltFile instanceof MIFVideoWrapper) {
			tabPane.setSelectedIndex(1);
			tabPane.setEnabledAt(0, false);
			tabPane.setEnabledAt(1, true);
			tabPane.setEnabledAt(2, false);
			tabPane.setEnabledAt(3, false);

			
			videoView.setIcons(((MIFVideoWrapper)meltFile).getPreviewImages());
			videoDetailsView.setDetails( ((MIFVideoWrapper)meltFile));
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
	
	public MIFFileWrapper<?> getCurrentMeltFile() {
		return selectedMeltFile;
	}

	public MIFAudioFileWrapper getCurrentAudioFile() {
		return selectedAudioFile;
	}
	
	public mxCell getCell() {
		return selectedCell;
	}

	public MIFTextFileWrapper getCurrentTextFile() {
		return selectedTextFile;
	}
}

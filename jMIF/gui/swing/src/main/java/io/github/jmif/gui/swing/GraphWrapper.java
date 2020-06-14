package io.github.jmif.gui.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import io.github.jmif.config.Configuration;
import io.github.jmif.core.LocalService;
import io.github.jmif.core.MIFException;
import io.github.jmif.core.MIFService;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.gui.swing.listener.ProjectListener;
import io.github.jmif.gui.swing.listener.ProjectListener.type;
import io.github.jmif.gui.swing.listener.SingleFrameCreatedListener;
import io.github.jmif.util.TimeUtil;

public class GraphWrapper {
	private static final Logger logger = LoggerFactory.getLogger(GraphWrapper.class);
	
	private final MIFService service = new LocalService();
	
	private Map<mxCell, MIFFile> nodeToMIFFile;
	private Map<mxCell, MIFAudioFile> nodeToMIFAudio;
	private Map<mxCell, MIFTextFile> nodeToMIFText;

	private List<ProjectListener> listenerProjectChanged;
	private List<SingleFrameCreatedListener> listenerSingleFrameCreated;

	private static final int XOFFSET = 10;
	private static final int YOFFSET = 10;
	private int currentLength = 0;

	
	private mxGraph graph;
	private mxGraphComponent graphComponent;
	private Object parent;

	private mxCell singleframeSlider;
	private MIFProject pr;
	
	public GraphWrapper() {
		pr = new MIFProject();
		this.nodeToMIFFile = new LinkedHashMap<>();
		this.nodeToMIFAudio = new LinkedHashMap<>();
		this.nodeToMIFText = new LinkedHashMap<>();
		
		// Listener
		this.listenerProjectChanged = new ArrayList<>();
		this.listenerSingleFrameCreated = new ArrayList<>();
		
		mxSwingConstants.EDGE_SELECTION_COLOR = null;
		initGraph();
	}

	public MIFProject getPr() {
		return this.pr;
	}
	
	public void save() {
		try {
			long time = System.currentTimeMillis();
			
			var file = new File(pr.getFileOfProject());
			var context = JAXBContext.newInstance(MIFProject.class);
			var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(pr, file);

			logger.info("Successfully saved project {} in {}", pr.getFileOfProject(), TimeUtil.getMessage(time));
		} catch (JAXBException ex) {
			logger.error("Unable to save project", ex);
		}
	}

	public void load() throws MIFException, InterruptedException, IOException {
		try {
			long time = System.currentTimeMillis();
			
			var file = new File(pr.getFileOfProject());
			var context = JAXBContext.newInstance(MIFProject.class);
			var unmarshaller = context.createUnmarshaller();

			MIFProject project = (MIFProject) unmarshaller.unmarshal(file);
			getCells().clear();
			getTextCells().clear();
			pr.getMIFFiles().clear();
			pr.getAudiotrack().getAudiofiles().clear();
			pr.getTexttrack().getEntries().clear();
			
			// TODO Add Audio or Text????
			
			var executor = Executors.newWorkStealingPool();
			for (MIFFile f : project.getMIFFiles()) {
				MIFFile mifFile = createMIFFile(f.getFile());
				mifFile.getFilters().addAll(f.getFilters());
				
				executor.submit(() -> {
					try {
						service.createPreview(mifFile, project.getWorkingDir());
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
			
			redrawGraph();
			createFramePreview();
			
			informListeners(type.LOAD_PROJECT);
			
			logger.info("Successfully loaded project {} in {}", pr.getFileOfProject(), TimeUtil.getMessage(time));
		} catch (JAXBException ex) {
			logger.info("Unable to load project {}. Exception: ", pr.getFileOfProject(), ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	public void informListeners(type t) {
		listenerProjectChanged.stream().forEach(c -> c.projectChanged(t));
	}
	
	public MIFAudioFile createMIFAudioFile(File fileToAdd) throws MIFException {
		var file = fileToAdd.getAbsoluteFile().getAbsolutePath();
		
		if (file.endsWith("mp3") || file.endsWith("MP3")) {
			// TODO Service
			var audioFile = service.createAudio(fileToAdd.getAbsolutePath());
			
			var n = "mp3";
			var x = currentLength + XOFFSET;
			var y = YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight;
			var w = getPixelwidth(audioFile);
			var h = Configuration.timelineentryHeight;
					
			put(audioFile, createVertex(n, x, y, w, h));
			
			return audioFile;
		}
		return null;
	}
	
	public MIFTextFile createMIFTextfile() throws MIFException {
		MIFTextFile textFile = service.createText();

		var n = "text";
		var x = currentLength + XOFFSET;
		var y = YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight;
		var w = getPixelwidth(textFile);
		var h = Configuration.timelineentryHeight;
				
		put(textFile, createVertex(n, x, y, w, h));
		
		return textFile;
	}
	
	public MIFFile createMIFFile(File fileToAdd) throws MIFException, InterruptedException, IOException {
		logger.info("Create node for {}", fileToAdd);
		
		var file = fileToAdd.getAbsoluteFile().getAbsolutePath();
		var extension = FilenameUtils.getExtension(file);

		MIFFile mifFile = null;
		
		var display = file.substring(file.lastIndexOf('/') + 1);
		if (Configuration.allowedImageTypes.contains(extension)) {
			mifFile = service.createImage(fileToAdd, display, 5000, "-1x-1", 1000, pr.getWorkingDir());
		} else if (Configuration.allowedVideoTypes.contains(extension)) {
			mifFile = service.createVideo(fileToAdd, display, -1, "1920x1080", 1000, pr.getWorkingDir());
		}
		
		if (mifFile != null) {
			if (!pr.getMIFFiles().isEmpty()) {
				currentLength -= (mifFile.getOverlayToPrevious() / 1000d) * 25;
			}

			var n = mifFile.getDisplayName();
			var x = currentLength + XOFFSET;
			var y = pr.getMIFFiles().size() % 2 == 0 ? 0 + YOFFSET : Configuration.timelineentryHeight + YOFFSET;
			var w = getPixelwidth(mifFile);
			var h = Configuration.timelineentryHeight;
			
			put(mifFile, createVertex(n, x, y, w, h));
			
			currentLength += w;
			
			return mifFile;
		}			
		
		return null;
	}
	
	public void initializeProject() {
		int current = 0;
		for (MIFFile file : pr.getMIFFiles()) {
			logger.info("Adding file {}", file.getFile());

			if (current > 0) {
				currentLength -= getOverlaywidth(file);
			}
			
			var n = file.getDisplayName();
			var x = currentLength + XOFFSET;
			var y = current % 2 == 0 ? 0 + YOFFSET : Configuration.timelineentryHeight + YOFFSET;
			var w = getPixelwidth(file);
			var h = Configuration.timelineentryHeight;;

			put(file, createVertex(n, x, y, w, h));
			
			currentLength += w;
			current++;
		}
		int audioLength = 0;
		current = 0;
		for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
			current++;
			
			mxCell mxCell = audioEntry.getKey();
			MIFAudioFile audioFile = audioEntry.getValue();
			
			var x = XOFFSET + audioLength;
			var y = YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight;
			var w = getPixelwidth(audioFile);
			var h = Configuration.timelineentryHeight;
			if (current % 2 == 0) {
				y += + Configuration.timelineentryHeight;
			}
			
			audioLength += w;
			resize(mxCell, new mxRectangle(x, y, w, h));
		}
		
		int textLength = 0;
		current = 0;
		for (Entry<mxCell, MIFTextFile> textEntry : nodeToMIFText.entrySet()) {
			current++;
			
			mxCell mxCell = textEntry.getKey();
			MIFTextFile textFile = textEntry.getValue();
			
			var x = XOFFSET + textLength;
			var y = YOFFSET*4 +YOFFSET/2 + 4*Configuration.timelineentryHeight;
			var w = getPixelwidth(textFile);
			var h = Configuration.timelineentryHeight;
			if (current % 2 == 0) {
				y += + Configuration.timelineentryHeight;
			}
			
			textLength += w;
			resize(mxCell, new mxRectangle(x, y, w, h));
		}
		
		createFramePreview();
	}
	
	public void redrawGraph() {
		var current = 0;
		currentLength = 0;
		for (mxCell c : getCells()) {

			var file = get(c);

			c.setValue(file.getDisplayName());

			if (current > 0) {
				currentLength -= getOverlaywidth(file);
			}

			// Erklären warum folgende Methode scheisse ist (benutzt preferredisze)
//    		graph.updateCellSize(c);

			var x = currentLength + XOFFSET;
			var y = current % 2 == 0 ? 0 + YOFFSET : Configuration.timelineentryHeight + YOFFSET;
			var w = getPixelwidth(file);
			var h = Configuration.timelineentryHeight;
			
			resize(c, new mxRectangle(x, y, w, h));

			currentLength += w;
			current++;
		}
		int audioLength = 0;
		current = 0;
		for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
			current++;
			
			mxCell mxCell = audioEntry.getKey();
			MIFAudioFile audioFile = audioEntry.getValue();
			
			if (current > 1) {
				audioLength -= getOverlaywidth(audioFile);
			}
			
			var x = XOFFSET + audioLength;
			var y = YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight;
			if (current % 2 == 0) {
				y += Configuration.timelineentryHeight;
			}
			var w = getPixelwidth(audioFile);
			var h = Configuration.timelineentryHeight;
			
			audioLength += w;
			resize(mxCell, new mxRectangle(x, y, w, h));
		}
		
		int textLength = 0;
		current = 0;
		for (Entry<mxCell, MIFTextFile> textEntry : nodeToMIFText.entrySet()) {
			current++;
			
			mxCell mxCell = textEntry.getKey();
			MIFTextFile textFile = textEntry.getValue();
			
			if (current > 1) {
				textLength -= getOverlaywidth(textFile);
			}
			
			var x = XOFFSET + textLength;
			var y = YOFFSET*4 +YOFFSET/2 + 4*Configuration.timelineentryHeight;
			if (current % 2 == 0) {
				y += Configuration.timelineentryHeight;
			}
			var w = getPixelwidth(textFile);
			var h = Configuration.timelineentryHeight;
			
			textLength += w;
			resize(mxCell, new mxRectangle(x, y, w, h));
			
		}
		
		this.graphComponent.updateUI();
	}
	
	@SuppressWarnings("serial")
	private void initGraph() {
		graph = new mxGraph() {
			@Override
			public boolean isCellSelectable(Object cell) {
				mxCell c = (mxCell) cell;
				if (c != null && c.isVertex()) {
					return get(c) != null || c == singleframeSlider || getAudio(c) != null || getText(c) != null;
				}
				return false;
			}

			// Only the preview-frame-slider should be move-able
			@Override
			public boolean isCellMovable(Object cell) {
				return ((mxCell)cell) == singleframeSlider;
			}
		};
		graphComponent = new mxGraphComponent(graph) {
			@Override
			protected mxGraphControl createGraphControl() {
				return new mxGraphControl() {
					@Override
					public void paintComponent(Graphics g) {
						if (!pr.getMIFFiles().isEmpty() || !pr.getAudiotrack().getAudiofiles().isEmpty()) {
							// 1. Timeline
							int length = 0;
							int current = 0;
							for (MIFFile file : pr.getMIFFiles()) {
								if (current > 0) {
									length -= getOverlaywidth(file);
								}
								length += getPixelwidth(file);
								current++;
							}
							int audioLength = 0;
							current = 0;
							for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
								if (current > 0) {
									audioLength -= getOverlaywidth(audioEntry.getValue());
								}
								audioLength += getPixelwidth(audioEntry.getValue());
								current++;
							}
							int textLength = 0;
							current = 0;
							for (Entry<mxCell, MIFTextFile> textEntry : nodeToMIFText.entrySet()) {
								if (current > 0) {
									textLength -= getOverlaywidth(textEntry.getValue());
								}
								textLength += getPixelwidth(textEntry.getValue());
								current++;
							}

							int max1 = Math.max(length, audioLength) ;
							int w = Math.max(max1, textLength) + XOFFSET/2;
							
							g.setColor(Color.BLUE);
							
							int y = 145; // =  6 * height + 2 offset (1 for image->text, 1 for text-> audio) + ? 
							g.drawLine(0, y, w, y);
							
							// 2. tracks
							
							var xOffsetTrack = 5;
							var yOffsetTrack = 5;
							var height = Configuration.timelineentryHeight;
							g.setColor(new Color(237,	237, 	237  ));
							g.fillRect(xOffsetTrack, yOffsetTrack,         w + xOffsetTrack, YOFFSET + 2*height); // Track 0
							g.fillRect(xOffsetTrack, YOFFSET*2 + 2*height, w + xOffsetTrack, YOFFSET + 2*height); // Track 1
							g.fillRect(xOffsetTrack, YOFFSET*4 + 4*height, w + xOffsetTrack, YOFFSET + 2*height); // Track 1
							
							
							// 3. 
							g.setColor(Color.DARK_GRAY);
							for (int i = 1; i <= w; i++) {
								// e.g. all 25 or 50 pixels
								if (i % Configuration.pixelwidth_per_second == 0) {
									g.drawLine(i+XOFFSET, 50+YOFFSET, i+XOFFSET, 45+YOFFSET);
								}
								if (i % 125 == 0) {
							        //creates a copy of the Graphics instance
							        Graphics2D g2d = (Graphics2D) g.create();

							        float dash2[] = {5f, 1f, 1f};
							        BasicStroke bs2 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash2, 2f);
							        
							        g2d.setStroke(bs2);
							        int x1 = i+XOFFSET;
							        int y1 = 200+YOFFSET;
							        int x2 = i+XOFFSET;
							        int y2 = 5;
									g2d.drawLine(x1, y1, x2, y2);
									
									String t = String.valueOf(i / Configuration.pixelwidth_per_second)+"s";
									if ((i/Configuration.pixelwidth_per_second) >= 60) {
										t = i/60+"m "+i%60+"s";
									}
											
									g.drawString(t, i + XOFFSET/2, 130);
									g.drawString(String.valueOf(i*pr.getProfileFramerate()/Configuration.pixelwidth_per_second), i + XOFFSET/2, 145); // framenumber
								}
							}
						}
						
						super.paintComponent(g);
					}
				};
			}
		};
		
		if (Configuration.useBorders) {
			graphComponent.setBorder(BorderFactory.createLineBorder(Color.RED));
		}
		
		graph.addListener(mxEvent.CELLS_MOVED, (sender, evt) -> {
			Object[] c = (Object[])evt.getProperties().get("cells");
			
			if (c[0] == singleframeSlider) {
				mxGeometry bounds = graph.getModel().getGeometry(c[0]);
				bounds.setY(0);
				graph.getModel().setGeometry(c[0], bounds); // don't allow move on y-axis
				
				 final int x = (int)bounds.getCenterX() - 10;

				 SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					@Override
				    public Void doInBackground() {
						exportFrame(x);
						return null;
				    }
				 };
				 worker.execute();
			}
		});
		// TODO UI: allow move framePreview by right/left.. maybe mxKeyboardHandler?
		
		graph.setCellsEditable(false); // Want to edit the value of a cell in the graph?
		graph.setCellsResizable(false); // Inhibit cell re-sizing.

		parent = graph.getDefaultParent();
		graphComponent.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		
		graphComponent.setConnectable(false); // Inhibit edge creation in the graph.
		
		graphComponent.getViewport().setOpaque(true);
		graphComponent.getViewport().setBackground(Configuration.bgColor);
		
		graphComponent.setMinimumSize(new Dimension(10, 180)); // xdim ignore
	}

	private void exportFrame(int frame) {
		try {
			var time = System.currentTimeMillis();
	    		
			// TODO Use temp dir...
			var output = pr.getWorkingDir()+"frame-"+frame+".jpg";
			service.exportImage(pr, output, frame);
	    		
			logger.info("Created image for frame {} in {}", frame, TimeUtil.getMessage(time));
			
			for (SingleFrameCreatedListener listener : listenerSingleFrameCreated ) {
				listener.created(output);
			}
		} catch (MIFException e) {
			logger.error("Unable to create single frame", e);
		}
	}

	public void createFramePreview() {
		if (singleframeSlider == null) {
			singleframeSlider = (mxCell) graph.insertVertex(parent, null, "", 50, 0, 10, 120);
		}
	}
	
	private mxCell createVertex(String label, int x, int y, float w, int h) {
		return (mxCell) graph.insertVertex(parent, null, label, x, y, w, h);
	}

	private int getPixelwidth(MIFAudioFile audioFile) {
		return (int)(Configuration.pixelwidth_per_second * ((audioFile.getEncodeEnde() - audioFile.getEncodeStart()) / 1000d));
	}
	
	private int getPixelwidth(MIFTextFile textFile) {
		return (int)(Configuration.pixelwidth_per_second * (textFile.getLength() / 1000d));
	}
	
	private int getPixelwidth(MIFFile mifFile) {
		return (int)(Configuration.pixelwidth_per_second  * (mifFile.getDuration() / 1000d));
	}
	
	private int getOverlaywidth(MIFFile mifFile) {
		return (int)(Configuration.pixelwidth_per_second * (mifFile.getOverlayToPrevious() / 1000d));
	}
	
	private int getOverlaywidth(MIFTextFile textfield) {
		return 0; // TODO Text: Overlay
	}
	
	private int getOverlaywidth(MIFAudioFile audioFiles) {
		return 25;
	}
	
	public void remove(mxCell cell) {
		graph.removeCells(new Object[] { cell });
	}
	
	private void resize(mxCell cell, mxRectangle rectangle) {
		graph.cellsResized(new Object[] { cell }, new mxRectangle[] { rectangle });
	}
	
	public mxGraph getGraph() {
		return graph;
	}

	public mxGraphComponent getGraphComponent() {
		return graphComponent;
	}
	
	public void addProjectListener(ProjectListener c) {
		this.listenerProjectChanged.add(c);
	}
	
	public void addSingleFrameCreatedListener(SingleFrameCreatedListener listener) {
		this.listenerSingleFrameCreated.add(listener);
	}
	
	public void remove(MIFFile meltFile, mxCell cell) {
		pr.getMIFFiles().remove(meltFile);
		nodeToMIFFile.remove(cell);
	}

	public void remove(MIFTextFile meltFile, mxCell cell) {
		pr.getTexttrack().getEntries().remove(meltFile);
		nodeToMIFFile.remove(cell);
	}
	
	public void put(MIFFile meltFile, mxCell cell) {
		pr.getMIFFiles().add(meltFile);
		this.nodeToMIFFile.put(cell, meltFile);
	}
	
	public void put(MIFTextFile textFile, mxCell cell) {
		pr.getTexttrack().getEntries().add(textFile);
		nodeToMIFText.put(cell, textFile);
	}

	public MIFFile get(mxCell cell) {
		return this.nodeToMIFFile.get(cell);
	}

	public List<mxCell> getCells() {
		return new ArrayList<>(this.nodeToMIFFile.keySet());
	}
	
	public void remove(MIFAudioFile audioFile, mxCell cell) {
		pr.getAudiotrack().getAudiofiles().remove(audioFile);
		nodeToMIFAudio.remove(cell);
	}

	public void put(MIFAudioFile audioFile, mxCell cell) {
		pr.getAudiotrack().getAudiofiles().add(audioFile);
		this.nodeToMIFAudio.put(cell, audioFile);
	}

	public MIFAudioFile getAudio(mxCell cell) {
		return this.nodeToMIFAudio.get(cell);
	}
	
	public MIFTextFile getText(mxCell cell) {
		return this.nodeToMIFText.get(cell);
	}

	public List<mxCell> getAudioCells() {
		return new ArrayList<>(this.nodeToMIFAudio.keySet());
	}
	
	public List<mxCell> getTextCells() {
		return new ArrayList<>(this.nodeToMIFText.keySet());
	}

	public boolean isSingleFrameNode(mxCell cell) {
		return cell == singleframeSlider;
	}

	public MIFService getService() {
		return service;
	}
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.view.mxGraph;

import io.github.jmif.LocalService;
import io.github.jmif.MIFException;
import io.github.jmif.MIFService;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.gui.swing.listener.ProjectListener;
import io.github.jmif.gui.swing.listener.ProjectListener.type;
import io.github.jmif.gui.swing.listener.SingleFrameCreatedListener;
import io.github.jmif.util.TimeUtil;

public class GraphWrapper {
	private static final Logger logger = LoggerFactory.getLogger(GraphWrapper.class);
	
	private final MIFService service = new LocalService();
	
	private Map<mxCell, MIFFile> nodeToMIFFile;
	private Map<mxCell, MIFAudioFile> nodeToMIFAudio;

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
			pr.getMIFFiles().clear();
			
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
			
			var v1 = insertVertex(
				"mp3",
				XOFFSET,
				YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight,
				25*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()),
				20);
			put(audioFile, v1);
			
			return audioFile;
		}
		return null;
	}
	
	public MIFFile createMIFFile(File fileToAdd) throws MIFException, InterruptedException, IOException {
		logger.info("Create node for {}", fileToAdd);
		
		var file = fileToAdd.getAbsoluteFile().getAbsolutePath();
		var extension = file.substring(file.lastIndexOf(".")+1);

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

			int x = currentLength + XOFFSET;
			int y = pr.getMIFFiles().size() % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET;
			int w = (int)(mifFile.getDuration()*25 / 1000d);
			int h = 20;
			var v1 = insertVertex(mifFile.getDisplayName(), x, y, w, h);
			
			put(mifFile, v1);
			
			currentLength += (mifFile.getDuration() / 1000d) * 25;
			
			return mifFile;
		}			
		
		return null;
	}
	
	public void initializeProject() {
		int current = 0;
		for (MIFFile file : pr.getMIFFiles()) {
			logger.info("Adding file {}", file.getFile());

			if (current > 0) {
				currentLength -= (file.getOverlayToPrevious() / 1000d);
			}
			int x = currentLength + XOFFSET;
			int y = current % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET;
			mxCell v1 = insertVertex(x, y, file);
			put(file, v1);
			currentLength += (file.getDuration() / 1000d);
			current++;
		}
		int audioLength = 0;
		current = 0;
		for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
			current++;
			
			mxCell mxCell = audioEntry.getKey();
			MIFAudioFile audioFile = audioEntry.getValue();
			
			var rec = new mxRectangle();
			rec.setX(XOFFSET + audioLength);
			rec.setY(YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight);
			if (current % 2 == 0) {
				rec.setY(rec.getY() + 20);
			}
			rec.setWidth(25*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()));
			audioLength += 25*(audioFile.getEncodeEnde()-audioFile.getEncodeStart());
			rec.setHeight(20);
			resize(mxCell, rec);
		}
		
		createFramePreview();
	}
	
	public void redrawGraph() {
		var current = 0;
		currentLength = 0;
		for (mxCell c : getCells()) {

			var file = get(c);

			c.setValue(file.getDisplayName());

			var frames = (file.getDuration() / 1000d) * 25;
			var overlayInFrames = (file.getOverlayToPrevious() / 1000d) * 25;
			if (current > 0) {
				currentLength -= overlayInFrames;
			}

			// Erklären warum folgende Methode scheisse ist (benutzt preferredisze)
//    		graph.updateCellSize(c);

			var rec = new mxRectangle();
			rec.setX(currentLength + XOFFSET);
			rec.setY(current % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET);
			rec.setHeight(20);
			rec.setWidth(frames);
			resize(c, rec);
			currentLength += frames;
			current++;
		}
		int audioLength = 0;
		current = 0;
		for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
			current++;
			
			mxCell mxCell = audioEntry.getKey();
			MIFAudioFile audioFile = audioEntry.getValue();
			
			if (current > 1) {
				audioLength -= 25; // FIXME Allow Overlay in milliseconds
			}
			
			var rec = new mxRectangle();
			rec.setX(XOFFSET + audioLength);
			rec.setY(YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight);
			if (current % 2 == 0) {
				rec.setY(rec.getY() + 20);
			}
			rec.setWidth(25*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()));
			audioLength += 25*(audioFile.getEncodeEnde()-audioFile.getEncodeStart());
			rec.setHeight(20);
			resize(mxCell, rec);
		}
		
		this.graphComponent.updateUI();
	}
	
	public mxCell insertVertex(int x, int y, MIFFile meltFile) {
		return (mxCell)graph.insertVertex(
			parent,
			null,
			meltFile.getDisplayName(), 
			x,
			y, 
			(meltFile.getDuration() / 1000d), 
			Configuration.timelineentryHeight);
	}
	
	@SuppressWarnings("serial")
	private void initGraph() {
		graph = new mxGraph() {
			@Override
			public boolean isCellSelectable(Object cell) {
				mxCell c = (mxCell) cell;
				if (c != null && c.isVertex()) {
					return get(c) != null || c == singleframeSlider || getAudio(c) != null;
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
									length -= (file.getOverlayToPrevious() / 1000d) * 25;
								}
								length += (file.getDuration() / 1000d) * 25;
								current++;
							}
							int audioLength = 0;
							current = 0;
							for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
								if (current > 0) {
									audioLength -= 25;
								}
								audioLength += 25*(audioEntry.getValue().getEncodeEnde() - audioEntry.getValue().getEncodeStart());
								current++;
							}
							
							int w = Math.max(length, audioLength) + XOFFSET/2;
							
							g.setColor(Color.BLUE);
							
							int y = 115;
							g.drawLine(0, y, w, y);
							
							// 2. tracks
							
							var xOffsetTrack = 5;
							var yOffsetTrack = 5;
							var height = Configuration.timelineentryHeight;
							g.setColor(new Color(237,	237, 	237  ));
							g.fillRect(xOffsetTrack, yOffsetTrack,         w + xOffsetTrack, YOFFSET + 2*height); // Track 0
							g.fillRect(xOffsetTrack, YOFFSET*2 + 2*height, w + xOffsetTrack, YOFFSET + 2*height); // Track 1
							
							
							// 3. 
							g.setColor(Color.DARK_GRAY);
							for (int i = 1; i <= w; i++) {
								// e.g. all 25 or 50 pixels
								if (i % 25 == 0) {
									g.drawLine(i+XOFFSET, 50+YOFFSET, i+XOFFSET, 45+YOFFSET);
								}
								if (i % 125 == 0) {
							        //creates a copy of the Graphics instance
							        Graphics2D g2d = (Graphics2D) g.create();

							        float dash2[] = {5f, 1f, 1f};
							        BasicStroke bs2 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash2, 2f);
							        
							        g2d.setStroke(bs2);
							        int x1 = i+XOFFSET;
							        int y1 = 100+YOFFSET;
							        int x2 = i+XOFFSET;
							        int y2 = 5;
									g2d.drawLine(x1, y1, x2, y2);
									
									String t = String.valueOf(i / 25)+"s";
									if ((i/25) >= 60) {
										t = i/60+"m "+i%60+"s";
									}
											
									g.drawString(t, i + XOFFSET/2, 130);
									g.drawString(String.valueOf(i*pr.getFramerate()/25), i + XOFFSET/2, 145); // framenumber
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
	
	private mxCell insertVertex(String label, int x, int y, float w, int h) {
		return (mxCell) graph.insertVertex(parent, null, label, x, y, w, h);
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

	public void put(MIFFile meltFile, mxCell cell) {
		pr.getMIFFiles().add(meltFile);
		this.nodeToMIFFile.put(cell, meltFile);
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

	public List<mxCell> getAudioCells() {
		return new ArrayList<>(this.nodeToMIFAudio.keySet());
	}

	public boolean isSingleFrameNode(mxCell cell) {
		return cell == singleframeSlider;
	}

	public MIFService getService() {
		return service;
	}
}

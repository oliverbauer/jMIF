package io.github.jmif.data;

import java.awt.BasicStroke;
import java.awt.Color;
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

import io.github.jmif.JMIFException;
import io.github.jmif.Service;
import io.github.jmif.config.Configuration;
import io.github.jmif.data.listener.ProjectListener;
import io.github.jmif.data.listener.ProjectListener.type;
import io.github.jmif.data.listener.SingleFrameCreatedListener;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.util.TimeUtil;

public class GraphWrapper {
	private static final Logger logger = LoggerFactory.getLogger(GraphWrapper.class);
	
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

	private mxCell framePreview;
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

	public void load() throws InterruptedException, IOException {
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
				MIFFile mifFile = createMIFFile(new File(f.getFile()));
				executor.submit(mifFile.getBackgroundRunnable(project.getWorkingDir()));
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
	
	/**
	 * cp's the file to 'orig' and calls {@link MIFFile#init}. 
	 * 
	 * @param file
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	private void initialize(MIFFile file) throws InterruptedException, IOException {
		var target = pr.getWorkingDir()+"orig/"+file.getFilename();
		var command = "cp "+file.getFile()+" "+target;
		
		// Copy to working dir... make sure each file has different name...
		if (!new File(target).exists()) {
			logger.info("Copy to {} ({})", file.getFilename(), command);
			new ProcessBuilder("bash", "-c", command)
				.directory(new File(pr.getWorkingDir()))
				.redirectErrorStream(true)
				.start()
				.waitFor();
		}
		
		file.init(pr.getWorkingDir(), pr.getFramerate());
	}
	
	public MIFAudioFile createMIFAudioFile(File fileToAdd) {
		var file = fileToAdd.getAbsoluteFile().getAbsolutePath();
		
		if (file.endsWith("mp3") || file.endsWith("MP3")) {
			var audioFile = new MIFAudioFile();
			audioFile.setAudiofile(fileToAdd.getAbsolutePath());
			audioFile.setEncodeStart(0);
			audioFile.setEncodeEnde(audioFile.getLengthOfInput());
			
			var v1 = insertVertex(
				"mp3",
				XOFFSET,
				YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight,
				pr.getFramerate()*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()),
				20);
			put(audioFile, v1);
			
			return audioFile;
		}
		return null;
	}
	
	public MIFFile createMIFFile(File fileToAdd) throws InterruptedException, IOException {
		logger.info("Create node for {}", fileToAdd);
		
		var file = fileToAdd.getAbsoluteFile().getAbsolutePath();
		
		if (file.endsWith("JPG") || file.endsWith("jpg")) {
			var display = file.substring(file.lastIndexOf('/') + 1);

			var image = new MIFImage(file, display, 5*pr.getFramerate(), "-1x-1", pr.getFramerate());
			initialize(image);

			if (!pr.getMIFFiles().isEmpty()) {
				currentLength -= image.getOverlayToPrevious();
			}
			
			var v1 = insertVertex(
				image.getDisplayName(),
				currentLength + XOFFSET,
				pr.getMIFFiles().size() % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET,
				image.getFramelength(), 
				20);
			put(image, v1);
			
			currentLength += image.getFramelength();
			
			return image;
		} else if (file.endsWith("mp4") || file.endsWith("MP4")) {
			var display = file.substring(file.lastIndexOf('/') + 1);
			var video = new MIFVideo(file, display, -1, "1920x1080", pr.getFramerate());
			initialize(video);

			if (!pr.getMIFFiles().isEmpty()) {
				currentLength -= video.getOverlayToPrevious();
			}
			
			var v1 = insertVertex(
				video.getDisplayName(),
				currentLength + XOFFSET,
				pr.getMIFFiles().size() % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET,
				video.getFramelength(), 
				20);	
			put(video, v1);
			
			currentLength += video.getFramelength();
			
			return video;
		}
		
		return null;
	}
	
	public void initializeProject() {
		int current = 0;
		for (MIFFile file : pr.getMIFFiles()) {
			logger.info("Adding file {}", file.getFile());

			if (current > 0) {
				currentLength -= file.getOverlayToPrevious();
			}
			int x = currentLength + XOFFSET;
			int y = current % 2 == 0 ? 0 + YOFFSET : 20 + YOFFSET;
			mxCell v1 = insertVertex(x, y, file);
			put(file, v1);
			currentLength += file.getFramelength();
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
			rec.setWidth(pr.getFramerate()*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()));
			audioLength += pr.getFramerate()*(audioFile.getEncodeEnde()-audioFile.getEncodeStart());
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

			var frames = file.getFramelength();
			var overlayInFrames = file.getOverlayToPrevious();
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
				audioLength -= pr.getFramerate();
			}
			
			var rec = new mxRectangle();
			rec.setX(XOFFSET + audioLength);
			rec.setY(YOFFSET*2 +YOFFSET/2 + 2*Configuration.timelineentryHeight);
			if (current % 2 == 0) {
				rec.setY(rec.getY() + 20);
			}
			rec.setWidth(pr.getFramerate()*(audioFile.getEncodeEnde()-audioFile.getEncodeStart()));
			audioLength += pr.getFramerate()*(audioFile.getEncodeEnde()-audioFile.getEncodeStart());
			rec.setHeight(20);
			resize(mxCell, rec);
		}
	}
	
	public mxCell insertVertex(int x, int y, MIFFile meltFile) {
		return (mxCell)graph.insertVertex(
			parent,
			null,
			meltFile.getDisplayName(), 
			x,
			y, 
			meltFile.getFramelength(), 
			Configuration.timelineentryHeight);
	}
	
	@SuppressWarnings("serial")
	private void initGraph() {
		graph = new mxGraph() {
			@Override
			public boolean isCellSelectable(Object cell) {
				mxCell c = (mxCell) cell;
				if (c != null && c.isVertex()) {
					return get(c) != null || c == framePreview || getAudio(c) != null;
				}
				return false;
			}

			// Only the preview-frame-slider should be move-able
			@Override
			public boolean isCellMovable(Object cell) {
				return ((mxCell)cell) == framePreview;
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
									length -= file.getOverlayToPrevious();
								}
								length += file.getFramelength();
								current++;
							}
							int audioLength = 0;
							current = 0;
							for (Entry<mxCell, MIFAudioFile> audioEntry : nodeToMIFAudio.entrySet()) {
								if (current > 0) {
									audioLength -= pr.getFramerate();
								}
								audioLength += pr.getFramerate()*(audioEntry.getValue().getEncodeEnde() - audioEntry.getValue().getEncodeStart());
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
							g.fillRect(xOffsetTrack, yOffsetTrack, w + xOffsetTrack, YOFFSET + 2*height); // Track 0
							g.fillRect(xOffsetTrack, YOFFSET*2 + 2*height, w + xOffsetTrack, YOFFSET + 2*height); // Track 1
							
							
							// 3. 
							g.setColor(Color.DARK_GRAY);
							for (int i = 1; i <= w / pr.getFramerate(); i++) {
								if (i % 5 == 0) {
							        //creates a copy of the Graphics instance
							        Graphics2D g2d = (Graphics2D) g.create();

							        float dash2[] = {5f, 1f, 1f};
							        BasicStroke bs2 = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, dash2, 2f);
							        
							        g2d.setStroke(bs2);
									g2d.drawLine(i*pr.getFramerate()+XOFFSET, 100+YOFFSET, i*pr.getFramerate()+XOFFSET, 5);
									
									String t = String.valueOf(i)+"s";
									if (i >= 60) {
										t = i/60+"m "+i%60+"s";
									}
											
									g.drawString(t, i*pr.getFramerate() + XOFFSET/2, 130);
									g.drawString(String.valueOf(i*pr.getFramerate()), i*pr.getFramerate() + XOFFSET/2, 145); // framenumber
									
								} else {
									g.drawLine(i*pr.getFramerate()+XOFFSET, 50+YOFFSET, i*pr.getFramerate()+XOFFSET, 45+YOFFSET);
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
			
			if (c[0] == framePreview) {
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
	}

	private void exportFrame(int frame) {
		try {
			var time = System.currentTimeMillis();
	    		
			// TODO Use temp dir...
			var output = pr.getWorkingDir()+"frame-"+frame+".jpg";
			new Service().exportImage(pr, output, frame);
	    		
			logger.info("Created image for frame {} in {}", frame, TimeUtil.getMessage(time));
			
			for (SingleFrameCreatedListener listener : listenerSingleFrameCreated ) {
				listener.created(output);
			}
		} catch (JMIFException e) {
			logger.error("Unable to create single frame", e);
		}
	}

	public void createFramePreview() {
		if (framePreview == null) {
			framePreview = (mxCell) graph.insertVertex(parent, null, "", 50, 0, 10, 120);
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
		return cell == framePreview;
	}
}

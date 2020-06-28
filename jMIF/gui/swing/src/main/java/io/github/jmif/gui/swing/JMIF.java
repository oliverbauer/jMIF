package io.github.jmif.gui.swing;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraphSelectionModel;

import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.gui.swing.config.UserConfig;
import io.github.jmif.gui.swing.graph.GraphView;
import io.github.jmif.gui.swing.listener.ProjectListener;
import io.github.jmif.gui.swing.listener.ProjectListener.type;
import io.github.jmif.gui.swing.menu.MenuView;
import io.github.jmif.gui.swing.selection.SelectionView;
import io.github.jmif.gui.swing.splash.Splashscreen;
import io.github.jmif.util.TimeUtil;

/**
 * jMIF (MLT, IMAGEMAGICK, FFMPEG)
 * 
 * TODO Disable click on node until Thread with preview images finished (otherwise image will not be displayed) 
 * TODO Release profile (bzip2) with example-Dir included 
 * TODO Config-Button (store config in HOME-Dir) 
 * TODO Config: Allow in-memory-preview (byte-arrays of pictures - needs more RAM) or hd-preview (copy files - needs more HD/Time for IO) 
 * TODO Allow multiple files with same name
 */
public class JMIF {
	private static final Logger LOGGER = LoggerFactory.getLogger(JMIF.class);

	private GraphWrapper graphWrapper;
	
	@Inject
	private MenuView mifMenuView;
	
	@Inject
	private GraphView mifGraphView;
	
	@Inject
	private UserConfig userConfig;
	
	@Inject
	private SelectionView mifSelectionView;

	public static void main(String[] args) throws Exception {
		Weld weld = new Weld()
			.property("org.jboss.weld.construction.relaxed", true)
			.property("org.jboss.weld.bootstrap.concurrentDeployment", false);
		
		
		try (WeldContainer container = weld.initialize()) {
			JMIF bean = container.select(JMIF.class).get();
			bean.show();
		}
	}
	
	public void show() throws Exception {
		if (userConfig.isGENERAL_SHOW_SPLASHSCREEN()) {
			var splashscreen = new Splashscreen();
			var worker = splashscreen.getWorker();
			while (worker.getState() != StateValue.DONE) {
				Thread.sleep(10);
			}
		}

		SwingUtilities.invokeLater(() -> {
			try {
				showFrame();
			} catch (MIFException | IOException | InterruptedException e) {
				LOGGER.error("", e);
			}
		});
	}

	public void showFrame() throws MIFException,  IOException, InterruptedException {
		var time = System.currentTimeMillis();

		var frame = new JFrame();
		frame.setUndecorated(true); // remove title bar...
		frame.setTitle("MIF-enizer (MLT/ImageMagickFFMPEG/)");

		// TODO Menu: disbale save-button, preview and gen-button on start...
		graphWrapper = createMIFProject(userConfig.getGENERAL_PROFILE());
		graphWrapper.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> cellClicked(sender));

		var panelBox = Box.createVerticalBox();
		mifMenuView.init(graphWrapper);
		panelBox.add(mifMenuView.getJPanel());

		// Add Project stuff: name, output.file, render-profile
		var horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(new JLabel("Project "+graphWrapper.getPr().getFileOfProject()));
		horizontalBox.add(Box.createHorizontalStrut(20));
		horizontalBox.add(new JLabel("Output "));
		var output = new JTextField();
		output.setText(graphWrapper.getPr().getOutputVideo());
		output.setMaximumSize(new Dimension(300, 30));
		output.setPreferredSize(new Dimension(300, 30));
		horizontalBox.add(output);
		horizontalBox.add(Box.createHorizontalGlue());
		
		var profiles = graphWrapper.getService().getProfiles();
		
		var profilesCombobox = new JComboBox<>(profiles.toArray(new String[profiles.size()]));
		profilesCombobox.setSelectedItem(graphWrapper.getPr().getProfile());
		profilesCombobox.addItemListener(itemEvent -> {
			try {
				var item = (String)profilesCombobox.getSelectedItem();
				graphWrapper.getPr().setProfile(item);
				graphWrapper.getService().updateProfile(graphWrapper.getPr());
				graphWrapper.redrawGraph();
			} catch (MIFException e) {
				LOGGER.error("Unable to change profile", e);
			}
		});
		profilesCombobox.setMaximumSize(new Dimension(300,30));

		horizontalBox.add(profilesCombobox);
		panelBox.add(horizontalBox);
		
		mifSelectionView.init(graphWrapper);
		mifGraphView.init(frame, graphWrapper, mifSelectionView);
		panelBox.add(mifGraphView.getGraphPanel());
		panelBox.add(mifSelectionView.getPanel());

		frame.setLayout(new GridBagLayout());

		frame.setContentPane(panelBox);
		frame.pack();
		frame.setLocationRelativeTo(null);
		
		// Select the first cell
		if (!graphWrapper.getCells().isEmpty()) {
			var cell = graphWrapper.getCells().get(0);
			mifSelectionView.updateImageOrVideo(cell, graphWrapper.get(cell));
		}
		
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setExtendedState(Frame.MAXIMIZED_BOTH); 
		frame.getRootPane().setWindowDecorationStyle(5);

		graphWrapper.save();
		graphWrapper.addProjectListener(createChangeListener());

		LOGGER.info("Initialized jMIF in {}", TimeUtil.getMessage(time));
	}

	private GraphWrapper createMIFProject(String profile) throws MIFException, IOException, InterruptedException {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		project.getPr().setProfile(profile);
		project.getService().updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
		project.getService().createWorkingDirs(project.getPr());
		project.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		project.getPr().setOutputVideo(project.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(project.getPr().getWorkingDir() + "1.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/2.MP4"),
				new File(project.getPr().getWorkingDir() + "2.MP4"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/3.JPG"),
				new File(project.getPr().getWorkingDir() + "3.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/audio.mp3"),
				new File(project.getPr().getWorkingDir() + "audio.mp3"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/4.JPG"),
				new File(project.getPr().getWorkingDir() + "4.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/audio2.mp3"),
				new File(project.getPr().getWorkingDir() + "audio2.mp3"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/5.JPG"),
				new File(project.getPr().getWorkingDir() + "5.JPG"));
		
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "2.MP4"));
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "3.JPG"));
		var audio = project.createMIFAudioFile(new File(project.getPr().getWorkingDir()+"audio.mp3"));
		audio.setEncodeEnde(10000); // [ms]
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "4.JPG"));
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "5.JPG"));
		var audio2 = project.createMIFAudioFile(new File(project.getPr().getWorkingDir()+"audio2.mp3"));
		audio2.setEncodeEnde(14000); // [ms]
		
		var text = project.createMIFTextfile();
		text.setLength(userConfig.getTEXT_DURATION()); // [ms]
		text.setBgcolour(userConfig.getTEXT_BG());
		text.setFgcolour(userConfig.getTEXT_FG());
		text.setOlcolour(userConfig.getTEXT_OL());
		
		var text2 = project.createMIFTextfile();
		text2.setLength(userConfig.getTEXT_DURATION()); // [ms]
		text2.setBgcolour(userConfig.getTEXT_BG());
		text2.setFgcolour(userConfig.getTEXT_FG());
		text2.setOlcolour(userConfig.getTEXT_OL());
		
		project.redrawGraph();
		project.createFramePreview();

		return project;
	}

	private ProjectListener createChangeListener() {
		return t -> {
			LOGGER.info("Change... redraw....{}", t);

			if (t == type.LOAD_PROJECT) {
				try {
					graphWrapper.initializeProject();

					// Exec background threads...
					var executor = Executors.newWorkStealingPool();
					
					for (MIFFile f : graphWrapper.getPr().getMIFFiles()) {
						executor.submit(() -> {
							try {
//								TODO übergeben
								graphWrapper.getService().createPreview(f, graphWrapper.getPr().getWorkingDir());
							} catch (Exception e) {
								LOGGER.error("", e);
							}
						});
					}

				} catch (Exception e) {
					LOGGER.error("", e);
				}
			} else if (t == type.NEW_PROJECT) {
				// Remove all image/video cells
				for (mxCell cell : graphWrapper.getCells()) {
					mifGraphView.remove(cell, graphWrapper.get(cell));
				}
				// Remove all audio cells
				for (mxCell cell : graphWrapper.getAudioCells()) {
					mifGraphView.remove(cell, graphWrapper.getAudio(cell));
				}
				
				graphWrapper.save();
				try {
					graphWrapper.initializeProject();
					graphWrapper.redrawGraph();
				} catch (Exception e) {
					LOGGER.error("Unable to create new project. Error: {}", e.getMessage());
				}
			}
		};
	}

	public void cellClicked(Object sender) {
		var sm = (mxGraphSelectionModel) sender;
		var cell = (mxCell) sm.getCell();

		mifSelectionView.clearSelection();
		
		if (graphWrapper.get(cell) != null) {
			LOGGER.info("Selected {}", graphWrapper.get(cell).getDisplayName());
			mifSelectionView.updateImageOrVideo(cell, graphWrapper.get(cell));
		} else if (graphWrapper.getAudio(cell) != null) {
			LOGGER.info("Selected {}", graphWrapper.getAudio(cell).getAudiofile());
			mifSelectionView.updateAudio(cell, graphWrapper.getAudio(cell));
		} else if (graphWrapper.isSingleFrameNode(cell)) {
			mifSelectionView.setSingleFrameView();
		} else if (graphWrapper.getText(cell) != null) {
			mifSelectionView.updateText(cell, graphWrapper.getText(cell));
		}
	}
}

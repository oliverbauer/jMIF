package io.github.jmif.gui.swing;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.WindowConstants;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxEvent;
import com.mxgraph.view.mxGraphSelectionModel;

import io.github.jmif.MIFException;
import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFFile;
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
 * TODO Menu: About (Used Software etc.) 
 * TODO Menu: How to Install/Use 
 * TODO Switch to Gradle 
 * TODO Config: Allow in-memory-preview (byte-arrays of pictures - needs more RAM) or hd-preview (copy files - needs more HD/Time for IO) 
 * 
 * TODO Audio: "audiowaveform -i 001.mp3 -o test.png -w 1000 -h 200 -s 0 -e ${seconds} --no-axis-labels" (or (better?) use Sox).. needs optional check in SplashScreen 
 * 	$ sudo add-apt-repository ppa:chris-needham/ppa
 *	$ sudo apt-get update
 *	$ sudo apt-get install audiowaveform
 *
 * TODO MLT: Checkout  https://gl-transitions.com/
 * TODO Allow multiple files with same name
 */
public class JMIF {
	private static final Logger LOGGER = LoggerFactory.getLogger(JMIF.class);

	private GraphWrapper graphWrapper;
	private MenuView mifMenuView;
	private GraphView mifGraphView;
	private SelectionView mifSelectionView;

	public static void main(String[] args) throws Exception {
		if (Configuration.showSplashscreen) {
			Splashscreen splashscreen = new Splashscreen();
			SwingWorker<Void, Integer> worker = splashscreen.getWorker();
			while (worker.getState() != StateValue.DONE) {
				Thread.sleep(10);
			}
		}

		SwingUtilities.invokeLater(() -> {
			try {
				JMIF u = new JMIF();
				u.showFrame();
			} catch (MIFException | IOException | InterruptedException e) {
				LOGGER.error("", e);
			}
		});
	}

	public void showFrame() throws MIFException,  IOException, InterruptedException {
		long time = System.currentTimeMillis();

		var frame = new JFrame();
		frame.setUndecorated(true); // remove title bar...
		frame.setTitle("MIF-enizer (MLT/ImageMagickFFMPEG/)");

		// TODO Menu: disbale save-button, preview and gen-button on start...
		graphWrapper = createMIFProject("atsc_1080p_25");
		graphWrapper.getGraph().getSelectionModel().addListener(mxEvent.CHANGE, (sender, evt) -> cellClicked(sender));

		var panelBox = Box.createVerticalBox();
		mifMenuView = new MenuView(graphWrapper);
		panelBox.add(mifMenuView.getJPanel());
		if (Configuration.transperencyOffset > 0) {
			panelBox.add(Box.createRigidArea(new Dimension(0, Configuration.transperencyOffset)));
		}

		// Add Project stuff: name, output.file, render-profile
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(new JLabel("Project "+graphWrapper.getPr().getFileOfProject()));
		horizontalBox.add(Box.createHorizontalStrut(20));
		horizontalBox.add(new JLabel("Output "));
		JTextField output = new JTextField();
		output.setText(graphWrapper.getPr().getOutputVideo());
		output.setMaximumSize(new Dimension(300, 30));
		output.setPreferredSize(new Dimension(300, 30));
		horizontalBox.add(output);
		horizontalBox.add(Box.createHorizontalGlue());
		
		List<String> profiles = graphWrapper.getService().getProfiles();
		
		JComboBox<String> profilesCombobox = new JComboBox<>(profiles.toArray(new String[profiles.size()]));
		profilesCombobox.setSelectedItem(graphWrapper.getPr().getProfile());
		profilesCombobox.addItemListener((itemEvent) -> {
			try {
				String item = (String)profilesCombobox.getSelectedItem();
				graphWrapper.getPr().setProfile(item);
				graphWrapper.getService().updateProfile(graphWrapper.getPr());
				graphWrapper.redrawGraph();
			} catch (MIFException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Profile-Change: Needs full refresh of all nodes, timeline etc. pp
			// TODO Profile-Change: Change overlay
		});
		profilesCombobox.setMaximumSize(new Dimension(300,30));

		horizontalBox.add(profilesCombobox);
		panelBox.add(horizontalBox);
		
		mifSelectionView = new SelectionView(graphWrapper);
		mifGraphView = new GraphView(frame, graphWrapper, mifSelectionView);
		mifGraphView.init();
		panelBox.add(mifGraphView.getGraphPanel());
		if (Configuration.transperencyOffset > 0) {
			panelBox.add(Box.createRigidArea(new Dimension(0, Configuration.transperencyOffset)));
		}

		panelBox.add(mifSelectionView.getPanel());
		if (Configuration.transperencyOffset > 0) {
			panelBox.add(Box.createRigidArea(new Dimension(0, Configuration.transperencyOffset)));
		}

		frame.setLayout(new GridBagLayout());

		frame.setContentPane(panelBox);
		frame.setBackground(Configuration.bgColor);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
		
		var file1 = project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		var file2 = project.createMIFFile(new File(project.getPr().getWorkingDir() + "2.MP4"));
		var file3 = project.createMIFFile(new File(project.getPr().getWorkingDir() + "3.JPG"));
		var audio = project.createMIFAudioFile(new File(project.getPr().getWorkingDir()+"audio.mp3"));
		audio.setEncodeEnde(10000); // [ms]
		var file4 = project.createMIFFile(new File(project.getPr().getWorkingDir() + "4.JPG"));
		var file5 = project.createMIFFile(new File(project.getPr().getWorkingDir() + "5.JPG"));
		var audio2 = project.createMIFAudioFile(new File(project.getPr().getWorkingDir()+"audio2.mp3"));
		audio2.setEncodeEnde(14000); // [ms]
		
		var executor = Executors.newWorkStealingPool();
		executor.submit(() -> {
			try {
				project.getService().createPreview(file1, project.getPr().getWorkingDir());
				project.getService().createPreview(file2, project.getPr().getWorkingDir());
				project.getService().createPreview(file3, project.getPr().getWorkingDir());
				project.getService().createPreview(file4, project.getPr().getWorkingDir());
				project.getService().createPreview(file5, project.getPr().getWorkingDir());
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		});

		project.redrawGraph();
		project.createFramePreview();

		return project;
	}

	private ProjectListener createChangeListener() {
		return (t) -> {
			LOGGER.info("Change... redraw....{}", t);

			if (t == type.LOAD_PROJECT) {
				try {
					graphWrapper.initializeProject();

					// Exec background threads...
					ExecutorService executor = Executors.newWorkStealingPool();
					for (MIFFile f : graphWrapper.getPr().getMIFFiles()) {
						executor.submit(() -> {
							try {
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
			mifSelectionView.updateAudioOrVoideo(cell, graphWrapper.get(cell));
		} else if (graphWrapper.getAudio(cell) != null) {
			LOGGER.info("Selected {}", graphWrapper.getAudio(cell).getAudiofile());
			mifSelectionView.updateAudio(cell, graphWrapper.getAudio(cell), graphWrapper);
		} else if (graphWrapper.isSingleFrameNode(cell)) {
			mifSelectionView.setSingleFrameView();
		}
	}
}

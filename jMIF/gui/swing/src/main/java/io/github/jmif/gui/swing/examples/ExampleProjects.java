package io.github.jmif.gui.swing.examples;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.JMIF;
import io.github.jmif.gui.swing.config.UserConfig;

public class ExampleProjects {
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleProjects.class);
	
	@Inject
	private UserConfig userConfig;
	
	public void createMIFProject(GraphWrapper graphWrapper, String profile) throws MIFException, IOException, InterruptedException {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		graphWrapper.init();
		graphWrapper.getPr().setProfile(profile);
		graphWrapper.getService().updateProfile(graphWrapper.getPr());
		graphWrapper.getPr().setWorkingDir(tempDir.getAbsolutePath());
		graphWrapper.getService().createWorkingDirs(graphWrapper.getPr());
		graphWrapper.getPr().setFileOfProject(graphWrapper.getPr().getWorkingDir() + "defaultproject.xml");
		graphWrapper.getPr().setOutputVideo(graphWrapper.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(graphWrapper.getPr().getWorkingDir() + "1.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/2.MP4"),
				new File(graphWrapper.getPr().getWorkingDir() + "2.MP4"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/3.JPG"),
				new File(graphWrapper.getPr().getWorkingDir() + "3.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/audio.mp3"),
				new File(graphWrapper.getPr().getWorkingDir() + "audio.mp3"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/4.JPG"),
				new File(graphWrapper.getPr().getWorkingDir() + "4.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/audio2.mp3"),
				new File(graphWrapper.getPr().getWorkingDir() + "audio2.mp3"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/5.JPG"),
				new File(graphWrapper.getPr().getWorkingDir() + "5.JPG"));
		
		MIFFile image1 = graphWrapper.createMIFFile(new File(graphWrapper.getPr().getWorkingDir() + "1.JPG"));
		MIFFile video2 = graphWrapper.createMIFFile(new File(graphWrapper.getPr().getWorkingDir() + "2.MP4"));
		MIFFile image3 = graphWrapper.createMIFFile(new File(graphWrapper.getPr().getWorkingDir() + "3.JPG"));
		var audio = graphWrapper.createMIFAudioFile(new File(graphWrapper.getPr().getWorkingDir()+"audio.mp3"));
		audio.setEncodeEnde(10000); // [ms]
		MIFFile image4 = graphWrapper.createMIFFile(new File(graphWrapper.getPr().getWorkingDir() + "4.JPG"));
		MIFFile image5 = graphWrapper.createMIFFile(new File(graphWrapper.getPr().getWorkingDir() + "5.JPG"));
		var audio2 = graphWrapper.createMIFAudioFile(new File(graphWrapper.getPr().getWorkingDir()+"audio2.mp3"));
		audio2.setEncodeEnde(14000); // [ms]
		
		var text = graphWrapper.createMIFTextfile();
		text.setLength(userConfig.getTEXT_DURATION()); // [ms]
		text.setBgcolour(userConfig.getTEXT_BG());
		text.setFgcolour(userConfig.getTEXT_FG());
		text.setOlcolour(userConfig.getTEXT_OL());
		text.setUseAffineTransition(true);
		
		int milliseconds = userConfig.getTEXT_DURATION();
		int frame = (milliseconds / 1000) * graphWrapper.getPr().getProfileFramerate();
		int f = frame/4;
		StringBuilder sb = new StringBuilder();
		sb.append(0+    "=-500 0    500 -1!; \n");
		sb.append(f-1+  "=1420 0    500 -1!; \n"); 
		sb.append(2*f-1+"=1420 1000 500 -1!; \n"); 
		sb.append(3*f-1+"=0    1000 500 -1!; \n"); 
		sb.append(4*f-1+"=0    0    500 -1!; \n"); 
		text.setAffineTransition(sb.toString());
		
		var text2 = graphWrapper.createMIFTextfile();
		text2.setLength(userConfig.getTEXT_DURATION()); // [ms]
		text2.setBgcolour(userConfig.getTEXT_BG());
		text2.setFgcolour(userConfig.getTEXT_FG());
		text2.setOlcolour(userConfig.getTEXT_OL());
		
		graphWrapper.redrawGraph();
		graphWrapper.createFramePreview();

		var executor = Executors.newWorkStealingPool();
		executor.submit(() -> {
			try {
				graphWrapper.getService().createPreview(image1, graphWrapper.getPr().getWorkingDir());
				graphWrapper.getService().createPreview(video2, graphWrapper.getPr().getWorkingDir());
				graphWrapper.getService().createPreview(image3, graphWrapper.getPr().getWorkingDir());
				graphWrapper.getService().createPreview(image4, graphWrapper.getPr().getWorkingDir());
				graphWrapper.getService().createPreview(image5, graphWrapper.getPr().getWorkingDir());
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		});
	}

}

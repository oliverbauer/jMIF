package io.github.jmif.gui.swing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import io.github.jmif.MIFService;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;

public class DefaultProjectTests {
	
	private final MIFService service = new CoreGateway();
	
	@Test
	public void withoutOverlay() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile().getAbsolutePath()+"/";

		// copy predefined example files (from src/main/resources/defaultproject) to some temp directory
		copy(tempDir, "1.JPG");
		copy(tempDir, "2.MP4");
		copy(tempDir, "3.JPG");
		copy(tempDir, "audio.mp3");

		// Create a project of 2 pics, 1 video and 1 audio file
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject.xml");
		pr.setOutputVideo(tempDir+"output.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		MIFFile f2 = project.createMIFFile(new File(tempDir + "2.MP4"));
		MIFFile f3 = project.createMIFFile(new File(tempDir + "3.JPG"));
		MIFAudioFile a1 = project.createMIFAudioFile(new File(tempDir+"audio.mp3"));
		f1.setDuration(5000);    // 5 sec
		f1.setOverlayToPrevious(1000); // no overlay
		f2.setDuration(5000);    // 5 sec
		f2.setOverlayToPrevious(1000); // no overlay
		f3.setDuration(5000);    // 5 sec
		f3.setOverlayToPrevious(1000); // no overlay
		a1.setEncodeStart(0);     // 15 sec
		a1.setEncodeEnde(15);     // 15 sec
		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject.xml").exists());
		
		service.convert(pr, false);
		
		Assert.assertTrue(new File(tempDir+"output.avi").exists());
		
		String length = get("ffprobe -v quiet -of csv=p=0 -show_entries format=duration "+tempDir+"output.avi");
		Assert.assertTrue(length.startsWith("15.")); // is etwas länger, warum nicht exakt?
	}
	
	@Test
	public void withOverlay() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile().getAbsolutePath()+"/";

		// copy predefined example files (from src/main/resources/defaultproject) to some temp directory
		copy(tempDir, "1.JPG");
		copy(tempDir, "2.MP4");
		copy(tempDir, "3.JPG");
		copy(tempDir, "audio.mp3");

		// Create a project of 2 pics, 1 video and 1 audio file
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject4711.xml");
		pr.setOutputVideo(tempDir+"output4711.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		MIFFile f2 = project.createMIFFile(new File(tempDir + "2.MP4"));
		MIFFile f3 = project.createMIFFile(new File(tempDir + "3.JPG"));
		MIFAudioFile a1 = project.createMIFAudioFile(new File(tempDir+"audio.mp3"));
		f1.setDuration(5000);    // 5 sec
		f1.setOverlayToPrevious(0); // no overlay
		f2.setDuration(5000);    // 5 sec
		f2.setOverlayToPrevious(25); // no overlay
		f3.setDuration(5000);    // 5 sec
		f3.setOverlayToPrevious(25); // no overlay
		a1.setEncodeStart(0);     // 13 sec
		a1.setEncodeEnde(13);     // 13 sec

		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject4711.xml").exists());
		
		service.convert(pr, false);
		
		Assert.assertTrue(new File(tempDir+"output4711.avi").exists());
		
		String length = get("ffprobe -v quiet -of csv=p=0 -show_entries format=duration "+tempDir+"output4711.avi");
		Assert.assertTrue(length+" should start with 13", length.startsWith("13.")); // is etwas länger, warum nicht exakt?
	}

	@Test
	public void withLongAudio() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile().getAbsolutePath()+"/";

		// copy predefined example files (from src/main/resources/defaultproject) to some temp directory
		copy(tempDir, "1.JPG");
		copy(tempDir, "audio.mp3");

		// Create a project of 2 pics, 1 video and 1 audio file
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject4711.xml");
		pr.setOutputVideo(tempDir+"output4711.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		MIFAudioFile a1 = project.createMIFAudioFile(new File(tempDir+"audio.mp3"));
		f1.setDuration(5000);
		f1.setOverlayToPrevious(0);
		a1.setEncodeStart(0);     // 15 sec
		a1.setEncodeEnde(8);     // 15 sec

		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject4711.xml").exists());
		
		service.convert(pr, false);
		// [consumer avformat] error with audio encode: -541478725 (frame 201) ?????
		
		Assert.assertTrue(new File(tempDir+"output4711.avi").exists());
		
		String length = get("ffprobe -v quiet -of csv=p=0 -show_entries format=duration "+tempDir+"output4711.avi");
		Assert.assertTrue(length+" should start with 8", length.startsWith("8.")); // is etwas länger, warum nicht exakt?
	}

	
	private String get(String command) throws Exception {
		Process process = new ProcessBuilder("bash", "-c", command).start();
		String output = null;
			
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output = line;
			}
		}
		return output;
	}
	
	private void copy(String dir, String file) throws IOException {
		FileUtils.copyInputStreamToFile(
			JMIF.class.getClassLoader().getResourceAsStream("defaultproject/"+file),
			new File(dir + file));
	}
}

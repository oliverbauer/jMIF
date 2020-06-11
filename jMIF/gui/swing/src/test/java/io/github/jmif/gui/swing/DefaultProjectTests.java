package io.github.jmif.gui.swing;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.jmif.MIFService;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;

public class DefaultProjectTests {
	
	private final MIFService service = new CoreGateway();
	
	private String tempDir;
	@Before
	public void before() throws IOException {
		tempDir = Files.createTempDirectory("jMIF").toFile().getAbsolutePath()+"/";
	}
	
	@Test
	public void checkProfile1080o25() throws Exception {
		copy(tempDir, "1.JPG");
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject.xml");
		pr.setOutputVideo(tempDir+"output.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		f1.setDuration(1000);    // 5 sec
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(pr);
		
		project.save();
		service.convert(pr, false);
		Assert.assertTrue(new File(tempDir+"output.avi").exists());

		// Is the image converted correctly?
		String imageCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+tempDir+"/scaled/1.JPG";
		Assert.assertEquals("1920x1080", execute(imageCommand));

		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output.avi";
		Assert.assertEquals("1.000000", execute(videoCommand.replace("TYPE", "duration")));
		Assert.assertEquals("25", execute(videoCommand.replace("TYPE", "nb_frames")));
		Assert.assertEquals("1920", execute(videoCommand.replace("TYPE", "width")));
		Assert.assertEquals("1080", execute(videoCommand.replace("TYPE", "height")));
	}
	
	@Test
	public void checkProfile1080p50() throws Exception {
		copy(tempDir, "1.JPG");
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject.xml");
		pr.setOutputVideo(tempDir+"output.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		f1.setDuration(1000); // 1 sec
		project.getPr().setProfile("atsc_1080p_50");
		service.updateProfile(pr);
		
		project.save();
		service.convert(pr, false);
		Assert.assertTrue(new File(tempDir+"output.avi").exists());

		// Is the image converted correctly?
		String imageCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+tempDir+"/scaled/1.JPG";
		Assert.assertEquals("1920x1080", execute(imageCommand));

		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output.avi";
		Assert.assertEquals("1.000000", execute(videoCommand.replace("TYPE", "duration")));
		Assert.assertEquals("50", execute(videoCommand.replace("TYPE", "nb_frames")));
		Assert.assertEquals("1920", execute(videoCommand.replace("TYPE", "width")));
		Assert.assertEquals("1080", execute(videoCommand.replace("TYPE", "height")));
	}
	
	@Test
	public void checkProfileSVCDPal() throws Exception {
		// melt -query "profile"=svcd_pal
		
		copy(tempDir, "1.JPG");
		var project = new GraphWrapper();
		var pr = project.getPr();
		pr.setWorkingDir(tempDir);
		service.createWorkingDirs(pr);
		pr.setFileOfProject(tempDir + "defaultproject.xml");
		pr.setOutputVideo(tempDir+"output.avi");
		MIFFile f1 = project.createMIFFile(new File(tempDir + "1.JPG"));
		f1.setDuration(1000); // 1 sec
		project.getPr().setProfile("svcd_pal");
		service.updateProfile(pr);
		
		project.save();
		service.convert(pr, false);
		Assert.assertTrue(new File(tempDir+"output.avi").exists());

		// Is the image converted correctly?
		String imageCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "+tempDir+"/scaled/1.JPG";
		Assert.assertEquals("480x576", execute(imageCommand));

		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output.avi";
		Assert.assertEquals("1.000000", execute(videoCommand.replace("TYPE", "duration")));
		Assert.assertEquals("25", execute(videoCommand.replace("TYPE", "nb_frames")));
		Assert.assertEquals("480", execute(videoCommand.replace("TYPE", "width")));
		Assert.assertEquals("576", execute(videoCommand.replace("TYPE", "height")));
	}
	
	@Test
	public void withoutOverlay() throws Exception {
		// copy predefined example files (from src/main/resources/defaultproject) to some temp directory
		copy(tempDir, "1.JPG");
		copy(tempDir, "2.MP4");
		copy(tempDir, "3.JPG");

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
		f1.setDuration(1000);
		f2.setDuration(1000);
		f3.setDuration(1000);
		f1.setOverlayToPrevious(0);
		f2.setOverlayToPrevious(0);
		f3.setOverlayToPrevious(0);
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(pr);
		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject.xml").exists());
		
		service.convert(pr, false);
		
		Assert.assertTrue(new File(tempDir+"output.avi").exists());
		
		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output.avi";
		Assert.assertEquals("3.000000", execute(videoCommand.replace("TYPE", "duration")));
	}
	
	@Test
	public void withOverlay() throws Exception {
		// copy predefined example files (from src/main/resources/defaultproject) to some temp directory
		copy(tempDir, "1.JPG");
		copy(tempDir, "2.MP4");
		copy(tempDir, "3.JPG");

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
		f1.setDuration(1000);
		f2.setDuration(1000);
		f3.setDuration(1000);
		f1.setOverlayToPrevious(0); // first one will be ignored
		f2.setOverlayToPrevious(500); // 25 frames for 50fps
		f3.setOverlayToPrevious(500); // 25 frames for 50fps
		project.getPr().setProfile("atsc_1080p_50");
		service.updateProfile(pr);
		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject4711.xml").exists());
		
		service.convert(pr, false);
		
		Assert.assertTrue(new File(tempDir+"output4711.avi").exists());
		
		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output4711.avi";
		Assert.assertEquals("2.000000", execute(videoCommand.replace("TYPE", "duration")));
	}

	@Test
	public void withLongAudio() throws Exception {
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
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(pr);
		project.save();
		Assert.assertTrue(new File(tempDir+"defaultproject4711.xml").exists());
		
		service.convert(pr, false);
		// [consumer avformat] error with audio encode: -541478725 (frame 201) ?????
		
		Assert.assertTrue(new File(tempDir+"output4711.avi").exists());
		
		// Is the video created correctly?
		String videoCommand = "ffprobe -v error -select_streams v:0 -show_entries stream=TYPE -of default=noprint_wrappers=1:nokey=1 "+tempDir+"output4711.avi";
		Assert.assertEquals("8.000000", execute(videoCommand.replace("TYPE", "duration")));
	}

	
	private String execute(String command) throws Exception {
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

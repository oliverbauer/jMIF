package io.github.jmif.gui.swing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.github.jmif.entities.melt.MeltFilter;

public class ProjectXmlTests {
	
	private final CoreGateway service = new CoreGateway();
	
	private String tempDir;
	
	@Before
	public void before() throws IOException {
		tempDir = Files.createTempDirectory("jMIF").toFile().getAbsolutePath()+"/";
	}
	
	@Test
	public void simpleImage() throws Exception {
		Weld weld = new Weld()
				.property("org.jboss.weld.construction.relaxed", true)
				.property("org.jboss.weld.bootstrap.concurrentDeployment", false);
			
		GraphWrapper project = null;
		try (WeldContainer container = weld.initialize()) {
			project = container.select(GraphWrapper.class).get();
		}
		
		project.init();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir);
		service.createWorkingDirs(project.getPr());
		project.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		project.getPr().setOutputVideo(project.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(project.getPr().getWorkingDir() + "1.JPG"));
		
		project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		project.save();
	}
	
	@Test
	public void simpleImageWithFilter() throws Exception {
		Weld weld = new Weld()
				.property("org.jboss.weld.construction.relaxed", true)
				.property("org.jboss.weld.bootstrap.concurrentDeployment", false);
			
		GraphWrapper project = null;
		try (WeldContainer container = weld.initialize()) {
			project = container.select(GraphWrapper.class).get();
		}
		project.init();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir);
		service.createWorkingDirs(project.getPr());
		project.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		project.getPr().setOutputVideo(project.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(project.getPr().getWorkingDir() + "1.JPG"));
		
		var mifFile = project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		var filter = new MeltFilter("oldfilm");
		filter.getFilterUsage().put("delta","400");
		filter.getFilterUsage().put("brightnessdelta_down","50");
		mifFile.addFilter(filter);
		
		project.save();
		project.load();
		
		Assert.assertEquals(1, project.getPr().getMIFFiles().size());
		Assert.assertEquals(1, project.getPr().getMIFFiles().get(0).getFilters().size());
		
		mifFile = project.getPr().getMIFFiles().get(0);
		filter = mifFile.getFilters().get(0);
		Assert.assertEquals("oldfilm", filter.getFiltername());
		
		Map<String,String> usage = filter.getFilterUsage();
		Assert.assertEquals("400", usage.get("delta"));
	}
	
	@Test
	// TODO Problem with storing imageheight/imagewidth, but they have been set, see logfile
	public void textLoaded() throws Exception {
		Weld weld = new Weld()
				.property("org.jboss.weld.construction.relaxed", true)
				.property("org.jboss.weld.bootstrap.concurrentDeployment", false);
			
		GraphWrapper project = null;
		try (WeldContainer container = weld.initialize()) {
			project = container.select(GraphWrapper.class).get();
		}
		project.init();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir);
		service.createWorkingDirs(project.getPr());
		project.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		project.getPr().setOutputVideo(project.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(project.getPr().getWorkingDir() + "1.JPG"));
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/audio.mp3"),
				new File(project.getPr().getWorkingDir() + "audio.mp3"));
		
		var mifFile = project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		mifFile.setDuration(10000); // 10 sec
		
		var mifText = project.createMIFTextfile();
		mifText.setText("Textcase");

		var audio = project.createMIFAudioFile(new File(project.getPr().getWorkingDir()+"audio.mp3"));
		audio.setEncodeEnde(10000); // [ms]
		
		Assert.assertTrue(project.getPr().getTexttrack().getEntries().size() == 1);
		Assert.assertTrue(project.getPr().getTexttrack().getEntries().get(0).getText().equals("Textcase"));
		Assert.assertTrue(project.getPr().getAudiotrack().getAudiofiles().size() == 1);
		
		project.save();

		String projectXml = project.getPr().getWorkingDir() + "defaultproject.xml";
		Assert.assertTrue(new File(projectXml).exists());
		
		GraphWrapper loadedProject = null;
		try (WeldContainer container = weld.initialize()) {
			loadedProject = container.select(GraphWrapper.class).get();
		}
		loadedProject.init();
		loadedProject.getPr().setWorkingDir(project.getPr().getWorkingDir());
		loadedProject.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		loadedProject.load();
		
		Assert.assertTrue(loadedProject.getPr().getAudiotrack().getAudiofiles().size() == 1);
		Assert.assertTrue(loadedProject.getPr().getTexttrack().getEntries().size() == 1);
		Assert.assertTrue(loadedProject.getPr().getTexttrack().getEntries().get(0).getText().equals("Textcase"));
		Assert.assertTrue(loadedProject.getPr().getMIFFiles().size() == 1);
		Assert.assertTrue(loadedProject.getPr().getMIFFiles().get(0).getWidth() == 5184);
		Assert.assertTrue(loadedProject.getPr().getMIFFiles().get(0).getHeight() == 3888);
	}
}

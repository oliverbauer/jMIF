package io.github.jmif.gui.swing;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import io.github.jmif.entities.melt.MeltFilter;

public class ProjectXmlTests {
	
	private final CoreGateway service = new CoreGateway();
	
	@Test
	public void simpleImage() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
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
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
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
	@Ignore // TODO Problem with loading textfile from stored defaultproject.xml, see GraphWrapper.load
	public void textLoaded() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		project.getPr().setProfile("atsc_1080p_25");
		service.updateProfile(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
		service.createWorkingDirs(project.getPr());
		project.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		project.getPr().setOutputVideo(project.getPr().getWorkingDir()+"output.avi");
		FileUtils.copyInputStreamToFile(JMIF.class.getClassLoader().getResourceAsStream("defaultproject/1.JPG"),
				new File(project.getPr().getWorkingDir() + "1.JPG"));
		
		var mifFile = project.createMIFFile(new File(project.getPr().getWorkingDir() + "1.JPG"));
		mifFile.setDuration(10000); // 10 sec
		
		var mifText = project.createMIFTextfile();
		mifText.setText("Textcase");
		project.save();

		
		var loadedProject = new GraphWrapper();
		loadedProject.getPr().setFileOfProject(project.getPr().getWorkingDir() + "defaultproject.xml");
		loadedProject.load();
		
		Assert.assertTrue(loadedProject.getPr().getTexttrack().getEntries().size() == 1);
		Assert.assertTrue(loadedProject.getPr().getTexttrack().getEntries().get(0).getText().equals("Textcase"));
		Assert.assertTrue(loadedProject.getPr().getMIFFiles().size() == 1);
		Assert.assertTrue(loadedProject.getPr().getMIFFiles().get(0).getDisplayName().equals("1.JPG"));
		
	}
}

package io.github.jmif.gui.swing;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import io.github.jmif.Service;
import io.github.jmif.entities.MeltFilter;

public class ProjectXmlTests {
	@Test
	public void simpleImage() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		new Service().updateFramerate(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
		new Service().createWorkingDirs(project.getPr());
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
		new Service().updateFramerate(project.getPr());
		project.getPr().setWorkingDir(tempDir.getAbsolutePath());
		new Service().createWorkingDirs(project.getPr());
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
	}
}

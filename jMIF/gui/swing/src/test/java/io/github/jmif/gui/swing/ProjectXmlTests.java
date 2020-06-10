package io.github.jmif.gui.swing;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import io.github.jmif.MIFService;
import io.github.jmif.entities.MeltFilter;

public class ProjectXmlTests {
	
	private final MIFService service = new CoreGateway();
	
	@Test
	public void simpleImage() throws Exception {
		var tempDir = Files.createTempDirectory("jMIF").toFile();

		var project = new GraphWrapper();
		service.updateFramerate(project.getPr());
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
		service.updateFramerate(project.getPr());
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
}

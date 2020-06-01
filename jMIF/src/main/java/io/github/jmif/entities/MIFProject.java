package io.github.jmif.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "mifProject")
public class MIFProject {
	private static final Logger logger = LoggerFactory.getLogger(MIFProject.class);
	
	@XmlElementWrapper(name = "mifFiles")
	@XmlElements({ @XmlElement(name = "mifImagee", type = MIFImage.class),
			@XmlElement(name = "mifVideo", type = MIFVideo.class) })
	private List<MIFFile> mifFiles;
	
	private MIFAudioTrack audiotrack;

	private String profile;
	private int framerate;
	
	private String fileOfProject;
	private String outputVideo;
	private String workingDir = null;
	
	public MIFProject() {
		this.mifFiles = new ArrayList<>();
		this.audiotrack = new MIFAudioTrack();
	}
	
	@XmlAttribute
	public void setFileOfProject(String fileOfProject) {
		this.fileOfProject = fileOfProject;
	}

	@XmlAttribute
	public void setOutputVideo(String outputVideo) {
		this.outputVideo = outputVideo;
	}

	public String getWorkingDir() {
		return workingDir;
	}
	
	public String getOutputVideo() {
		return outputVideo;
	}
	
	@XmlElementRef
	public MIFAudioTrack getAudiotrack() {
		return audiotrack;
	}
	
	public List<MIFFile> getMIFFiles() {
		return this.mifFiles;
	}
	
	public String getProfile() {
		return profile;
	}
	
	@XmlAttribute
	public void setProfile(String profile) {
		this.profile = profile;
		
		try {
			Process process = new ProcessBuilder("bash", "-c", "melt -query \"profile\"="+this.profile).start();
	
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("frame_rate_num:")) {
						framerate = Integer.parseInt(line.substring(line.indexOf(": ")+1).trim());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to extract framerate for "+profile, e);
		}
		
		logger.info("Set profile to {} (framerate {})", profile, framerate);
	}

	public int getFramerate() {
		return framerate;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
		if (!workingDir.endsWith("/")) {
			setWorkingDir(workingDir+"/");
		}
		
		logger.info("WorkingDir '{}'", getWorkingDir());
		
		new File(getWorkingDir()).mkdirs();
		
		var orig = new File(getWorkingDir()+"orig/").mkdirs();    // Copy of the original file
		var prevew = new File(getWorkingDir()+"preview/").mkdirs(); // preview files for ui
		var scaled = new File(getWorkingDir()+"scaled/").mkdirs();  // HQ for preview-video/video
		if (orig) {
			logger.info("Created dir 'orig' within {}", getWorkingDir());
		}
		if (prevew) {
			logger.info("Created dir 'preview' within {}", getWorkingDir());
		}
		if (scaled) {
			logger.info("Created dir 'scaled' within {}", getWorkingDir());
		}
	}

	public String getFileOfProject() {
		return fileOfProject;
	}
}

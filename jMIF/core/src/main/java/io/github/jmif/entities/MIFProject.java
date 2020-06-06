package io.github.jmif.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mifProject")
public class MIFProject {
	
	@XmlElementWrapper(name = "mifFiles")
	@XmlElements({ @XmlElement(name = "mifImages", type = MIFImage.class),
			@XmlElement(name = "mifVideos", type = MIFVideo.class) })
	private List<MIFFile> mifFiles;
	
	private MIFAudioTrack audiotrack;

	private String profile;
	private int framerate;
	
	// TODO Consider removing this field
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
	}

	public int getFramerate() {
		return framerate;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}
	
	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getFileOfProject() {
		return fileOfProject;
	}
}

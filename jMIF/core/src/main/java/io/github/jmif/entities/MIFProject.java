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
	private MIFTextTrack texttrack;

	private String profile;
	private int profileWidth;
	private int profileHeight;
	private int profileFramerate;
	
	// TODO Consider removing this field
	private String fileOfProject;
	private String outputVideo;
	private String workingDir = null;
	
	public MIFProject() {
		this.mifFiles = new ArrayList<>();
		this.audiotrack = new MIFAudioTrack();
		this.texttrack = new MIFTextTrack();
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
	
	@XmlElementRef
	public MIFTextTrack getTexttrack() {
		return texttrack;
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

	public int getProfileFramerate() {
		return profileFramerate;
	}

	public void setProfileFramerate(int framerate) {
		this.profileFramerate = framerate;
	}
	
	public int getProfileWidth() {
		return profileWidth;
	}

	public void setProfileWidth(int profileWidth) {
		this.profileWidth = profileWidth;
	}

	public int getProfileHeight() {
		return profileHeight;
	}

	public void setProfileHeight(int profileHeight) {
		this.profileHeight = profileHeight;
	}

	public void setWorkingDir(String workingDir) {
		this.workingDir = workingDir;
	}

	public String getFileOfProject() {
		return fileOfProject;
	}
}

/**
 * 
 */
package io.github.jmif.gui.swing.entities;

import java.util.List;

import io.github.jmif.entities.MIFAudioTrack;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextTrack;

/**
 * @author thebrunner
 *
 */
public class MIFProjectWrapper {

	private final MIFProject project;
	
	public MIFProjectWrapper(MIFProject project) {
		this.project = project;
	}

	public MIFProject toMIFProject() {
		return project;
	}

	public boolean equals(Object obj) {
		return project.equals(obj);
	}

	public void setFileOfProject(String fileOfProject) {
		project.setFileOfProject(fileOfProject);
	}

	public void setOutputVideo(String outputVideo) {
		project.setOutputVideo(outputVideo);
	}

	public String getWorkingDir() {
		return project.getWorkingDir();
	}

	public String getOutputVideo() {
		return project.getOutputVideo();
	}

	public MIFAudioTrack getAudiotrack() {
		return project.getAudiotrack();
	}

	public MIFTextTrack getTexttrack() {
		return project.getTexttrack();
	}

	@Deprecated // TODO
	public List<MIFFileWrapper<?>> getMIFFiles() {
		return MIFFileWrapper.wrap(project.getMIFFiles());
	}

	public String getProfile() {
		return project.getProfile();
	}

	public void setProfile(String profile) {
		project.setProfile(profile);
	}

	public int getProfileFramerate() {
		return project.getProfileFramerate();
	}

	public void setProfileFramerate(int framerate) {
		project.setProfileFramerate(framerate);
	}

	public int getProfileWidth() {
		return project.getProfileWidth();
	}

	public void setProfileWidth(int profileWidth) {
		project.setProfileWidth(profileWidth);
	}

	public int getProfileHeight() {
		return project.getProfileHeight();
	}

	public void setProfileHeight(int profileHeight) {
		project.setProfileHeight(profileHeight);
	}

	public void setWorkingDir(String workingDir) {
		project.setWorkingDir(workingDir);
	}

	public String getFileOfProject() {
		return project.getFileOfProject();
	}

	public int hashCode() {
		return project.hashCode();
	}

	public String toString() {
		return project.toString();
	}
}

/**
 * 
 */
package io.github.jmif.gui.swing.entities;

import java.util.List;

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

	@Deprecated // TODO refactor
	public MIFTextTrack getTexttrack() {
		return project.getTexttrack();
	}

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

	public void clearMIFFiles() {
		project.getMIFFiles().clear();
	}

	public void addMIFFile(MIFFileWrapper<?> mifFile) {
		project.getMIFFiles().add(mifFile.toMIFFile());
	}

	public void removeMIFFile(MIFFileWrapper<?> mifFile) {
		project.getMIFFiles().remove(mifFile.toMIFFile());
	}

	public void clearAudiofiles() {
		project.getAudiotrack().getAudiofiles().clear();
	}

	public void addAudiofile(MIFAudioFileWrapper audioFile) {
		project.getAudiotrack().getAudiofiles().add(audioFile.toMIFFile());
	}

	public void removeAudiofile(MIFAudioFileWrapper audioFile) {
		project.getAudiotrack().getAudiofiles().remove(audioFile.toMIFFile());
	}

	public boolean isAudioFilesEmpty() {
		return project.getAudiotrack().getAudiofiles().isEmpty();
	}

	public void clearTextfiles() {
		project.getTexttrack().getEntries().clear();
	}

	public void addTextFile(MIFTextFileWrapper textFile) {
		project.getTexttrack().getEntries().add(textFile.toMIFTextFile());
	}
	
	public void removeTextFile(MIFTextFileWrapper textFile) {
		project.getTexttrack().getEntries().remove(textFile.toMIFTextFile());
	}

}

package io.github.jmif.core;

import java.io.File;
import java.util.List;

import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilter;
import io.github.jmif.entities.melt.MeltFilterDetails;

public interface MIFService {

	void exportImage(MIFProject pr, String output, int frame) throws MIFException;

	void convert(MIFProject pr, boolean preview) throws MIFException;

	void updateProfile(MIFProject project) throws MIFException;

	void createWorkingDirs(MIFProject project) throws MIFException;

	MIFVideo createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException;

	MIFTextFile createText() throws MIFException;
	
	MIFFile createPreview(MIFFile file, String workingDir) throws MIFException;

	void createManualPreview(MIFImage image) throws MIFException;

	MIFImage createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException;

	MIFAudioFile createAudio(String path) throws MIFException;

	List<String> getProfiles() throws MIFException;

	List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException;

	List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException;

	MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException;

	void applyFilter(MIFProject pr, MIFFile mifImage, MeltFilter meltFilter) throws MIFException;
}
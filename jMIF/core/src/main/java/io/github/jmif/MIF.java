package io.github.jmif;

import java.io.File;
import java.util.concurrent.Future;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MeltFilter;
import io.github.jmif.melt.Melt;

public interface MIF {

	long exportImage(MIFProject pr, String output, int frame) throws MIFException;

	long convert(MIFProject pr, boolean preview) throws MIFException;

	long updateFramerate(MIFProject project) throws MIFException;

	long createWorkingDirs(MIFProject project) throws MIFException;

	long createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException;

	long createPreview(MIFFile file, String workingDir) throws MIFException;

	long createManualPreview(MIFImage image) throws MIFException ;

	long createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException;

	long createAudio(String path) throws MIFException;

	long getProfiles() throws MIFException;
	
	long applyFilter(MIFImage mifImage, MeltFilter meltFilter) throws MIFException;

	Future<?> get(long id) throws MIFException;

	long getMeltVideoFilterDetails(Melt melt) throws MIFException;

	long getMeltAudioFilterDetails(Melt melt) throws MIFException;

	long getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException;

}
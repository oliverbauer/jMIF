/**
 * 
 */
package io.github.jmif.gui.swing;

import java.io.File;
import java.util.List;

import io.github.jmif.core.LocalService;
import io.github.jmif.core.MIFException;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilter;
import io.github.jmif.entities.melt.MeltFilterDetails;
import io.github.jmif.gui.swing.entities.MIFAudioFileWrapper;
import io.github.jmif.gui.swing.entities.MIFFileWrapper;
import io.github.jmif.gui.swing.entities.MIFImageWrapper;
import io.github.jmif.gui.swing.entities.MIFProjectWrapper;
import io.github.jmif.gui.swing.entities.MIFTextFileWrapper;
import io.github.jmif.gui.swing.entities.MIFVideoWrapper;

/**
 * @author thebrunner
 *
 */
public class CoreGateway {

	private final LocalService service = new LocalService();

	public void exportImage(MIFProjectWrapper pr, String output, int frame) throws MIFException {
		service.exportImage(pr.toMIFProject(), output, frame);
	}


	public void convert(MIFProjectWrapper pr, boolean preview) throws MIFException {
		service.convert(pr.toMIFProject(), preview);
	}


	public void updateProfile(MIFProjectWrapper project) throws MIFException {
		service.updateProfile(project.toMIFProject());
	}


	public void createWorkingDirs(MIFProjectWrapper project) throws MIFException {
		service.createWorkingDirs(project.toMIFProject());
	}


	public MIFVideoWrapper createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		return new MIFVideoWrapper(service.createVideo(file, display, frames, dim, overlay, workingDir));
	}


	public MIFFileWrapper<?> createPreview(MIFFileWrapper<?> file, String workingDir) throws MIFException {
		return MIFFileWrapper.wrap(service.createPreview(file.toMIFFile(), workingDir));
	}


	public void createManualPreview(MIFImageWrapper image) throws MIFException {
		service.createManualPreview(image.toMIFFile());
	}


	public MIFImageWrapper createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		return new MIFImageWrapper(service.createImage(file, display, frames, dim, overlay, workingDir));
	}


	public MIFAudioFileWrapper createAudio(String path) throws MIFException {
		return new MIFAudioFileWrapper(service.createAudio(path));
	}


	public List<String> getProfiles() throws MIFException {
		return service.getProfiles();
	}


	public List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException {
		return service.getMeltVideoFilterDetails(melt);
	}


	public List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException {
		return service.getMeltAudioFilterDetails(melt);
	}


	public MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		return service.getMeltFilterDetailsFor(melt, meltFilter);
	}


	public void applyFilter(MIFProjectWrapper pr, MIFFileWrapper<?> mifImage, MeltFilter meltFilter) throws MIFException {
		service.applyFilter(pr.toMIFProject(), mifImage.toMIFFile(), meltFilter);
	}


	public MIFTextFileWrapper createText() throws MIFException {
		return new MIFTextFileWrapper(service.createText());
	}
}

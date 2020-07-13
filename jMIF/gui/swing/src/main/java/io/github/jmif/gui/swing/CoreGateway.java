/**
 * 
 */
package io.github.jmif.gui.swing;

import java.io.File;
import java.util.List;

import io.github.jmif.core.LocalService;
import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFAudio;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilter;
import io.github.jmif.entities.melt.MeltFilterDetails;

/**
 * @author thebrunner
 *
 */
public class CoreGateway {
	private final LocalService service = new LocalService();

	public void exportImage(MIFProject pr, String output, int frame) throws MIFException {
		service.exportImage(pr, output, frame);
	}

	public void convert(MIFProject pr, boolean preview) throws MIFException {
		service.convert(pr, preview);
	}

	public void updateProfile(MIFProject project) throws MIFException {
		service.updateProfile(project);
	}

	public void createWorkingDirs(MIFProject project) throws MIFException {
		service.createWorkingDirs(project);
	}

	public MIFVideo createVideo(File file, String display, int frames, int overlay, String workingDir) throws MIFException {
		return service.createVideo(file, display, frames, overlay, workingDir);
	}

	public MIFFile createPreview(MIFFile file, String workingDir) throws MIFException {
		return service.createPreview(file, workingDir);
	}

	public void createManualPreview(MIFImage image) throws MIFException {
		service.createManualPreview(image);
	}

	public MIFImage createImage(File file, String display, int frames, int overlay, String workingDir) throws MIFException {
		return service.createImage(file, display, frames, overlay, workingDir);
	}

	public MIFAudio createAudio(String path) throws MIFException {
		return service.createAudio(path);
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

	public void applyFilter(MIFProject pr, MIFFile mifImage, MeltFilter meltFilter) throws MIFException {
		service.applyFilter(pr, mifImage, meltFilter);
	}

	public MIFTextFile createText() throws MIFException {
		return service.createText();
	}
}

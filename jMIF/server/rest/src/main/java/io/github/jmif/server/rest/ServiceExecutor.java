/**
 * 
 */
package io.github.jmif.server.rest;

import java.io.File;
import java.util.concurrent.Future;

import io.github.jmif.core.LocalService;
import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilter;

/**
 * @author thebrunner
 *
 */
public class ServiceExecutor implements MIF {

	private final LocalService service = new LocalService();

	private final ThreadExecutor executor = new ThreadExecutor();

	@Override
	public long exportImage(MIFProject pr, String output, int frame) throws MIFException {
		return executor.doIt(() -> {
			service.exportImage(pr, output, frame);
			return null;
		});
	}

	@Override
	public long convert(MIFProject pr, boolean preview) throws MIFException {
		return executor.doIt(() -> {
			service.convert(pr, preview);
			return null;
		});
	}

	@Override
	public long updateProfile(MIFProject project) throws MIFException {
		return executor.doIt(() -> {
			service.updateProfile(project);
			return null;
		});
	}

	@Override
	public long createWorkingDirs(MIFProject project) throws MIFException {
		return executor.doIt(() -> {
			service.createWorkingDirs(project);
			return null;
		});
	}

	@Override
	public long createVideo(File file, String display, int frames, int overlay, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			return service.createVideo(file, display, frames, overlay, workingDir);
		});
	}

	@Override
	public long createPreview(MIFFile file, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			return service.createPreview(file, workingDir);
		});
	}

	@Override
	public long createManualPreview(MIFImage image) {
		return executor.doIt(() -> {
			service.createManualPreview(image);
			return null;
		});
	}

	@Override
	public long createImage(File file, String display, int frames, int overlay, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			return service.createImage(file, display, frames, overlay, workingDir);
		});
	}

	@Override
	public long createAudio(File file) throws MIFException {
		return executor.doIt(() -> {
			return service.createAudio(file);
		});
	}

	@Override
	public long getProfiles() throws MIFException {
		return executor.doIt(() -> {
			return service.getProfiles();
		});
	}

	@Override
	public long getMeltVideoFilterDetails(Melt melt) throws MIFException {
		return executor.doIt(() -> {
			return service.getMeltVideoFilterDetails(melt);
		});
	}

	@Override
	public long getMeltAudioFilterDetails(Melt melt) throws MIFException {
		return executor.doIt(() -> {
			return service.getMeltAudioFilterDetails(melt);
		});
	}

	@Override
	public long getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		return executor.doIt(() -> {
			return service.getMeltFilterDetailsFor(melt, meltFilter);
		});
	}

	@Override
	public long applyFilter(MIFProject pr, MIFFile mifImage, MeltFilter meltFilter) throws MIFException {
		return executor.doIt(() -> {
			 service.applyFilter(pr, mifImage, meltFilter);
			 return null;
		});
	}
	
	@Override
	public Future<?> get(long id) throws MIFException {
		return executor.get(id);
	}

	@Override
	public long createText() throws MIFException {
		return executor.doIt(() -> {
			 return service.createText();
		});
	}
}

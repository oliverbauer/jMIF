/**
 * 
 */
package io.github.jmif;

import java.io.File;
import java.util.concurrent.Future;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MeltFilter;
import io.github.jmif.melt.Melt;

/**
 * @author thebrunner
 *
 */
public class ServiceExecutor implements MIF {

	private final MIFService service = new LocalService();

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
	public long updateFramerate(MIFProject project) throws MIFException {
		return executor.doIt(() -> {
			service.updateFramerate(project);
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
	public long createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			return service.createVideo(file, display, frames, dim, overlay, workingDir);
		});
	}

	@Override
	public long createPreview(MIFFile file, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			service.createPreview(file, workingDir);
			return null;
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
	public long createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			return service.createImage(file, display, frames, dim, overlay, workingDir);
		});
	}

	@Override
	public long createAudio(String path) throws MIFException {
		return executor.doIt(() -> {
			return service.createAudio(path);
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
	public Future<?> get(long id) throws MIFException {
		return executor.get(id);
	}
}

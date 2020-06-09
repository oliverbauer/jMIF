/**
 * 
 */
package io.github.jmif;

import java.io.File;
import java.util.Optional;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;

/**
 * @author thebrunner
 *
 */
public class ServiceExecutor implements MIF {

	private final Service service = new Service();

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
	public long updateFramerate(MIFProject project) {
		return executor.doIt(() -> {
			service.updateFramerate(project);
			return null;
		});
	}

	@Override
	public long createWorkingDirs(MIFProject project) {
		return executor.doIt(() -> {
			service.createWorkingDirs(project);
			return null;
		});
	}

	@Override
	public long createVideo(String file, String display, float frames, String dim, int overlay, String workingDir, int profileFramelength) throws MIFException {
		return executor.doIt(() -> {
			return service.createVideo(new File(file), display, frames, dim, overlay, workingDir, profileFramelength);
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
	public long createImage(String file, String display, float frames, String dim, int overlay, String workingDir, int framelength) throws MIFException {
		return executor.doIt(() -> {
			return service.createImage(new File(file), display, frames, dim, overlay, workingDir, framelength);
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
	public Optional<?> get(long id) throws MIFException {
		return executor.get(id);
	}
}

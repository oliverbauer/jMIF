/**
 * 
 */
package io.github.jmif.server.rest;

import java.io.File;
import java.util.Optional;

import io.github.jmif.MIFException;
import io.github.jmif.Service;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;

/**
 * @author thebrunner
 *
 */
public class ServiceDelegate {

	private final Service service = new Service();

	private final ThreadExecutor executor = new ThreadExecutor();

	public long exportImage(MIFProject pr, String output, int frame) throws MIFException {
		return executor.doIt(() -> {
			service.exportImage(pr, output, frame);
			return null;
		});
	}

	public long convert(MIFProject pr, boolean preview) throws MIFException {
		return executor.doIt(() -> {
			service.convert(pr, preview);
			return null;
		});
	}

	public long updateFramerate(MIFProject project) {
		return executor.doIt(() -> {
			service.updateFramerate(project);
			return null;
		});
	}

	public long createWorkingDirs(MIFProject project) {
		return executor.doIt(() -> {
			service.createWorkingDirs(project);
			return null;
		});
	}

	public long createVideo(String file, String display, float frames, String dim, int overlay, String workingDir, int profileFramelength) throws MIFException {
		return executor.doIt(() -> {
			return service.createVideo(new File(file), display, frames, dim, overlay, workingDir, profileFramelength);
		});
	}

	public long createPreview(MIFFile file, String workingDir) throws MIFException {
		return executor.doIt(() -> {
			service.createPreview(file, workingDir);
			return null;
		});
	}

	public long createManualPreview(MIFImage image) {
		return executor.doIt(() -> {
			service.createManualPreview(image);
			return null;
		});
	}

	public long createImage(String file, String display, float frames, String dim, int overlay, String workingDir, int framelength) throws MIFException {
		return executor.doIt(() -> {
			return service.createImage(new File(file), display, frames, dim, overlay, workingDir, framelength);
		});
	}

	public long createAudio(String path) throws MIFException {
		return executor.doIt(() -> {
			return service.createAudio(path);
		});
	}

	public long getProfiles() throws MIFException {
		return executor.doIt(() -> {
			return service.getProfiles();
		});
	}

	public long getFilters() throws MIFException {
		return executor.doIt(() -> {
			return service.getFilters();
		});
	}

	public long getFilterDetails(String filter) throws MIFException {
		return executor.doIt(() -> {
			return service.getFilterDetails(filter);
		});
	}

	public Optional<?> get(long id) throws MIFException {
		return executor.get(id);
	}
}

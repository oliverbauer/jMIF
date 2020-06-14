/**
 * 
 */
package io.github.jmif.gui.swing;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.MIF;
import io.github.jmif.core.MIFException;
import io.github.jmif.core.ServiceExecutor;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
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

	private static final Logger logger = LoggerFactory.getLogger(GraphWrapper.class);

	private final MIF service = new ServiceExecutor();

	private final static int MAX_THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

	private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(MAX_THREADS);

	public void exportImage(MIFProjectWrapper pr, String output, int frame) throws MIFException {
		final var id = service.exportImage(pr.toMIFProject(), output, frame);
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public void convert(MIFProjectWrapper pr, boolean preview) throws MIFException {
		final var id = service.convert(pr.toMIFProject(), preview);
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}		
	}


	public void updateProfile(MIFProjectWrapper project) throws MIFException {
		final var id = service.updateFramerate(project.toMIFProject());
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public void createWorkingDirs(MIFProjectWrapper project) throws MIFException {
		final var id = service.createWorkingDirs(project.toMIFProject());
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MIFVideoWrapper createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createVideo(file, display, frames, dim, overlay, workingDir);
		try {
			return new MIFVideoWrapper(MIFVideo.class.cast(executor.submit(() -> service.get(id)).get().get()));
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MIFFileWrapper<?> createPreview(MIFFileWrapper<?> file, String workingDir) throws MIFException {
		final var id = service.createPreview(file.toMIFFile(), workingDir);
		try {
			return MIFFileWrapper.wrap(MIFFile.class.cast(executor.submit(() -> service.get(id)).get().get()));
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public void createManualPreview(MIFImageWrapper image) throws MIFException {
		final var id = service.createManualPreview(image.toMIFFile());
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MIFImageWrapper createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createImage(file, display, frames, dim, overlay, workingDir);
		try {
			return new MIFImageWrapper(MIFImage.class.cast(executor.submit(() -> service.get(id)).get().get()));
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MIFAudioFileWrapper createAudio(String path) throws MIFException {
		final var id = service.createAudio(path);
		try {
			return new MIFAudioFileWrapper(MIFAudioFile.class.cast(executor.submit(() -> service.get(id)).get().get()));
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public List<String> getProfiles() throws MIFException {
		final var id = service.getProfiles();
		try {
			return List.class.cast(executor.submit(() -> service.get(id)).get().get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltVideoFilterDetails(melt);
		try {
			return List.class.cast(executor.submit(() -> service.get(id)).get().get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltAudioFilterDetails(melt);
		try {
			return List.class.cast(executor.submit(() -> service.get(id)).get().get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		final var id = service.getMeltFilterDetailsFor(melt, meltFilter);
		try {
			return	MeltFilterDetails.class.cast(executor.submit(() -> service.get(id)).get().get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public void applyFilter(MIFProjectWrapper pr, MIFFileWrapper<?> mifImage, MeltFilter meltFilter) throws MIFException {
		final var id = service.applyFilter(pr.toMIFProject(), mifImage.toMIFFile(), meltFilter);
		try {
			executor.submit(() -> service.get(id)).get().get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}


	public MIFTextFileWrapper createText() throws MIFException {
		final var id = service.createText();
		try {
			return new MIFTextFileWrapper(MIFTextFile.class.cast(executor.submit(() -> service.get(id)).get().get()));
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}
}

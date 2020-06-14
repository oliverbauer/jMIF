/**
 * 
 */
package io.github.jmif.gui.swing;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

	private final ExecutorService executor = Executors.newWorkStealingPool();

	private final class Waiter<T> implements Callable<T> {

		private final long id;

		public Waiter(long id) {
			this.id = id;
		}

		@SuppressWarnings("unchecked")
		
		public T call() throws Exception {
			Future<?> result = null;
			try {
				do {
					Thread.sleep(1000);
					result = service.get(id);
				} while(!result.isDone() || result.isCancelled());
			} catch (InterruptedException | MIFException e) {
				logger.error("", e);
			}
			return (T)result.get();
		}

	}

	
	public void exportImage(MIFProjectWrapper pr, String output, int frame) throws MIFException {
		final var id = service.exportImage(pr.toMIFProject(), output, frame);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public void convert(MIFProjectWrapper pr, boolean preview) throws MIFException {
		final var id = service.convert(pr.toMIFProject(), preview);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}		
	}

	
	public void updateProfile(MIFProjectWrapper project) throws MIFException {
		final var id = service.updateFramerate(project.toMIFProject());
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public void createWorkingDirs(MIFProjectWrapper project) throws MIFException {
		final var id = service.createWorkingDirs(project.toMIFProject());
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MIFVideoWrapper createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createVideo(file, display, frames, dim, overlay, workingDir);
		final Waiter<MIFVideo> waiter = new Waiter<>(id);
		try {
			return new MIFVideoWrapper(executor.submit(waiter).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MIFFileWrapper<?> createPreview(MIFFileWrapper<?> file, String workingDir) throws MIFException {
		final var id = service.createPreview(file.toMIFFile(), workingDir);
		final Waiter<MIFFile> waiter = new Waiter<>(id);
		try {
			return MIFFileWrapper.wrap(executor.submit(waiter).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public void createManualPreview(MIFImageWrapper image) throws MIFException {
		final var id = service.createManualPreview(image.toMIFFile());
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MIFImageWrapper createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createImage(file, display, frames, dim, overlay, workingDir);
		final Waiter<MIFImage> waiter = new Waiter<>(id);
		try {
			return new MIFImageWrapper(executor.submit(waiter).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MIFAudioFileWrapper createAudio(String path) throws MIFException {
		final var id = service.createAudio(path);
		final Waiter<MIFAudioFile> waiter = new Waiter<>(id);
		try {
			return new MIFAudioFileWrapper(executor.submit(waiter).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public List<String> getProfiles() throws MIFException {
		final var id = service.getProfiles();
		final Waiter<List<String>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltVideoFilterDetails(melt);
		final Waiter<List<MeltFilterDetails>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltAudioFilterDetails(melt);
		final Waiter<List<MeltFilterDetails>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		final var id = service.getMeltFilterDetailsFor(melt, meltFilter);
		final Waiter<MeltFilterDetails> waiter = new Waiter<>(id);
		try {
		return	executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public void applyFilter(MIFProjectWrapper pr, MIFFileWrapper<?> mifImage, MeltFilter meltFilter) throws MIFException {
		final var id = service.applyFilter(pr.toMIFProject(), mifImage.toMIFFile(), meltFilter);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	
	public MIFTextFileWrapper createText() throws MIFException {
		final var id = service.createText();
		final Waiter<MIFTextFile> waiter = new Waiter<>(id);
		try {
			return new MIFTextFileWrapper(executor.submit(waiter).get());
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}
}

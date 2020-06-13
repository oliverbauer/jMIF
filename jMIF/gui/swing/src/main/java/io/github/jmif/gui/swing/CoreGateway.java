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
import io.github.jmif.core.MIFService;
import io.github.jmif.core.ServiceExecutor;
import io.github.jmif.entities.MIFAudioFile;
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
public class CoreGateway implements MIFService {

	private static final Logger logger = LoggerFactory.getLogger(GraphWrapper.class);

	private final MIF service = new ServiceExecutor();

	private final ExecutorService executor = Executors.newWorkStealingPool();

	private final class Waiter<T> implements Callable<T> {

		private final long id;

		public Waiter(long id) {
			this.id = id;
		}

		@SuppressWarnings("unchecked")
		@Override
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

	@Override
	public void exportImage(MIFProject pr, String output, int frame) throws MIFException {
		final var id = service.exportImage(pr, output, frame);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public void convert(MIFProject pr, boolean preview) throws MIFException {
		final var id = service.convert(pr, preview);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}		
	}

	@Override
	public void updateProfile(MIFProject project) throws MIFException {
		final var id = service.updateFramerate(project);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public void createWorkingDirs(MIFProject project) throws MIFException {
		final var id = service.createWorkingDirs(project);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public MIFVideo createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createVideo(file, display, frames, dim, overlay, workingDir);
		final Waiter<MIFVideo> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public void createPreview(MIFFile file, String workingDir) throws MIFException {
		final var id = service.createPreview(file, workingDir);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public void createManualPreview(MIFImage image) throws MIFException {
		final var id = service.createManualPreview(image);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public MIFImage createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		final var id = service.createImage(file, display, frames, dim, overlay, workingDir);
		final Waiter<MIFImage> waiter = new Waiter<>(id);
		try {
			return	executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public MIFAudioFile createAudio(String path) throws MIFException {
		final var id = service.createAudio(path);
		final Waiter<MIFAudioFile> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public List<String> getProfiles() throws MIFException {
		final var id = service.getProfiles();
		final Waiter<List<String>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltVideoFilterDetails(melt);
		final Waiter<List<MeltFilterDetails>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException {
		final var id = service.getMeltAudioFilterDetails(melt);
		final Waiter<List<MeltFilterDetails>> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		final var id = service.getMeltFilterDetailsFor(melt, meltFilter);
		final Waiter<MeltFilterDetails> waiter = new Waiter<>(id);
		try {
		return	executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public void applyFilter(MIFProject pr, MIFFile mifImage, MeltFilter meltFilter) throws MIFException {
		final var id = service.applyFilter(pr, mifImage, meltFilter);
		final Waiter<Void> waiter = new Waiter<>(id);
		try {
			executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}

	@Override
	public MIFTextFile createText() throws MIFException {
		final var id = service.createText();
		final Waiter<MIFTextFile> waiter = new Waiter<>(id);
		try {
			return executor.submit(waiter).get();
		} catch (InterruptedException | ExecutionException e) {
			throw new MIFException(e);
		}
	}
}

package io.github.jmif.gui.swing.entities;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.MeltFilter;

public class MIFFileWrapper<T extends MIFFile> {

	private final T mifFile;
	
	public MIFFileWrapper(T mifFile) {
		this.mifFile = mifFile;
	}
	
	public T toMIFFile() {
		return mifFile;
	}
	
	public static MIFFileWrapper<?> wrap(MIFFile mifFile) {
		if (mifFile instanceof MIFImage) {
			return new MIFImageWrapper(MIFImage.class.cast(mifFile));
		} else if (mifFile instanceof MIFAudioFile) {
			return new MIFAudioFileWrapper(MIFAudioFile.class.cast(mifFile));
		}
		return new MIFVideoWrapper(MIFVideo.class.cast(mifFile));
	}
	
	public static List<MIFFileWrapper<?>> wrap(Collection<MIFFile> mifFiles) {
		return mifFiles.stream().map(MIFFileWrapper::wrap).collect(Collectors.toList());
	}

	public void addFilter(MeltFilter currentlySelectedFilter) {
		mifFile.addFilter(currentlySelectedFilter);
	}

	public int getOverlayToPrevious() {
		return mifFile.getOverlayToPrevious();
	}

	public void setOverlayToPrevious(int overlayToPrevious) {
		mifFile.setOverlayToPrevious(overlayToPrevious);
	}

	public String getDisplayName() {
		return mifFile.getDisplayName();
	}

	public File getFile() {
		return mifFile.getFile();
	}

	public int getDuration() {
		return mifFile.getDuration();
	}

	public void setDuration(int framelength) {
		mifFile.setDuration(framelength);
	}

	public int getHeight() {
		return mifFile.getHeight();
	}

	public int getWidth() {
		return mifFile.getWidth();
	}

	public List<MeltFilter> getFilters() {
		return mifFile.getFilters();
	}

	public void setFile(File file) {
		mifFile.setFile(file);
	}

	@Override
	public boolean equals(Object obj) {
		return hashCode() == obj.hashCode();
	}
	
	@Override
	public int hashCode() {
		return getFile().hashCode();
	}
}

package io.github.jmif.entities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "meltfile")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class MIFFile {
	private File file;
	private String filename;
	protected String displayName;

	// TODO Overlay: Depends on #9 Configuration: Create properties-file
	protected int overlayToPrevious = 1000; // 1 Second
	protected int height = -1;
	protected int width = -1;

	// Will be set if file gets checked.
	// Videos: Default Videolength
	protected int duration = 5000;

	@XmlTransient
	protected boolean initialized = false;

	@XmlTransient
	protected boolean fileExists = true;
	
	@XmlElement(name = "mifFilters")
	private List<MeltFilter> meltFilter = new ArrayList<>();
	
	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFileExtension() {
		return filename.substring(filename.lastIndexOf('.') + 1);
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	public void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getOverlayToPrevious() {
		return overlayToPrevious;
	}

	public void setOverlayToPrevious(int overlayToPrevious) {
		this.overlayToPrevious = overlayToPrevious;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public File getFile() {
		return this.file;
	}
	
	public int getDuration() {
		return duration;
	}

	public void setDuration(int framelength) {
		this.duration = framelength;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	public boolean isPicture() {
		return getClass() == MIFImage.class;
	}

	public void setFileExists(boolean fileExists) {
		this.fileExists = fileExists;
	}

	public void addFilter(MeltFilter currentlySelectedFilter) {
		meltFilter.add(currentlySelectedFilter);		
	}
	
	public void setFilters(List<MeltFilter> f) {
		this.meltFilter = f;
	}
	
	public List<MeltFilter> getFilters() {
		return this.meltFilter;
	}
}

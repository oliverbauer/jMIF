package io.github.jmif.entities;

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
	private String file;
	private String filename;
	protected String displayName;

	// TODO Overlay: Depends on #9 Configuration: Create properties-file
	// TODO Overlay: Depends on Project-framerate
	// TODO Overlay: Refactor to use seconds
	protected int overlayToPrevious = 25; // 1 Second
	protected int height = -1;
	protected int width = -1;

	// Will be set if file gets checked.
	// Images: Default 125
	// Videos: Default Videolength
	protected float framelength = -1;

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

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return this.file;
	}
	
	public float getFramelength() {
		return framelength;
	}

	public void setFramelength(float framelength) {
		this.framelength = framelength;
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

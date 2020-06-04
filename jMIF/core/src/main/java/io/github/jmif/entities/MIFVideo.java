package io.github.jmif.entities;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.github.jmif.Service;

@XmlRootElement(name = "video")
public class MIFVideo extends MIFFile {

	@XmlTransient
	private String previewImages[];

	public MIFVideo() {

	}

	public MIFVideo(String file, String display, float frames, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setFramelength(frames);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public String[] getPreviewImages() {
		return this.previewImages;
	}

	public void setPreviewImages(String[] previewImages) {
		this.previewImages = previewImages;
	}
	
	@Override
	public void init(String workingDir, int profileFramelength) {
		// TODO refactor to caller
		new Service().init(this, workingDir, profileFramelength);
	}

	@Override
	public Runnable getBackgroundRunnable(String workingDir) {
		// TODO refactor to caller
		return  () -> {
			try {
				new Service().createPreview(this, workingDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
}

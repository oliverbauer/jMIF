package io.github.jmif.entities;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import io.github.jmif.Service;

@XmlRootElement(name = "picture")
public class MIFImage extends MIFFile {

	// CROP, HARD, FILL, MANUAL
	private String style = "CROP";
	private String manualStyleCommand = null;
	
	@XmlTransient
	private String imagePreview;
	
	@XmlTransient
	private String previewHardResize;
	
	@XmlTransient
	private String previewFillWColor;
	
	@XmlTransient
	private String previewCrop;
	
	@XmlTransient
	private String previewManual;

	@XmlTransient
	private int previewHeight;

	@XmlTransient
	private int previewWidth;

	public MIFImage() {

	}

	public MIFImage(String file, String display, float frames, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setFramelength(frames);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public String getManualStyleCommand() {
		return manualStyleCommand;
	}

	public void setManualStyleCommand(String manualStyleCommand) {
		this.manualStyleCommand = manualStyleCommand;
		this.style = "MANUAL";
		// TODO refactor to caller
		new Service().createPreview(this);
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getPreviewHardResize() {
		return previewHardResize;
	}

	public String getPreviewFillWColor() {
		return previewFillWColor;
	}

	public String getPreviewCrop() {
		return previewCrop;
	}

	public String getImagePreview() {
		return imagePreview;
	}

	public String getPreviewManual() {
		return previewManual;
	}

	public void setPreviewManual(String previewManual) {
		this.previewManual = previewManual;
	}
	
	public int getPreviewWidth() {
		return previewWidth;
	}
	
	public void setPreviewHeight(int previewHeight) {
		this.previewHeight = previewHeight;
	}
	
	public void setPreviewWidth(int previewWidth) {
		this.previewWidth = previewWidth;
	}
	
	public int getPreviewHeight() {
		return previewHeight;
	}
	
	public void setPreviewCrop(String previewCrop) {
		this.previewCrop = previewCrop;
	}
	
	public void setImagePreview(String imagePreview) {
		this.imagePreview = imagePreview;
	}
	
	public void setPreviewHardResize(String previewHardResize) {
		this.previewHardResize = previewHardResize;
	}
	
	public void setPreviewFillWColor(String previewFillWColor) {
		this.previewFillWColor = previewFillWColor;
	}

	@Override
	public void init(String workingDir, int framelength) {
		// TODO refactor to caller
		new Service().init(this, workingDir, framelength);
	}

	@Override
	public Runnable getBackgroundRunnable(String workingDir) {
		return  () -> {
			try {
				// TODO refactor to caller
				new Service().createPreview(this, workingDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

}

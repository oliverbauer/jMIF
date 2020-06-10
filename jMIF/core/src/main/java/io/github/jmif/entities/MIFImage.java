package io.github.jmif.entities;

import java.io.File;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "picture")
public class MIFImage extends MIFFile {
	public static enum ImageResizeStyle {
		CROP,
		HARD,
		FILL,
		MANUAL
	}
	
	private ImageResizeStyle style = ImageResizeStyle.CROP;
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

	public MIFImage(File file, String display, int duration, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setDuration(duration);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public String getManualStyleCommand() {
		return manualStyleCommand;
	}

	public void setManualStyleCommand(String manualStyleCommand) {
		this.manualStyleCommand = manualStyleCommand;
	}

	public ImageResizeStyle getStyle() {
		return style;
	}

	public void setStyle(ImageResizeStyle style) {
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

}

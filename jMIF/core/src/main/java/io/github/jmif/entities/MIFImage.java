package io.github.jmif.entities;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "picture")
public class MIFImage extends MIFFile {
	public static enum ImageResizeStyle {
		CROP,
		MANUAL
	}
	
	private ImageResizeStyle style = ImageResizeStyle.CROP;
	private String manualStyleCommand = null;
	
	@XmlTransient
	private Path imagePreviewPath;
	
	@XmlTransient
	private Path previewCropPath;
	
	@XmlTransient
	private Path previewManualPath;

	@XmlTransient
	private Image imagePreview;
	
	@XmlTransient
	private Image previewHardResize;
	
	@XmlTransient
	private Image previewCrop;
	
	@XmlTransient
	private Image previewManual;

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

	public Path getPreviewCropPath() {
		return previewCropPath;
	}

	public Path getImagePreviewPath() {
		return imagePreviewPath;
	}

	public Path getPreviewManualPath() {
		return previewManualPath;
	}

	public void setPreviewManualPath(Path previewManual) {
		this.previewManualPath = previewManual;
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
	
	public void setPreviewCropPath(Path previewCrop) {
		this.previewCropPath = previewCrop;
	}
	
	public void setImagePreviewPath(Path imagePreview) {
		this.imagePreviewPath = imagePreview;
	}
	
	public Image getImagePreview() {
		return imagePreview;
	}

	public void setImagePreview(Image imagePreview) {
		this.imagePreview = imagePreview;
	}

	public Image getPreviewHardResize() {
		return previewHardResize;
	}

	public void setPreviewHardResize(Image previewHardResize) {
		this.previewHardResize = previewHardResize;
	}

	public Image getPreviewCrop() {
		return previewCrop;
	}

	public void setPreviewCrop(Image previewCrop) {
		this.previewCrop = previewCrop;
	}

	public Image getPreviewManual() {
		return previewManual;
	}

	public void setPreviewManual(Image previewManual) {
		this.previewManual = previewManual;
	}

}

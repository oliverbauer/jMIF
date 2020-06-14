/**
 * 
 */
package io.github.jmif.gui.swing.entities;

import java.awt.Image;

import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFImage.ImageResizeStyle;

/**
 * @author thebrunner
 *
 */
public class MIFImageWrapper extends MIFFileWrapper<MIFImage> {

	public MIFImageWrapper(MIFImage mifFile) {
		super(mifFile);
	}

	public void setManualStyleCommand(String manualStyleCommand) {
		toMIFFile().setManualStyleCommand(manualStyleCommand);
	}

	public ImageResizeStyle getStyle() {
		return toMIFFile().getStyle();
	}

	public Image getPreviewCrop() {
		return toMIFFile().getPreviewCrop();
	}

	public Image getPreviewFillWColor() {
		return toMIFFile().getPreviewFillWColor();
	}
	
	public Image getPreviewHardResize() {
		return toMIFFile().getPreviewHardResize();
	}
	
	public Image getPreviewManual() {
		return toMIFFile().getPreviewManual();
	}

	public void setStyle(ImageResizeStyle style) {
		toMIFFile().setStyle(style);
	}

	public Image getImagePreview() {
		return toMIFFile().getImagePreview();
	}
	
	@Override
	public int hashCode() {
		return getFile().hashCode();
	}
}

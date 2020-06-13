package io.github.jmif.gui.swing.entities;

import java.awt.Image;
import java.util.Collection;

import io.github.jmif.entities.MIFVideo;

public class MIFVideoWrapper extends MIFFileWrapper<MIFVideo> {

	public MIFVideoWrapper(MIFVideo mifFile) {
		super(mifFile);
	}

	public int getFps() {
		return toMIFFile().getFps();
	}
	
	public String getVideoCodec() {
		return toMIFFile().getVideoCodec();
	}
	
	public int getAudioBitrate() {
		return toMIFFile().getAudioBitrate();
	}
	
	public String getAudioCodec() {
		return toMIFFile().getAudioCodec();
	}

	public String getAr() {
		return toMIFFile().getAr();
	}
	
	public int getVideoBitrate() {
		return toMIFFile().getVideoBitrate();
	}

	public Collection<Image> getPreviewImages() {
		return toMIFFile().getPreviewImages();
	}
}

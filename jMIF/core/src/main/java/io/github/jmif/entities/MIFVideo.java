package io.github.jmif.entities;

import java.awt.Image;
import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "video")
public class MIFVideo extends MIFFile {

	@XmlTransient
	private Map<Path, Image> previewImages = new LinkedHashMap<>();
	
	private int fps;
	private String videoCodec;
	private int audioBitrate;
	private int videoBitrate;
	private String audioCodec;
	private String ar;

	public MIFVideo() {

	}

	public MIFVideo(File file, String display, int duration, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setDuration(duration);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public Collection<Path> getPreviewImagesPath() {
		return this.previewImages.keySet();
	}

	public void addPreviewImagePath(Path previewImagePath) {
		this.previewImages.put(previewImagePath, null);
	}
	
	public String getAr() {
		return ar;
	}

	public void setAr(String ar) {
		this.ar = ar;
	}

	public int getVideoBitrate() {
		return videoBitrate;
	}

	public void setVideoBitrate(int videoBitrate) {
		this.videoBitrate = videoBitrate;
	}

	public int getFps() {
		return fps;
	}

	public void setFps(int fps) {
		this.fps = fps;
	}

	public int getAudioBitrate() {
		return audioBitrate;
	}

	public void setAudioBitrate(int audioBitrate) {
		this.audioBitrate = audioBitrate;
	}

	public String getAudioCodec() {
		return audioCodec;
	}

	public void setAudioCodec(String codec) {
		this.audioCodec = codec;
	}
	
	public String getVideoCodec() {
		return videoCodec;
	}

	public void setVideoCodec(String videoCodec) {
		this.videoCodec = videoCodec;
	}

	public void addPreviewImage(Path previewImagePath, Image previewImage) {
		previewImages.put(previewImagePath, previewImage);
	}
	
	public Collection<Image> getPreviewImages() {
		return previewImages.values();
	}

}

package io.github.jmif.entities;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "video")
public class MIFVideo extends MIFFile {

	@XmlTransient
	private Collection<String> previewImages = new LinkedList<>();
	
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

	public Collection<String> getPreviewImages() {
		return this.previewImages;
	}

	public void addPreviewImage(String previewImage) {
		this.previewImages.add(previewImage);
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

}

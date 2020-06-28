package io.github.jmif.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "audiofile")
public class MIFAudio extends MIFFile {
	
	private String audiofile;
	private int lengthOfInput = -1;
	private int encodeStart = 0;
	private int encodeEnde = -1;
	private int fadeIn = 1;
	private int fadeOut = 1;
	private boolean normalize = true;
	
	private int bitrate = -1;
	
	public boolean isNormalize() {
		return normalize;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public int getFadeIn() {
		return fadeIn;
	}

	public void setFadeIn(int fadeIn) {
		this.fadeIn = fadeIn;
	}

	public int getFadeOut() {
		return fadeOut;
	}

	public void setFadeOut(int fadeOut) {
		this.fadeOut = fadeOut;
	}

	public String getAudiofile() {
		return audiofile;
	}

	public void setAudiofile(String audiofile) {
		this.audiofile = audiofile;
	}

	public int getLengthOfInput() {
		return lengthOfInput;
	}

	public void setLengthOfInput(int lengthOfInput) {
		this.lengthOfInput = lengthOfInput;
	}

	public void setEncodeStart(int encodeStart) {
		this.encodeStart = encodeStart;
	}

	public void setEncodeEnde(int encodeEnde) {
		this.encodeEnde = encodeEnde;
	}

	public int getEncodeStart() {
		return encodeStart;
	}

	public int getEncodeEnde() {
		return encodeEnde;
	}

}

package io.github.jmif.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "audiotrack")
public class MIFAudioTrack {
	private List<MIFAudioFile> audiofiles;

	public MIFAudioTrack() {
		this.audiofiles = new ArrayList<>();
	}
	
	public List<MIFAudioFile> getAudiofiles() {
		return audiofiles;
	}

	public void setAudiofiles(List<MIFAudioFile> audiofiles) {
		this.audiofiles = audiofiles;
	}
}

package io.github.jmif.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "audiotrack")
public class MIFAudioTrack {
	private List<MIFAudio> audiofiles;

	public MIFAudioTrack() {
		this.audiofiles = new ArrayList<>();
	}
	
	public List<MIFAudio> getAudiofiles() {
		return audiofiles;
	}

	public void setAudiofiles(List<MIFAudio> audiofiles) {
		this.audiofiles = audiofiles;
	}
}

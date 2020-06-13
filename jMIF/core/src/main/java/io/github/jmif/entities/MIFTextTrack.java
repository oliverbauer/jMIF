package io.github.jmif.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "texttrack")
public class MIFTextTrack {
	private List<MIFTextFile> entries;
	
	public MIFTextTrack() {
		this.entries = new ArrayList<>();
	}

	public List<MIFTextFile> getEntries() {
		return entries;
	}

	public void setEntries(List<MIFTextFile> entries) {
		this.entries = entries;
	}
}

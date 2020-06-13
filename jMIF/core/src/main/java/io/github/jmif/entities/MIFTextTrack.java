package io.github.jmif.entities;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "texttrack")
public class MIFTextTrack {
	private List<MIFTextFile> textEntries;
	
	public MIFTextTrack() {
		this.textEntries = new ArrayList<>();
	}
	
	public List<MIFTextFile> getEntries() {
		return this.textEntries;
	}
}

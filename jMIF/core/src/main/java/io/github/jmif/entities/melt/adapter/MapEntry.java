package io.github.jmif.entities.melt.adapter;

import javax.xml.bind.annotation.XmlElement;

public class MapEntry {
	@XmlElement
	public String key;
	@XmlElement
	public String value;
}

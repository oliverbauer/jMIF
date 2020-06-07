package io.github.jmif.melt.adapter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class MapList {
	@XmlElement
	public List<MapEntry> col;
}

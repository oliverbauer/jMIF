package io.github.jmif.entities.melt.adapter;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;

public class MapWrapper {
	@XmlElement
	public List<MapList> row;
}

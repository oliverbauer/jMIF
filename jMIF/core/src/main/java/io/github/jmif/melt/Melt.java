package io.github.jmif.melt;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "melt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Melt {
	
	private List<MeltFilterDetails> meltFilters = new LinkedList<>();

	public List<MeltFilterDetails> getMeltFilterDetails() {
		return meltFilters;
	}
	
}

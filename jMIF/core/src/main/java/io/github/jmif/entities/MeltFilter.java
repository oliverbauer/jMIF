package io.github.jmif.entities;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "meltfilter")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeltFilter {
	private String filtername; // e.g. oldfilm
	
	// What the user has entered... overrides default values
	private Map<String, String> filterUsage;
	
	public MeltFilter() {
		this.filterUsage = new HashMap<>();
	}
	
	public MeltFilter(String filtername) {
		this.filtername = filtername;
		this.filterUsage = new HashMap<>();
	}

	public Map<String, String> getFilterUsage() {
		return filterUsage;
	}

	public void setFilterUsage(Map<String, String> filterUsage) {
		this.filterUsage = filterUsage;
	}

	public String getFiltername() {
		return this.filtername;
	}
}

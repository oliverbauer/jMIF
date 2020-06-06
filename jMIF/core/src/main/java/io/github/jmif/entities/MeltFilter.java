package io.github.jmif.entities;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "meltfilter")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeltFilter {
	private String filtername; // e.g. oldfilm
	
	@XmlTransient
	private Map<String, Map<String, String>> configuration;     // e.g. delta -> {"title", "Y-Delta"},{"type", "integer},....
	
	// What the user has entered... overrides default values
	private Map<String, String> filterUsage;
	
	public MeltFilter() {
		this.configuration = new LinkedHashMap<>();
		this.filterUsage = new HashMap<>();
	}
	
	public MeltFilter(String filtername) {
		this.filtername = filtername;
		this.configuration = new LinkedHashMap<>();
		this.filterUsage = new HashMap<>();
	}

	public Map<String, String> getFilterUsage() {
		return filterUsage;
	}

	public void setFilterUsage(Map<String, String> filterUsage) {
		this.filterUsage = filterUsage;
	}

	public void appendConfigurationParameter(String parameter) {
		this.configuration.put(parameter, new LinkedHashMap<>());
	}
	
	public void appendConfigurationDetail(String parameter, String key, String description) {
		this.configuration.get(parameter).put(key, description);
	}
	
	public String getFiltername() {
		return this.filtername;
	}
	
	public Map<String, Map<String, String>> getConfiguration() {
		return this.configuration;
	}
}

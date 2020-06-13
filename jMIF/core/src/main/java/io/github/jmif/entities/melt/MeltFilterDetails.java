package io.github.jmif.entities.melt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import io.github.jmif.entities.melt.adapter.MapAdapter;

@XmlRootElement(name = "meltfilterdetails")
@XmlAccessorType(XmlAccessType.FIELD)
public class MeltFilterDetails {
	private String filtername; // e.g. oldfilm

	/*
	 * schema_version: 0.3 title: deflicker version: Lavfi7.57.100 identifier:
	 * avfilter.deflicker description: Remove temporal frame luminance variations.
	 * creator: libavfilter maintainers type: filter tags: - Video
	 */
	private Map<String, String> generalInformations;

	// For a concreate filter all available parameters with there key-/value
	// informations.
	// e.g.
	// delta -> {"title", "Y-Delta"}, {"type", "integer},....
	// brightnessdelta_up -> {"title", "Brightness up"}, {"readonly", "no"},
	// {"widget", "spinner"},...
	// ...
	@XmlJavaTypeAdapter(MapAdapter.class)
	private List<Map<String, String>> configuration;
	
	private Map<String, Integer> configIndex;

	public MeltFilterDetails() {
		this.configuration = new ArrayList<>();
		this.generalInformations = new HashMap<>();
		this.configIndex = new LinkedHashMap<>();
	}

	public MeltFilterDetails(String filtername) {
		this.filtername = filtername;
		this.configuration = new ArrayList<>();
		this.generalInformations = new HashMap<>();
		this.configIndex = new LinkedHashMap<>();
	}

	public void addGeneralInformations(String key, String value) {
		this.generalInformations.put(key, value);
	}

	public Map<String, String> getGeneralInformations() {
		return generalInformations;
	}

	public void appendConfigurationParameter(String parameter) {
		this.configIndex.put(parameter, this.configuration.size());
		this.configuration.add(new HashMap<>());
	}

	public void appendConfigurationDetail(String parameter, String key, String description) {
		this.configuration.get(configIndex.get(parameter)).put(key, description);
	}

	public void appendAllowedValueForParameter(String parameter, String allowedConfig) {
		String currentlySaved = this.configuration.get(configIndex.get(parameter)).get("values");
		if (currentlySaved == null) {
			this.configuration.get(configIndex.get(parameter)).put("values", allowedConfig);
		} else {
			this.configuration.get(configIndex.get(parameter)).put("values", currentlySaved + "," + allowedConfig);
		}
	}

	public Map<String, Integer> getConfigIndex() {
		return configIndex;
	}

	public String getFiltername() {
		return this.filtername;
	}

	public List<Map<String, String>> getConfiguration() {
		return this.configuration;
	}
}

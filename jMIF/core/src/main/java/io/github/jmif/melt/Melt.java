package io.github.jmif.melt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.Service;
import io.github.jmif.entities.MeltFilter;

@XmlRootElement(name = "melt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Melt {
	private static final Logger logger = LoggerFactory.getLogger(Melt.class);
	
	private List<MeltFilterDetails> meltFilters;

	public Melt() {
		meltFilters = new ArrayList<>();
	}
	
	private List<MeltFilterDetails> getOrLoad() {
		if (!meltFilters.isEmpty()) {
			return meltFilters;
		}
		
		if (new File("meltfilterdetails.xml").exists()) {
			try {
				var context = JAXBContext.newInstance(Melt.class);
				var unmarshaller = context.createUnmarshaller();
				Melt m = (Melt) unmarshaller.unmarshal(new File("meltfilterdetails.xml"));
			
				this.meltFilters.addAll(m.getMeltFilterDetails());
				
				System.err.println("loaded...");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return meltFilters;
		} else {
			return save();
		}
	}
	
	private List<MeltFilterDetails> save() {
		// TODO read/write an input xml containing the result of the following expensive output
		try {
			List<String> filterNames = new Service().getFilters();
			logger.info("Filter {} loaded", filterNames.size());
			
			for (String filter: filterNames) {
				List<String> details = new Service().getFilterDetails(filter);
				
				// E.g. oldfilm has 8 parameter...
				MeltFilterDetails meltFilter = new MeltFilterDetails(filter);
				
				boolean parametersStarted = false;
				boolean valuesStarted = false;
				String currentParameter = null;
				for (String d : details) {
					if (!parametersStarted) {
						if (d.contains(":")) {
							String parts[] = d.split(":");
							
							if (parts.length == 2) {
								meltFilter.addGeneralInformations(parts[0].trim(), parts[1].trim());
							} else {
								meltFilter.addGeneralInformations(parts[0].trim(), "tags...");
							}
						} else {
							if (d.contains("Video")) {
								meltFilter.addGeneralInformations("tags", "Video");
							} else if (d.contains("Audio")) {
								meltFilter.addGeneralInformations("tags", "Audio");
							}
						}
					}
					
					if (d.contains("parameters:")) {
						parametersStarted = true;
						valuesStarted = false;
					} else if (d.contains("identifier:") && parametersStarted) {
						currentParameter = d.substring(d.indexOf("identifier: ")+"identifier: ".length());
						meltFilter.appendConfigurationParameter(currentParameter); // z.B. delta, every, brightnessdelta_up, ...
						valuesStarted = false;
					} else if (d.contains("values") && parametersStarted) {
						valuesStarted = true;
					} else if (parametersStarted) {
						// Extract parameter details...
						d = d.trim();
						if (d.contains(":")) {
							valuesStarted = false;
							
							String key = d.substring(0, d.indexOf(':')).trim();
							String description = d.substring(d.indexOf(':')+1).trim();
	
							meltFilter.appendConfigurationDetail(currentParameter, key, description);
						} else {
							if (valuesStarted) {
								d = d.replace("-", "");
								d = d.trim();
								
								// ... is last line
								if (!d.contentEquals("...")) {
									meltFilter.appendAllowedValueForParameter(currentParameter, d);
								}
							}
						}
					}
				}
				
				logger.info("-{} done", filter);
				
				meltFilters.add(meltFilter);
			}
			
			var context = JAXBContext.newInstance(Melt.class);
			var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(this, new File("meltfilterdetails.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return meltFilters;
	}
	
	public List<MeltFilterDetails> getMeltFilterDetails() {
		return getOrLoad();
	}
	
	public MeltFilterDetails getMeltFilterDetailsFor(MeltFilter meltFilter) {
		return getOrLoad().stream().filter(mdf -> mdf.getFiltername().contentEquals(meltFilter.getFiltername())).findAny().get();
	}
}

package io.github.jmif.entities;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "textfile")
public class MIFTextFile {
	private String text = "change me:-)";
	private String bgcolour = "0xff000080";
	private String fgcolour = "0x000000ff";
	private String olcolour = "0x000000ff";
	private int size = 192;
	private int weight = 1000;

	private int length = 1000; // [ms]
	
	//left, center, right
	private String halign = "center";
	// top, middle, bottom
	private String valign = "bottom";
	
	private boolean useAffineTransition = false;
	private String affineTransition = "";
	
	public MIFTextFile() {
		
	}
	
	public String getHalign() {
		return halign;
	}

	public void setHalign(String halign) {
		this.halign = halign;
	}

	public String getValign() {
		return valign;
	}

	public void setValign(String valign) {
		this.valign = valign;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getBgcolour() {
		return bgcolour;
	}

	public void setBgcolour(String bgcolour) {
		this.bgcolour = bgcolour;
	}

	public String getFgcolour() {
		return fgcolour;
	}

	public void setFgcolour(String fgcolour) {
		this.fgcolour = fgcolour;
	}

	public String getOlcolour() {
		return olcolour;
	}

	public void setOlcolour(String olcolour) {
		this.olcolour = olcolour;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public boolean isUseAffineTransition() {
		return useAffineTransition;
	}

	public void setUseAffineTransition(boolean useAffineTransition) {
		this.useAffineTransition = useAffineTransition;
	}

	public String getAffineTransition() {
		return affineTransition;
	}

	public void setAffineTransition(String affineTransition) {
		this.affineTransition = affineTransition;
	}
}

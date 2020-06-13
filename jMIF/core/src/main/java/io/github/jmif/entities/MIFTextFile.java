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
}

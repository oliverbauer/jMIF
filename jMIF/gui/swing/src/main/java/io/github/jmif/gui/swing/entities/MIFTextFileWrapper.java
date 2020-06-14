package io.github.jmif.gui.swing.entities;

import io.github.jmif.entities.MIFTextFile;

public class MIFTextFileWrapper {

	private final MIFTextFile textFile;

	public MIFTextFileWrapper(MIFTextFile textFile) {
		this.textFile = textFile;
	}
	
	public MIFTextFile toMIFTextFile() {
		return textFile;
	}

	public String getText() {
		return textFile.getText();
	}

	public void setText(String text) {
		textFile.setText(text);
	}

	public String getBgcolour() {
		return textFile.getBgcolour();
	}

	public void setBgcolour(String bgcolour) {
		textFile.setBgcolour(bgcolour);
	}

	public String getFgcolour() {
		return textFile.getFgcolour();
	}

	public void setFgcolour(String fgcolour) {
		textFile.setFgcolour(fgcolour);
	}

	public String getOlcolour() {
		return textFile.getOlcolour();
	}

	public void setOlcolour(String olcolour) {
		textFile.setOlcolour(olcolour);
	}

	public int getSize() {
		return textFile.getSize();
	}

	public void setSize(int size) {
		textFile.setSize(size);
	}

	public int getWeight() {
		return textFile.getWeight();
	}

	public void setWeight(int weight) {
		textFile.setWeight(weight);
	}

	public int getLength() {
		return textFile.getLength();
	}

	public void setLength(int length) {
		textFile.setLength(length);
	}
	
	public String getValign() {
		return textFile.getValign();
	}

	public String getHalign() {
		return textFile.getHalign();
	}
	
	public void setValign(String valign) {
		textFile.setValign(valign);
	}

	public void setHalign(String halign) {
		textFile.setHalign(halign);
	}
}

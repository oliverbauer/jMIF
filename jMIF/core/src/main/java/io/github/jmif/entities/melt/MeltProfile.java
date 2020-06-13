package io.github.jmif.entities.melt;

public class MeltProfile {
	private String name;

	private int width;
	private int height;
	private int frateRate;
	
	public MeltProfile(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getFrateRate() {
		return frateRate;
	}

	public void setFrateRate(int frateRate) {
		this.frateRate = frateRate;
	}
}

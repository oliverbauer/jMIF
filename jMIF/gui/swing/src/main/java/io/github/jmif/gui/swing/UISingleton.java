package io.github.jmif.gui.swing;

import io.github.jmif.gui.swing.selection.image.ImageDetailsView;
import io.github.jmif.gui.swing.selection.image.ImageView;

public class UISingleton {
	private static UISingleton instance;
	
	private ImageDetailsView imageDetailsView;
	private ImageView imageView;
	
	private UISingleton() {
		
	}
	
	public static UISingleton get() {
		if (instance == null) {
			instance = new UISingleton();
		}
		return instance;
	}

	public ImageDetailsView getImageDetailsView() {
		return imageDetailsView;
	}

	public void setImageDetailsView(ImageDetailsView imageDetailsView) {
		this.imageDetailsView = imageDetailsView;
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}
	
	
}

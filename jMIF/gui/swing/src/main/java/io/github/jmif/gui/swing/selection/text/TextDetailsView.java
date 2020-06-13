package io.github.jmif.gui.swing.selection.text;

import javax.swing.Box;

import io.github.jmif.gui.swing.GraphWrapper;

public class TextDetailsView {
	private Box box;
	private GraphWrapper graphWrapper;
	
	public TextDetailsView(GraphWrapper graphWrapper) {
		this.graphWrapper = graphWrapper;
	}

	public Box getBox() {
		return box;
	}
}

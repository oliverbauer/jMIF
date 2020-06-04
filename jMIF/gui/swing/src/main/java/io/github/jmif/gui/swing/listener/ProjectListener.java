package io.github.jmif.gui.swing.listener;

public interface ProjectListener {
	enum type {
		NEW_PROJECT,
		LOAD_PROJECT
	}
	void projectChanged(type t);
}

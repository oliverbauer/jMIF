package io.github.jmif.gui.swing.graph;

public interface AddRemoveListener {
	void onRemoveFile();
	void onRemoveAudio();
	void onRemoveText();
	
	void onAddFile();
	void onAddAudio();
	void onAddText();
}

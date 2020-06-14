package io.github.jmif.gui.swing.entities;

import io.github.jmif.entities.MIFAudioFile;

public class MIFAudioFileWrapper extends MIFFileWrapper<MIFAudioFile> {

	public MIFAudioFileWrapper(MIFAudioFile mifFile) {
		super(mifFile);
	}

	public void setNormalize(boolean normalize) {
		toMIFFile().setNormalize(normalize);
	}
	
	public int getLengthOfInput() {
		return toMIFFile().getLengthOfInput();
	}
	
	public void setEncodeEnde(int encodeEnde) {
		toMIFFile().setEncodeEnde(encodeEnde);
	}
	
	public void setFadeIn(int fadeIn) {
		toMIFFile().setFadeIn(fadeIn);
	}
	
	public void setFadeOut(int fadeOut) {
		toMIFFile().setFadeOut(fadeOut);
	}
	
	public String getAudiofile() {
		return toMIFFile().getAudiofile();
	}
	
	public int getBitrate() {
		return toMIFFile().getBitrate();
	}
	
	public int getEncodeStart() {
		return toMIFFile().getEncodeStart();
	}
	
	public int getEncodeEnde() {
		return toMIFFile().getEncodeEnde();
	}
	
	public int getFadeIn() {
		return toMIFFile().getFadeIn();
	}
	
	public int getFadeOut() {
		return toMIFFile().getFadeOut();
	}
	
	public boolean isNormalize() {
		return toMIFFile().isNormalize();
	}
}

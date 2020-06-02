package io.github.jmif.entities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "audiofile")
public class MIFAudioFile {
	private static final Logger logger = LoggerFactory.getLogger(MIFAudioFile.class);
	
	private String audiofile;
	private int lengthOfInput = -1;
	private int encodeStart = 0;
	private int encodeEnde = -1;
	private int fadeIn = 1;
	private int fadeOut = 1;
	
	private int bitrate = -1;
	
	public int getBitrate() {
		return bitrate;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public int getFadeIn() {
		return fadeIn;
	}

	public void setFadeIn(int fadeIn) {
		this.fadeIn = fadeIn;
	}

	public int getFadeOut() {
		return fadeOut;
	}

	public void setFadeOut(int fadeOut) {
		this.fadeOut = fadeOut;
	}

	public String getAudiofile() {
		return audiofile;
	}

	public void setAudiofile(String audiofile) {
		this.audiofile = audiofile;
		checkLengthInSeconds();
	}

	public int getLengthOfInput() {
		return lengthOfInput;
	}

	public void setLengthOfInput(int lengthOfInput) {
		this.lengthOfInput = lengthOfInput;
	}

	public void setEncodeStart(int encodeStart) {
		this.encodeStart = encodeStart;
	}

	public void setEncodeEnde(int encodeEnde) {
		this.encodeEnde = encodeEnde;
	}

	public int getEncodeStart() {
		return encodeStart;
	}

	public int getEncodeEnde() {
		return encodeEnde;
	}

	private void checkLengthInSeconds() {
		Process process;
		try {
			String command = "ffprobe -v error -select_streams a:0 -show_entries stream=duration,bit_rate -of default=noprint_wrappers=1:nokey=1 "+audiofile;
			
			process = new ProcessBuilder("bash", "-c", command)
				.redirectErrorStream(true)
				.start();
			process.waitFor();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String durationOutput = reader.readLine();
				String bitrateOutput = reader.readLine();

				setLengthOfInput(Integer.parseInt(durationOutput.substring(0, durationOutput.indexOf('.'))));
				setBitrate(Integer.parseInt(bitrateOutput));
			}
		} catch (IOException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		} catch (InterruptedException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		}
	}
}

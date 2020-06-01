package io.github.jmif.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class AudioBuilder {
	private static AudioBuilder instance;

	private AudioBuilder() {

	}

	public static AudioBuilder get() {
		instance = new AudioBuilder();
		return instance;
	}

	private List<String[]> mp3s = new ArrayList<>();

	public AudioBuilder withMP3(String input, String start, String end) {
		String[] s = {input, start, end};
		mp3s.add(s);
		return instance;
	}

	public void convert(String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path + "audio.sh")));
		bw.append("#/!/usr/bin/env bash");
		bw.newLine();
		bw.newLine();
		int count = 100;
		for (String[] mp3 : mp3s) {
			bw.append("ffmpeg -i " + mp3[0] + " -ss " + mp3[1] + " -t " + mp3[2] + " " + path + "temp_" + count
					+ ".mp3;");
			count++;
			bw.newLine();
		}
		bw.newLine();
		bw.newLine();

		/**
		 * ffmpeg -i 0.mp3 -i 1.mp3 -i 2.mp3 -i 3.mp3 -vn -filter_complex
		 * "[0][1]acrossfade=d=10:c1=tri:c2=tri[a01];
		 * [a01][2]acrossfade=d=10:c1=tri:c2=tri[a02];
		 * [a02][3]acrossfade=d=10:c1=tri:c2=tri" out.mp3
		 */
		count = 100;
		bw.append("ffmpeg ");
		for (int i = 0; i <= mp3s.size() - 1; i++) {
			bw.append("-i " + path + "temp_" + count + ".mp3 ");
			count++;
		}
		bw.append(" -vn \\");
		bw.newLine();
		bw.append(" -filter_complex \"[0][1]acrossfade=d=10:c1=tri:c2=tri[a01]; \\");
		bw.newLine();
		for (int i = 1; i <= mp3s.size() - 2; i++) {
			bw.append(" [a0" + i + "][" + (i + 1) + "]acrossfade=d=10:c1=tri:c2=tri");
			if (i != mp3s.size() - 2) {
				bw.append("[a0" + (i + 1) + "]; \\");
			} else {
				bw.append("\" \\");
			}
			bw.newLine();
		}
		bw.append("outtest.mp3;");

		bw.newLine();
		bw.newLine();
		// Fade-In, Face-Out of final file...
		bw.append(" ffmpeg -i outtest.mp3 -filter_complex \"afade=d=5, areverse, afade=d=5, areverse\" output2.mp4;");

		bw.close();
	}
}

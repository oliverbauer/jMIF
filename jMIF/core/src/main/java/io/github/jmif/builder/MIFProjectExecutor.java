package io.github.jmif.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.util.TimeUtil;

public class MIFProjectExecutor { 
	private static final Logger LOGGER = LoggerFactory.getLogger(MIFProjectExecutor.class);
	
	private MIFProject project;
	
	/**
	 * -consumer sdl2 terminate_on_pause=1
	 * -consumer avformat:output.avi acodec=libmp3lame vcodec=libx264
	 * -consumer avformat:frame5.jpg in=5 out=5
	 */
	private String consumerPreview      = " -consumer sdl2 terminate_on_pause=1";
	private String consumerRender       = " -consumer avformat:${1} acodec=libmp3lame vcodec=libx264";
	private String consumerFrameExport  = " -consumer avformat:${1} in=${2} out=${3}";
	
	public MIFProjectExecutor(MIFProject project) {
		this.project = project;
	}
	
	// TODO Image: create HQ image... really? for faster response create low quality, from low quality images
	public void exportImage(String output, int frame) throws IOException {
		adjustImagesIfNecessary();
		
		String consumer = consumerFrameExport
			.replace("${1}", output)
			.replace("${2}", String.valueOf(frame))
			.replace("${3}", String.valueOf(frame))	;
		
		createMeltFile(consumer, "melt-frameexport.sh");
		executeMELT("sh melt-frameexport.sh");
	}
	
	public void convert(boolean preview) throws IOException {
		long startTime = System.currentTimeMillis();
		
		adjustImagesIfNecessary();
		if (preview) {
			createMeltFile(consumerPreview, "melt.sh");
		} else {
			// TODO Make sure that outputVideo never is null
			String output = project.getOutputVideo();
			if (output == null) {
				output = "output.avi";
			}
			
			createMeltFile(consumerRender.replace("${1}", output), "melt.sh");
		}
		
		// TODO Audio: start audio from second x to second y?
		copyMP3();
	
		
		executeMELT("sh melt.sh");
		
		LOGGER.info("Runtime for {}: {}", project.getOutputVideo(), TimeUtil.getMessage(startTime));
	}
	
	private void copyMP3() throws IOException {
		if (project.getAudiotrack() != null) {
			int count = 0;
			for (MIFAudioFile audio : project.getAudiotrack().getAudiofiles()) {
				count++;
				
				execute("cp "+audio.getAudiofile()+" "+project.getWorkingDir()+"temp.mp3");
				
				String input = project.getWorkingDir()+"temp.mp3";
				String output = project.getWorkingDir()+count+"_mp3.mp3";
				// -y: overwrite
				execute("ffmpeg -y -i "+input+" -ss "+audio.getEncodeStart()+" -t "+audio.getEncodeEnde()+" "+output);
				
				// TODO Audio: Fadein/Fadeout
			}
		}
	}
	
	private void createMeltFile(String consumer, String scriptName) {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("#/!/usr/bin/env bash\n\n");
			sb.append("melt \\\n");
			
			int count = 0;
			for (MIFFile meltfile : this.project.getMIFFiles()) {
				String input = project.getWorkingDir()+"scaled/"+meltfile.getFilename();
				
				boolean useMixer = count != 0;
				
				if (input.endsWith("jpg") || input.endsWith("JPG")) {
					sb.append("   ").append(input).append(" in=0 out=").append(meltfile.getFramelength()-1);
				} else if (input.endsWith("mp4") || input.endsWith("MP4")) {
					sb.append("   ").append(input).append(" in=0 out=").append(meltfile.getFramelength()-1);
				}
				if (useMixer && meltfile.getOverlayToPrevious() > 0) {
					sb.append(" -mix ").append(meltfile.getOverlayToPrevious()).append(" -mixer luma");
				}
				sb.append(" \\\n");
				count++;
			}
			sb.append("\\\n");
			if (!project.getAudiotrack().getAudiofiles().isEmpty()) {
				sb.append(" -audio-track ");
				for (int i=0; i<=project.getAudiotrack().getAudiofiles().size()-1; i++) {
					sb.append(" "+(i+1)+"_mp3.mp3 "); // TODO Audio: Overlay
				}
				sb.append(" \\\n");
			}
			sb.append(" -profile "+project.getProfile()+" \\\n");
			sb.append(consumer);
			
			FileUtils
				.writeStringToFile(
					new File(project.getWorkingDir()+""+scriptName), 
					sb.toString(),
					Charset.defaultCharset()
			);
			
		} catch (IOException e) {
			LOGGER.error("", e);
		}
	}
	
	private void adjustImagesIfNecessary() throws IOException {
		LOGGER.info("Start adjusting images (if necessary)...");
		
		String workingDir = project.getWorkingDir();
		for (MIFFile f : project.getMIFFiles()) {
			String filename = f.getFilename();
			String input = workingDir+"orig/"+filename;
			String output = workingDir + "scaled/" + filename;

			if (f instanceof MIFImage) {
				if (!new File(output).exists()) {
					LOGGER.info("Create {}", output);

					switch (((MIFImage) f).getStyle()) {
					case "CROP":
						// TODO Image Crop convert
					case "HARD":
						// TODO Image Hard convert
					case "FILL":
						execute("convert "+input+" -geometry 1920x -crop 1920x1080+0+180 -quality 100 "+output);
						break;
					case "MANUAL":
						execute("convert "+input+" "+((MIFImage) f).getManualStyleCommand()+" "+output);
						break;
					}
				}
			} else if (f instanceof MIFVideo) {
				if (!new File(output).exists()) {
					LOGGER.info("Create {}", output);
					
					execute("cp "+input+" "+output);
				}
			}
		}
	}
	
	private void execute(String command) throws IOException {
		execute(command, true);
	}
	
	private void executeMELT(String command) throws IOException {
		Process process = createProcess(command);
		
		Runnable r = () -> {
			try {
				
				try (BufferedReader reader = new BufferedReader(
					    new InputStreamReader(process.getInputStream()))) {
					String line;
					Set<Integer> percentages = new HashSet<>();
					while ((line = reader.readLine()) != null) {
						
						
						int index = line.indexOf(", percentage");
						if (index != -1) {
							line = line.substring(index+", percentage".length()+1);
							line.replace(" ", "");
							line = line.trim();
							
							int i = Integer.parseInt(line);
							if (!percentages.contains(i)) {
								LOGGER.info("{}% done...", i);
								percentages.add(i);
							}
						} else {
							LOGGER.info(line);
						}
					}
				}	
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		};
		new Thread(r).start();
		
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void execute(String command, boolean logOutput) throws IOException {
		Process process = createProcess(command);
		
		if (logOutput) {
			new Thread(() -> logProcess(process)).start();
		}
		
		try {
			process.waitFor();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private Process createProcess(String command) throws IOException {
		return new ProcessBuilder("bash", "-c", command)
			.directory(new File(project.getWorkingDir()))
			.redirectErrorStream(true)
			.start();
	}
	
	private void logProcess(Process process) {
		try (BufferedReader reader = new BufferedReader(
			    new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				LOGGER.info(line);
			}
		} catch (IOException e) {
			LOGGER.error("", e);
		}		
	}

}

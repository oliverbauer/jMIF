package io.github.jmif.builder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.MeltFilter;
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

				if (audio.isNormalize()) {
					/*
					 * ffmpeg -y -i .../temp.mp3 -ss 0 -t 10  -filter:a loudnorm .../1_xx_mp3.mp3
					 * ffmpeg -y -i .../1_xx_mp3.mp3 -ss 0 -t 10 -filter_complex "afade=d=1, areverse, afade=d=1, areverse" .../1_mp3.mp3
					 * 
					 * or, if no fadein/fadeout defined, directly to
					 * 
					 * ffmpeg -y -i .../temp.mp3 -ss 0 -t 10  -filter:a loudnorm .../1_mp3.mp3
					 * 
					 * Two commands are necessary because of:
					 * 
					 * Filtergraph 'loudnorm' was specified through the -vf/-af/-filter option for output stream 0:0, which is fed from a complex filtergraph.
					 * -vf/-af/-filter and -filter_complex cannot be used together for the same stream.
					 */
					StringBuilder sb = new StringBuilder("ffmpeg -y -i ");
					sb.append(input);
					sb.append(" -ss ");
					sb.append(audio.getEncodeStart());
					sb.append(" -t ");
					sb.append(audio.getEncodeEnde());
					sb.append(" ");
					if (audio.isNormalize()) {
						sb.append(" -filter:a loudnorm ");
					}
					String tempOutput = output;
					if (audio.getFadeIn() > 0 || audio.getFadeOut() > 0) {
						tempOutput = project.getWorkingDir()+count+"_xx_mp3.mp3";
					}
					sb.append(tempOutput);
					
					String command = sb.toString();
					LOGGER.info("Execute {}",command);
					execute(command);
					
					if (audio.getFadeIn() > 0 || audio.getFadeOut() > 0) {
						StringBuilder sb2 = new StringBuilder("ffmpeg -y -i ");
						sb2.append(tempOutput);
						sb2.append(" -ss ");
						sb2.append(audio.getEncodeStart());
						sb2.append(" -t ");
						sb2.append(audio.getEncodeEnde());
						sb2.append(" -filter_complex \"");
						if (audio.getFadeIn() > 0) {
							sb2.append("afade=d=");
							sb2.append(audio.getFadeIn());
						}
						if (audio.getFadeIn() > 0 && audio.getFadeOut()> 0) {
							sb2.append(", ");
						}
						if (audio.getFadeOut() > 0) {
							sb2.append("areverse, afade=d=");
							sb2.append(audio.getFadeOut());
							sb2.append(", areverse");
						}
						sb2.append("\" ");
						sb2.append(output);
						
						String command2 = sb2.toString();
						LOGGER.info("Execute {}",command2);
						execute(command2);
					}
				} else {
					StringBuilder sb = new StringBuilder("ffmpeg -y -i ");
					sb.append(input);
					sb.append(" -ss ");
					sb.append(audio.getEncodeStart());
					sb.append(" -t ");
					sb.append(audio.getEncodeEnde());
					sb.append(" ");
					if (audio.getFadeIn() > 0 || audio.getFadeOut() > 0) {
						sb.append("-filter_complex \"");
						if (audio.getFadeIn() > 0) {
							sb.append("afade=d=");
							sb.append(audio.getFadeIn());
						}
						if (audio.getFadeIn() > 0 && audio.getFadeOut()> 0) {
							sb.append(", ");
						}
						if (audio.getFadeOut() > 0) {
							sb.append("areverse, afade=d=");
							sb.append(audio.getFadeOut());
							sb.append(", areverse");
						}
						sb.append("\" ");
					}
					sb.append(output);
					
					String command = sb.toString();
					LOGGER.info("Execute {}",command);
					execute(command);
				}
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
				String extension = meltfile.getFileExtension();
				
				boolean useMixer = count != 0;
				int frames = (int)((meltfile.getDuration() / 1000d) * project.getProfileFramerate());
				
				if (Configuration.allowedImageTypes.contains(extension)) {
					sb.append("   ").append(input).append(" in=0 out=").append(frames - 1);
					
					for (MeltFilter currentlyAddedFilters : meltfile.getFilters()) {
						sb.append(" -attach-cut ");
						sb.append(currentlyAddedFilters.getFiltername());
						sb.append(" ");
						Map<String, String> filterUsage = currentlyAddedFilters.getFilterUsage();
						for (String v : filterUsage.keySet()) {
							sb.append(v).append("=").append(filterUsage.get(v)).append(" ");
						}				
					}
					
				} else if (Configuration.allowedVideoTypes.contains(extension)) {
					sb.append("   ").append(input).append(" in=0 out=").append(frames - 1);
				} else {
					// Exception
					LOGGER.error("Unsupported file extension {}", extension);
				}
				if (useMixer && meltfile.getOverlayToPrevious() > 0) {
					int overlay = (int)((meltfile.getOverlayToPrevious() / 1000d) * project.getProfileFramerate());
					sb.append(" -mix ").append(overlay).append(" -mixer luma");
				}
				sb.append(" \\\n");
				count++;
			}
			sb.append("\\\n");
			if (!project.getAudiotrack().getAudiofiles().isEmpty()) {
				sb.append(" -audio-track ");
				for (int i=0; i<=project.getAudiotrack().getAudiofiles().size()-1; i++) {
					MIFAudioFile mifAudioFile = project.getAudiotrack().getAudiofiles().get(i);
					int seconds = mifAudioFile.getEncodeEnde() - mifAudioFile.getEncodeStart();
					int frames = (seconds * project.getProfileFramerate() - 1);
					
					sb.append(" "+(i+1)+"_mp3.mp3 in=0 out="+frames+" "); // TODO Audio: Overlay
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
					case CROP:
						// TODO Image Crop convert
					case HARD:
						// TODO Image Hard convert
					case FILL:
						int w = project.getProfileWidth();  // e.g. 1920
						int h = project.getProfileHeight(); // e.g. 1080
						/*
						 * Cf. https://stackoverflow.com/questions/21262466/imagemagick-how-to-minimally-crop-an-image-to-a-certain-aspect-ratio
						 */
						execute("convert "+input+" -geometry "+w+"x"+h+"^ -gravity center -crop "+w+"x"+h+"+0+0 -quality 100 "+output);
						break;
					case MANUAL:
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
	
	public void execute(String command) throws IOException {
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

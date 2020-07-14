package io.github.jmif.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFAudio;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.MeltFilter;
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
		
		var consumer = consumerFrameExport
			.replace("${1}", output)
			.replace("${2}", String.valueOf(frame))
			.replace("${3}", String.valueOf(frame))	;
		
		createMeltFile(consumer, "melt-frameexport.sh");
		executeMELT("sh melt-frameexport.sh");
	}
	
	public void convert(boolean preview) throws IOException {
		var startTime = System.currentTimeMillis();
		
		adjustImagesIfNecessary();
		if (preview) {
			createMeltFile(consumerPreview, "melt.sh");
		} else {
			// TODO Make sure that outputVideo never is null
			var output = project.getOutputVideo();
			if (output == null) {
				output = "output.avi";
			}
			
			createMeltFile(consumerRender.replace("${1}", output), "melt.sh");
		}
		
		copyMP3();
		executeMELT("sh melt.sh");
		
		LOGGER.info("Runtime for {}: {}", project.getOutputVideo(), TimeUtil.getMessage(startTime));
	}
	
	/**
	 * All {@link MIFFile} that are reflected with this {@link MIFTextFile} needs to be provided.
	 * 
	 * @param files
	 * @param geometry
	 * @throws IOException
	 */
	public void affineTextPreview(MIFProject pr, MIFTextFile textFile) throws IOException {
		int skipFramesOfFirstMIFFile = -1;
		
		int millis = textFile.getLength();
		int framesPerSecond = pr.getProfileFramerate();
		int framelengthOfText = (millis*framesPerSecond/1000);
		
		int startframeoftext = 0;
		if (!project.getTexttrack().getEntries().isEmpty()) {
			for (MIFTextFile t : project.getTexttrack().getEntries()) {
				if (t == textFile) {
					break;
				}
				int f = (t.getLength()/1000)*pr.getProfileFramerate();
				startframeoftext += f;
			}
		}
		LOGGER.debug("startframe of text="+startframeoftext);
		LOGGER.debug("num frames of text="+framelengthOfText);
		int currentF = 0;
		List<MIFFile> involvedFiles = new ArrayList<>();
		Map<MIFFile, Integer> from = new HashMap<>();
		Map<MIFFile, Integer> to = new HashMap<>();

		for (MIFFile meltfile : this.project.getMIFFiles()) {
			var f = (int)((meltfile.getDuration() / 1000d) * project.getProfileFramerate());
			
//			System.err.println("-check "+meltfile.getDisplayName());
//			System.err.println("- currentF = "+currentF+", f = "+f);
			
			if (currentF >= framelengthOfText+startframeoftext) {
//				involvedFiles.add(meltfile);
				break;
			}
			
			if (currentF + f < startframeoftext) {
				// to early...
				currentF += f;
				
			} else if (currentF + f > startframeoftext) {
				involvedFiles.add(meltfile);
				from.put(meltfile, currentF);
				currentF += f;
				to.put(meltfile, currentF);
			} else if (currentF >= startframeoftext) {
				from.put(meltfile, currentF);
				involvedFiles.add(meltfile);
				to.put(meltfile, currentF);
				currentF += f;
			} else {
				currentF += f;
			}
			// TODO aber nur wenn es nicht das erste file ist oder?
			currentF -= (int)((meltfile.getOverlayToPrevious() / 1000d) * project.getProfileFramerate());
		}
		for (MIFFile f : involvedFiles) {
			LOGGER.debug("Invoved files: "+f.getDisplayName()+" "+from.get(f)+"->"+to.get(f));
		}
		// So skip 125 frames of fist video...
		skipFramesOfFirstMIFFile = startframeoftext - from.get(involvedFiles.get(0));
		LOGGER.debug("Skip "+skipFramesOfFirstMIFFile+" frames of first file...");
		
		// TODO Skip some frames of last frame...
		
		
		var sb = new StringBuilder();
		sb.append("#/!/usr/bin/env bash\n\n");
		sb.append("melt \\\n");
		
		sb.append("color:black out=1 \\\n"); // FIXME what is the overall framelength?
		
		sb.append("-track \\\n");
		for (int i=0; i<=involvedFiles.size()-1; i++) {
			var meltfile = involvedFiles.get(i);
			var file = meltfile.getFile();
			var input = project.getWorkingDir()+"scaled/"+file.getName();
			var extension = FilenameUtils.getExtension(file.getName());
			
			var useMixer = i != 0;
			var frames = (int)((meltfile.getDuration() / 1000d) * project.getProfileFramerate());
			
			if (Configuration.allowedImageTypes.contains(extension) || Configuration.allowedVideoTypes.contains(extension)) {
				if (i== 0) {
					sb.append("   ").append(input).append(" in="+skipFramesOfFirstMIFFile+" out=").append(frames - 1);
				} else {
					sb.append("   ").append(input).append(" in=0 out=").append(frames - 1);
				}
				
				for (MeltFilter currentlyAddedFilters : meltfile.getFilters()) {
					sb.append(" -attach ");
					sb.append(currentlyAddedFilters.getFiltername());
					sb.append(" ");
					var filterUsage = currentlyAddedFilters.getFilterUsage();
					for (String v : filterUsage.keySet()) {
						sb.append(v).append("=").append(filterUsage.get(v)).append(" ");
					}				
				}
				
			} else {
				// Exception
				LOGGER.error("Unsupported file extension {}", extension);
			}
			if (useMixer && meltfile.getOverlayToPrevious() > 0) {
				var overlay = (int)((meltfile.getOverlayToPrevious() / 1000d) * project.getProfileFramerate());
				sb.append(" -mix ").append(overlay).append(" -mixer luma");
			}
			sb.append(" \\\n");
		}
		sb.append("\\\n");
		sb.append("-track \\\n");
		
		sb.append(" pango: \\\n");
			
		sb.append(String.format(" text=\"%s\" bgcolour=%s fgcolour=%s olcolour=%s out=%s size=%s weight=%s \\\n", 
			textFile.getText(), 
			textFile.getBgcolour(), 
			textFile.getFgcolour(),
			textFile.getOlcolour(), 
			(textFile.getLength()/1000)*project.getProfileFramerate(), 
			textFile.getSize(), 
			textFile.getWeight()));
		sb.append("  -attach affine transition.fill=0 transition.distort=1 transition.geometry=\" \n");
		sb.append(textFile.getAffineTransition());
		sb.append("\" \\\n");
		
		
		sb.append(" -transition mix:-1 always_active=1 a_track=0 b_track=1 sum=1  \\\n");
		sb.append(" -transition frei0r.cairoblend a_track=0 b_track=1 disable=0 \\\n");
		if (!project.getTexttrack().getEntries().isEmpty()) {
			sb.append(" -transition affine a_track=0 b_track=2 \\\n");
		}
		
		sb.append(" -profile "+project.getProfile()+" \\\n");
		sb.append(" -consumer sdl2 terminate_on_pause=1");
		
		String meltFile = sb.toString();
		
		
		LOGGER.info(meltFile);
		
		FileUtils
			.writeStringToFile(
				new File(project.getWorkingDir()+"test.sh"), 
				meltFile,
				Charset.defaultCharset()
		);
		
		adjustImagesIfNecessary();
		
		executeMELT("sh test.sh");
	}
	
	private void copyMP3() throws IOException {
		if (project.getAudiotrack() != null) {
			var count = 0;
			for (MIFAudio audio : project.getAudiotrack().getAudiofiles()) {
				count++;
				
				var filename = audio.getAudiofile().getAbsolutePath()
					.replace(" ", "\\ ")
					.replace("(", "\\(")
					.replace(")", "\\)");
				
				execute("cp "+filename+" "+project.getWorkingDir()+"temp.mp3");
				
				var input = project.getWorkingDir()+"temp.mp3";
				var output = project.getWorkingDir()+count+"_mp3.mp3";

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
					
					// TODO Audio: Allow with milliseconds
					var sb = new StringBuilder("ffmpeg -y -i ");
					sb.append(input);
					sb.append(" -ss ");
					sb.append(DurationFormatUtils.formatDuration(audio.getEncodeStart(), "HH:mm:ss"));
					sb.append(" -t ");
					sb.append(DurationFormatUtils.formatDuration(audio.getEncodeEnde(), "HH:mm:ss"));
					sb.append(" ");
					if (audio.isNormalize()) {
						sb.append(" -filter:a loudnorm ");
					}
					var tempOutput = output;
					if (audio.getFadeIn() > 0 || audio.getFadeOut() > 0) {
						tempOutput = project.getWorkingDir()+count+"_xx_mp3.mp3";
					}
					sb.append(tempOutput);
					
					execute(sb.toString(), false);
					
					if (audio.getFadeIn() > 0 || audio.getFadeOut() > 0) {
						// TODO Audio: Allow with milliseconds
						var sb2 = new StringBuilder("ffmpeg -y -i ");
						sb2.append(tempOutput);
						sb2.append(" -ss ");
						sb2.append(DurationFormatUtils.formatDuration(audio.getEncodeStart(), "HH:mm:ss"));
						sb2.append(" -t ");
						sb2.append(DurationFormatUtils.formatDuration(audio.getEncodeEnde(), "HH:mm:ss"));
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
						
						execute(sb2.toString(), false);
					}
				} else {
					// TODO Audio: Allow with milliseconds
					var sb = new StringBuilder("ffmpeg -y -i ");
					sb.append(input);
					sb.append(" -ss ");
					sb.append(DurationFormatUtils.formatDuration(audio.getEncodeStart(), "HH:mm:ss"));
					sb.append(" -t ");
					sb.append(DurationFormatUtils.formatDuration(audio.getEncodeEnde(), "HH:mm:ss"));
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
					
					execute(sb.toString(), false);
				}
			}
		}
	}
	
	private void createMeltFile(String consumer, String scriptName) {
		try {
			var sb = new StringBuilder();
			sb.append("#/!/usr/bin/env bash\n\n");
			sb.append("melt \\\n");
			
			sb.append("color:black out=1 \\\n"); // FIXME what is the overall framelength?
			
			var count = 0;
			sb.append("-track \\\n");
			for (MIFFile meltfile : this.project.getMIFFiles()) {
				var filename = meltfile.getFile().getName()
					.replace(" ", "\\ ")
					.replace("(", "\\(")
					.replace(")", "\\)");
				var input = project.getWorkingDir()+"scaled/"+filename;
				var extension = FilenameUtils.getExtension(filename);
				
				var useMixer = count != 0;
				var frames = (int)((meltfile.getDuration() / 1000d) * project.getProfileFramerate());
				
				if (Configuration.allowedImageTypes.contains(extension) || Configuration.allowedVideoTypes.contains(extension)) {
					sb.append("   ").append(input).append(" in=0 out=").append(frames - 1);
					
					for (MeltFilter currentlyAddedFilters : meltfile.getFilters()) {
						sb.append(" -attach-cut ");
						sb.append(currentlyAddedFilters.getFiltername());
						sb.append(" ");
						var filterUsage = currentlyAddedFilters.getFilterUsage();
						for (String v : filterUsage.keySet()) {
							sb.append(v).append("=").append(filterUsage.get(v)).append(" ");
						}				
					}
					
				} else {
					// Exception
					LOGGER.error("Unsupported file extension {}", extension);
				}
				if (useMixer && meltfile.getOverlayToPrevious() > 0) {
					var overlay = (int)((meltfile.getOverlayToPrevious() / 1000d) * project.getProfileFramerate());
					sb.append(" -mix ").append(overlay).append(" -mixer luma");
				}
				sb.append(" \\\n");
				count++;
			}
			sb.append("\\\n");
			// Add text
			if (!project.getTexttrack().getEntries().isEmpty()) {
				sb.append("-track \\\n");
				
				for (MIFTextFile textFile : project.getTexttrack().getEntries()) {
					sb.append(" pango: \\\n");
					
					sb.append(String.format(" text=\"%s\" bgcolour=%s fgcolour=%s olcolour=%s out=%s size=%s weight=%s \\\n", 
						textFile.getText(), 
						textFile.getBgcolour(), 
						textFile.getFgcolour(),
						textFile.getOlcolour(), 
						(textFile.getLength()/1000)*project.getProfileFramerate(), 
						textFile.getSize(), 
						textFile.getWeight()));
					if (textFile.isUseAffineTransition()) {
						sb.append("  -attach affine transition.fill=0 transition.distort=1 transition.geometry=\" \n");
						sb.append(textFile.getAffineTransition());
						sb.append("\" \\\n");
					} else {
						sb.append("  -attach affine transition.valign="+textFile.getValign()+" transition.halign="+textFile.getHalign()+" transition.fill=0 \\\n");
					}
				}
			}
			
			if (!project.getAudiotrack().getAudiofiles().isEmpty()) {
				sb.append(" -audio-track ");
				for (var i=0; i<=project.getAudiotrack().getAudiofiles().size()-1; i++) {
					var mifAudioFile = project.getAudiotrack().getAudiofiles().get(i);
					var millis = mifAudioFile.getEncodeEnde() - mifAudioFile.getEncodeStart();
					var frames = ((millis/1000) * project.getProfileFramerate() - 1);
					
					sb.append(" "+(i+1)+"_mp3.mp3 in=0 out="+frames+" "); // TODO Audio: Overlay
				}
				sb.append(" \\\n");
			}
			sb.append(" \\\n");
			
			sb.append(" -transition mix:-1 always_active=1 a_track=0 b_track=1 sum=1  \\\n");
			sb.append(" -transition frei0r.cairoblend a_track=0 b_track=1 disable=0 \\\n");
			if (!project.getTexttrack().getEntries().isEmpty()) {
				sb.append(" -transition affine a_track=0 b_track=2 \\\n");
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
		var workingDir = project.getWorkingDir();
		for (MIFFile f : project.getMIFFiles()) {
			var filename = f.getFile().getName()
					.replace(" ", "\\ ")
					.replace("(", "\\(")
					.replace(")", "\\)");
			
			var input = workingDir+"orig/"+filename;
			var output = workingDir + "scaled/" + filename;

			if (f instanceof MIFImage) {
				if (!new File(output).exists()) {
					createFinalImageConversion((MIFImage)f, output);
				}
			} else if (f instanceof MIFVideo) {
				if (!new File(output).exists()) {
					execute("cp "+input+" "+output);
				}
			}
		}
	}
	
	public void createFinalImageConversion(MIFImage image, String output) throws IOException {
		var workingDir = project.getWorkingDir();
		var filename = image.getFile().getName();
		var input = workingDir+"orig/"+filename;
		
		var w = project.getProfileWidth();  // e.g. 1920
		var h = project.getProfileHeight(); // e.g. 1080

		switch (image.getStyle()) {
		case CROP:
			/*
			 * Cf. 
			 * https://stackoverflow.com/questions/21262466/imagemagick-how-to-minimally-crop-an-image-to-a-certain-aspect-ratio
			 * http://www.fmwconcepts.com/imagemagick/aspectcrop/index.php
			 */
			execute("convert "+input+" -geometry "+w+"x"+h+"^ -gravity center -crop "+w+"x"+h+"+0+0 -quality 100 "+output);
			break;
		case MANUAL:
			execute("convert "+input+" "+image.getManualStyleCommand()+" "+output);
			break;
		}
	}
	
	public void execute(String command) throws IOException {
		LOGGER.info("Execute: {}", command);
		execute(command, true);
	}
	
	private void executeMELT(String command) throws IOException {
		var process = createProcess(command);
		
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
			LOGGER.info("Completed... {}", command);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void execute(String command, boolean logOutput) throws IOException {
		LOGGER.info("Execute: {}", command);
		
		var process = createProcess(command);
		
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
		try (var reader = new BufferedReader(
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

package io.github.jmif;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.builder.MIFProjectExecutor;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFImage.ImageResizeStyle;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.MeltFilter;
import io.github.jmif.melt.Melt;
import io.github.jmif.melt.MeltFilterDetails;

/**
 * @author thebrunner
 */
public class Service {

	private static final Logger logger = LoggerFactory.getLogger(Service.class);

	public void exportImage(MIFProject pr, String output, int frame) throws MIFException {
		try {
			new MIFProjectExecutor(pr).exportImage(output, frame);
		} catch (IOException e) {
			throw new MIFException(e);
		}
	}

	public void convert(MIFProject pr, boolean preview) throws MIFException {
		try {
			new MIFProjectExecutor(pr).convert(preview);
		} catch (IOException e) {
			throw new MIFException(e);
		}
	}

	public void updateFramerate(MIFProject project) {
		var framerate = project.getFramerate();
		try {
			Process process = new ProcessBuilder("bash", "-c", "melt -query \"profile\"="+project.getProfile()).start();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("frame_rate_num:")) {
						framerate = Integer.parseInt(line.substring(line.indexOf(": ")+1).trim());
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to extract framerate for "+project.getProfile(), e);
		}

		logger.info("Set profile to {} (framerate {})", project.getProfile(), framerate);
		project.setFramerate(framerate);
	}

	public void createWorkingDirs(MIFProject project) {
		if (!project.getWorkingDir().endsWith("/")) {
			project.setWorkingDir(project.getWorkingDir()+"/");
		}

		logger.info("WorkingDir '{}'", project.getWorkingDir());

		new File(project.getWorkingDir()).mkdirs();

		var orig = new File(project.getWorkingDir()+"orig/").mkdirs();    // Copy of the original file
		var prevew = new File(project.getWorkingDir()+"preview/").mkdirs(); // preview files for ui
		var scaled = new File(project.getWorkingDir()+"scaled/").mkdirs();  // HQ for preview-video/video
		if (orig) {
			logger.info("Created dir 'orig' within {}", project.getWorkingDir());
		}
		if (prevew) {
			logger.info("Created dir 'preview' within {}", project.getWorkingDir());
		}
		if (scaled) {
			logger.info("Created dir 'scaled' within {}", project.getWorkingDir());
		}
	}

	public MIFVideo createVideo(File file, String display, float frames, String dim, int overlay, String workingDir, int profileFramelength) throws MIFException {
		var video = new MIFVideo(file, display, frames, dim, overlay);
		updateFile(video);
		copy(video, workingDir);
		var filename = video.getFilename();

		logger.info("Init: Video {}", filename);
		Process process;
		String command;
		try {
			// Audio Stream
			// codec_long_name=AAC (Advanced Audio Coding)
			// bit_rate=128771 => 128 kbps
			command = "ffprobe -v error -select_streams a:0 -show_entries stream=bit_rate,codec_long_name -of default=noprint_wrappers=1 " + video.getFile();
			process = new ProcessBuilder("bash", "-c", command)
					.directory(file.getParentFile())
					.redirectErrorStream(true)
					.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("codec_long_name")) {
						video.setAudioCodec(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("bit_rate")) {
						String value = line.substring(line.indexOf('=')+1);
						video.setAudioBitrate(Integer.valueOf(value) / 1000);
					}
				}
			}
			// Video Stream
			// codec_long_name=H.265 / HEVC (High Efficiency Video Coding)
			// width=1920
			// height=1080
			// display_aspect_ratio=16:9
			// duration=7.000000
			// bit_rate=2504857  => 2504 kbps
			command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height,duration,bit_rate,codec_long_name,display_aspect_ratio,r_frame_rate -of default=noprint_wrappers=1 " + video.getFile();
			process = new ProcessBuilder("bash", "-c", command)
					.directory(file.getParentFile())
					.redirectErrorStream(true)
					.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					
					if (line.startsWith("width")) {
						video.setWidth(Integer.parseInt(line.substring(line.indexOf('=')+1)));
					} else if (line.startsWith("height")) {
						video.setHeight(Integer.parseInt(line.substring(line.indexOf('=')+1)));
					} else if (line.startsWith("duration")) {
						line = line.substring(line.indexOf('=')+1);
						String s = line.substring(0, line.indexOf('.') + 2);
						video.setFramelength(Float.parseFloat(s) * profileFramelength);
					} else if (line.startsWith("bit_rate")) {
						video.setVideoBitrate(Integer.parseInt(line.substring(line.indexOf('=')+1)) / 1000);
					} else if (line.startsWith("codec_long_name")) {
						video.setVideoCodec(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("display_aspect_ratio")) {
						video.setAr(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("r_frame_rate")) {
						String s = line.substring(line.indexOf('=')+1, line.indexOf('/'));
						video.setFps(Integer.parseInt(s));
					}
				}
			}				
		} catch (IOException e) {
			logger.error("Unable to get video details", e);
		}

		// set images...
		for (int i = 1; i <= 10; i++) {
			video.addPreviewImage(workingDir+"/preview/"+"_low_"+filename+"_"+i+".png");
		}

		return video;
	}

	public void createPreview(MIFFile file, String workingDir) throws MIFException {
		if (file instanceof MIFImage) {
			createPreview(MIFImage.class.cast(file), workingDir);
		} else if (file instanceof MIFVideo) {
			createPreview(MIFVideo.class.cast(file), workingDir);
		}
	}

	private void createPreview(MIFVideo video, String workingDir) throws MIFException {
		var filename = video.getFile().getName();
		var videoFileName = workingDir+"/orig/"+filename;

		if (video.getWidth() == -1 || video.getHeight() == -1) {
			logger.info("Init: Check Width/Height");
			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 " + videoFileName;
			Process process;
			try {
				process = new ProcessBuilder("bash", "-c", command)
						.directory(new File(workingDir))
						.redirectErrorStream(true)
						.start();
			} catch (IOException e) {
				throw new MIFException(e);
			}

			String output = null;

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output = line;
				}
			} catch (IOException e) {
				throw new MIFException(e);
			}
			video.setWidth(Integer.valueOf(output.substring(0, output.indexOf('x'))));
			video.setHeight(Integer.valueOf(output.substring(output.indexOf('x') + 1)));
		} else {
			logger.debug("Init: Width/Height already known");
		}

		/*
		 * Preview-Video in low quality 
		 */

		var videoLowQuality = workingDir+"/preview/_low_"+filename;
		if (!new File(videoLowQuality).exists()) {
			logger.info("Init: Create Preview-Video (low quality) {}", videoLowQuality);
			var command = "ffmpeg -y -i " + videoFileName + " -vf scale=320:-1 -c:v libx264 -crf 0 -an -r 25 -preset ultrafast "+videoLowQuality;
			try {
				new ProcessBuilder("bash", "-c", command)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}
		} else {
			logger.debug("Init: Preview-Video already computed");
		}

		/*
		 * 10 images from the video 
		 */
		video.getPreviewImages().clear();
		var cnt = (int) (video.getFramelength() / 10);
		for (int i = 1; i <= 10; i++) {
			var image = workingDir+"/preview/_low_"+filename+"_"+i+".png";
			if (!new File(image).exists()) {
				logger.info("Init: Create Preview-Video-Image {}", image);

				var command = "ffmpeg -y -i " + videoLowQuality + " -vf \"select=eq(n\\," + (i * cnt) + ")\" -vframes 1 "+ videoLowQuality + "_" + i + ".png";
				try {
					new ProcessBuilder("bash", "-c", command)
					.directory(new File(workingDir))
					.redirectErrorStream(true)
					.start()
					.waitFor();
				} catch (InterruptedException | IOException e) {
					throw new MIFException(e);
				}
				video.addPreviewImage(image);
			} else {
				logger.debug("Init: Preview-Video-Image {} already computed", image);
			}
		}
	}

	public void createManualPreview(MIFImage image) {
		image.setStyle(ImageResizeStyle.MANUAL);
		Process process;
		try {
			String temp = image.getManualStyleCommand();
			temp = temp.replace("geometry 1920", "geometry "+image.getPreviewWidth()+"x");

			logger.info("Execute {}", temp);

			process = new ProcessBuilder("bash", "-c", "convert "+image.getFile()+" "+temp+" "+image.getPreviewManual())
				.redirectErrorStream(true)
				.start();
			
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info(line);
				}
			}
			
			process.waitFor();
		} catch (Exception e) {
			logger.error("Unable to create manual style image ",e);
		}
	}

	public MIFImage createImage(File file, String display, float frames, String dim, int overlay, String workingDir, int framelength) throws MIFException {
		var image = new MIFImage(file, display, frames, dim, overlay);
		updateFile(image);
		copy(image, workingDir);
		if (framelength == -1) {
			framelength = 5*framelength; // constant, default: 5 seconds...
		}

		var filename = image.getFilename();

		var copy = workingDir + "orig/" + filename;

		if (image.getWidth() == -1 || image.getHeight() == -1) {
			logger.debug("Init: Check Width/Height of '{}'", filename);

			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "	+ copy;
			Process process;
			try {
				process = new ProcessBuilder("bash", "-c", command)
					.directory(file.getParentFile())
					.redirectErrorStream(true)
					.start();
				String output = null;

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output = line;
					}
				}
				image.setWidth(Integer.valueOf(output.substring(0, output.indexOf('x'))));
				image.setHeight(Integer.valueOf(output.substring(output.indexOf('x') + 1)));
			} catch (IOException e) {
				logger.error("Unable to get image dimension", e);
			}
		} else {
			logger.debug("Init: Check Width/Height of '{}' already available", copy);
		}

		image.setPreviewHeight(image.getHeight() / 16);
		image.setPreviewWidth((int) (image.getPreviewHeight() * 1.78));

		var origHeight = 3888;
		var aspectHeight = origHeight / 16;
		var estimatedWith = (int) (aspectHeight * 1.78);

		var wxh = estimatedWith + "x" + aspectHeight;
		var withoutFileExtension = filename.substring(0, filename.lastIndexOf('.'));
		var fileExtension = image.getFileExtension();
		image.setImagePreview(workingDir + "preview/" + withoutFileExtension + "_thumb." + wxh + "."
				+ fileExtension);
		image.setPreviewHardResize(workingDir + "preview/" + withoutFileExtension + "_hard." + wxh + "."
				+ fileExtension);
		image.setPreviewFillWColor(workingDir + "preview/" + withoutFileExtension + "_fill." + wxh + "."
				+ fileExtension);
		image.setPreviewCrop(workingDir + "preview/" + withoutFileExtension + "_kill." + wxh + "." + fileExtension);
		image.setPreviewManual(workingDir + "preview/" + withoutFileExtension + "_manual." + wxh + "." + fileExtension);
		return image;
	}

	private void createPreview(MIFImage image, String workingDir) throws MIFException {
		var filename = image.getFilename();

		var original = workingDir + "orig/" + filename;

		if (!new File(original).exists()) {
			try {
				new ProcessBuilder("bash", "-c", "cp "+image.getFile()+" "+original)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}
		}

		var origHeight = 3888;
		var aspectHeight = origHeight / 16;
		var estimatedWith = (int) (aspectHeight * 1.78);

		if (!new File(image.getImagePreview()).exists()) {
			// Create preview: convert -thumbnail 200 abc.png thumb.abc.png
			logger.info("Init: Create Preview-Image {}", image.getImagePreview());
			var command = "convert -geometry " + image.getPreviewWidth() + "x " + original + " " + image.getImagePreview();
			try {
				var process = new ProcessBuilder("bash", "-c", command)
						.directory(new File(workingDir))
						.redirectErrorStream(true)
						.start();
				process.waitFor();


				try (BufferedReader reader = new BufferedReader(
						new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						logger.error(line);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException | InterruptedException e) {
				throw new MIFException(e);
			}


			// Preview: 324x243 (Aspect ration) from 5184/16 x 3888/16
		} else {
			logger.debug("Init: Preview-Image {} already exists", image.getImagePreview());
		}

		if (!new File(image.getPreviewHardResize()).exists()) {
			logger.info("Init: Create HARD-Preview-Image {}", image.getPreviewHardResize());

			var hardRescale = "convert " + image.getImagePreview() + " -quality 100 -resize " + estimatedWith + "x"
					+ aspectHeight + "! " + image.getPreviewHardResize();
			try {
				var process = new ProcessBuilder("bash", "-c", hardRescale).directory(new File(workingDir))
						.redirectErrorStream(true).start();
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				throw new MIFException(e);
			}
		} else {
			logger.debug("Init: HARD-Preview-Image {} already exists", image.getPreviewHardResize());
		}

		if (!new File(image.getPreviewFillWColor()).exists()) {
			logger.info("Init: Create FILL-Preview-Image {}", image.getPreviewFillWColor());

			var fill = "convert " + image.getImagePreview() + " -quality 100 -geometry x" + aspectHeight + " fill." + filename;
			try {
				var process = new ProcessBuilder("bash", "-c", fill).directory(new File(workingDir))
						.redirectErrorStream(true).start();
				process.waitFor();

				var fill2 = "convert fill." + filename + " \\( -clone 0 -quality 100 -blur 0x5 -resize " + estimatedWith
						+ "x" + aspectHeight + "\\! -fill black -quality 100 -colorize 100% \\) \\( -clone 0 -resize "
						+ estimatedWith + "x" + aspectHeight + " \\) -delete 0 -gravity center -composite "
						+ image.getPreviewFillWColor();
				process = new ProcessBuilder("bash", "-c", fill2).directory(new File(workingDir)).redirectErrorStream(true)
						.start();
				process.waitFor();

				// rm tempfile...
				process = new ProcessBuilder("bash", "-c", "rm fill." + filename).directory(new File(workingDir))
						.redirectErrorStream(true).start();
				process.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}
		} else {
			logger.debug("Init: FILL-Preview-Image {} already exists", image.getPreviewFillWColor());
		}

		if (!new File(image.getPreviewCrop()).exists()) {
			logger.info("Init: Create CROP-Preview-Image {}", image.getPreviewCrop());

			var kill = "convert " + image.getImagePreview() + " -quality 100 -geometry " + estimatedWith + "x kill." + filename;
			try {
				new ProcessBuilder("bash", "-c", kill)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();

				var kill2 = "convert kill." + filename + " -quality 100 -crop " + estimatedWith + "x" + aspectHeight
						+ "+0+46 " + image.getPreviewCrop();
				new ProcessBuilder("bash", "-c", kill2)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();

				// rm tempfile...
				new ProcessBuilder("bash", "-c", "rm kill." + filename)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}

		} else {
			logger.debug("Init: CROP-Preview-Image {} already exists", image.getPreviewCrop());
		}

		// TODO manual preview if exists
	}

	public MIFAudioFile createAudio(String path) throws MIFException {
		var audioFile = new MIFAudioFile();
		audioFile.setAudiofile(path);
		checkLengthInSeconds(audioFile);
		audioFile.setEncodeStart(0);
		audioFile.setEncodeEnde(audioFile.getLengthOfInput());
		return audioFile;
	}
	
	private void checkLengthInSeconds(MIFAudioFile audio) {
		Process process;
		try {
			String command = "ffprobe -v error -select_streams a:0 -show_entries stream=duration,bit_rate -of default=noprint_wrappers=1:nokey=1 "+audio.getAudiofile();

			process = new ProcessBuilder("bash", "-c", command)
					.redirectErrorStream(true)
					.start();
			process.waitFor();

			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String durationOutput = reader.readLine();
				String bitrateOutput = reader.readLine();

				audio.setLengthOfInput(Integer.parseInt(durationOutput.substring(0, durationOutput.indexOf('.'))));
				audio.setBitrate(Integer.parseInt(bitrateOutput));
			}
		} catch (IOException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		} catch (InterruptedException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		}
	}

	private void updateFile(MIFFile file) {
		file.setFileExists(file.getFile().exists());
		file.setFilename(file.getFile().getName());
	}
	
	private void copy(MIFFile file, String workingDir) throws MIFException {
		var target = workingDir+"orig/"+file.getFilename();
		var command = "cp "+file.getFile()+" "+target;
		
		// Copy to working dir... make sure each file has different name...
		if (!new File(target).exists()) {
			logger.info("Copy to {} ({})", file.getFilename(), command);
			try {
				new ProcessBuilder("bash", "-c", command)
					.directory(new File(workingDir))
					.redirectErrorStream(true)
					.start()
					.waitFor();
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}
		}
	}
	
	public List<String> getProfiles() throws MIFException {
		try {
			List<String> profiles = new ArrayList<>();
			Process process = new ProcessBuilder("bash", "-c", "melt -query \"profiles\"")
				.start();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("_")) {
						line = line.replace(" ", "");
						line = line.replace("-", "");
						line = line.trim();
						profiles.add(line);
					}
				}
			}
			return profiles;
		} catch (IOException e) {
			throw new MIFException(e);
		}
	}
	
	private List<String> getFilters() throws MIFException {
		try {
			List<String> filters = new ArrayList<>();
			Process process = new ProcessBuilder("bash", "-c", "melt -query \"filters\"")
				.start();
			process.waitFor();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith(" ")) {
						line = line.replace(" ", "");
						line = line.replace("-", "");
						line = line.trim();
						filters.add(line);
					}
				}
			}
			return filters;
		} catch (IOException | InterruptedException e) {
			throw new MIFException(e);
		}
	}
	
	private List<String> getFilterDetails(String filter) throws MIFException {
		try {
			List<String> filterDetails = new ArrayList<>();
			Process process = new ProcessBuilder("bash", "-c", "melt -query \"filter\"="+filter)
				.start();
			process.waitFor();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					filterDetails.add(line);
				}
			}
			return filterDetails;
		} catch (IOException | InterruptedException e) {
			throw new MIFException(e);
		}
	}
	
	private List<MeltFilterDetails> save(Melt melt) throws MIFException {
		var meltFilters = melt.getMeltFilterDetails();
		
		// TODO read/write an input xml containing the result of the following expensive output
		try {
			List<String> filterNames = getFilters();
			logger.info("Filter {} loaded", filterNames.size());
			
			for (String filter: filterNames) {
				List<String> details = getFilterDetails(filter);
				
				// E.g. oldfilm has 8 parameter...
				MeltFilterDetails meltFilter = new MeltFilterDetails(filter);
				
				boolean parametersStarted = false;
				boolean valuesStarted = false;
				String currentParameter = null;
				for (String d : details) {
					if (!parametersStarted) {
						if (d.contains(":")) {
							String parts[] = d.split(":");
							
							if (parts.length == 2) {
								meltFilter.addGeneralInformations(parts[0].trim(), parts[1].trim());
							} else {
								meltFilter.addGeneralInformations(parts[0].trim(), "tags...");
							}
						} else {
							if (d.contains("Video")) {
								meltFilter.addGeneralInformations("tags", "Video");
							} else if (d.contains("Audio")) {
								meltFilter.addGeneralInformations("tags", "Audio");
							}
						}
					}
					
					if (d.contains("parameters:")) {
						parametersStarted = true;
						valuesStarted = false;
					} else if (d.contains("identifier:") && parametersStarted) {
						currentParameter = d.substring(d.indexOf("identifier: ")+"identifier: ".length());
						meltFilter.appendConfigurationParameter(currentParameter); // z.B. delta, every, brightnessdelta_up, ...
						valuesStarted = false;
					} else if (d.contains("values") && parametersStarted) {
						valuesStarted = true;
					} else if (parametersStarted) {
						// Extract parameter details...
						d = d.trim();
						if (d.contains(":")) {
							valuesStarted = false;
							
							String key = d.substring(0, d.indexOf(':')).trim();
							String description = d.substring(d.indexOf(':')+1).trim();
	
							meltFilter.appendConfigurationDetail(currentParameter, key, description);
						} else {
							if (valuesStarted) {
								d = d.replace("-", "");
								d = d.trim();
								
								// ... is last line
								if (!d.contentEquals("...")) {
									meltFilter.appendAllowedValueForParameter(currentParameter, d);
								}
							}
						}
					}
				}
				
				logger.info("-{} done", filter);
				
				meltFilters.add(meltFilter);
			}
			
			var context = JAXBContext.newInstance(Melt.class);
			var marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			marshaller.marshal(melt, new File("meltfilterdetails.xml"));
			
			logger.info("Saved 'meltfilterdetails.xml'");
		} catch (Exception e) {
			throw new MIFException(e);
		}
		return meltFilters;
	}
	
	private List<MeltFilterDetails> getOrLoad(Melt melt) throws MIFException {
		var meltFilters = melt.getMeltFilterDetails();
		if (!meltFilters.isEmpty()) {
			return meltFilters;
		}
		
		if (new File("meltfilterdetails.xml").exists()) {
			try {
				var context = JAXBContext.newInstance(Melt.class);
				var unmarshaller = context.createUnmarshaller();
				Melt m = (Melt) unmarshaller.unmarshal(new File("meltfilterdetails.xml"));
			
				meltFilters.addAll(m.getMeltFilterDetails());
				
				logger.info("Loaded 'meltfilterdetails.xml'");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			return meltFilters;
		} else {
			return save(melt);
		}
	}
	
	public List<MeltFilterDetails> getMeltVideoFilterDetails(Melt melt) throws MIFException {
		return getOrLoad(melt)
			.stream()
			.filter(i -> i.getGeneralInformations().containsKey("tags"))
			.filter(i -> i.getGeneralInformations().get("tags").equals("Video"))
			.collect(Collectors.toList());
	}
	
	public List<MeltFilterDetails> getMeltAudioFilterDetails(Melt melt) throws MIFException {
		return getOrLoad(melt)
			.stream()
			.filter(i -> i.getGeneralInformations().containsKey("tags"))
			.filter(i -> i.getGeneralInformations().get("tags").equals("Audio"))
			.collect(Collectors.toList());
	}
	
	public MeltFilterDetails getMeltFilterDetailsFor(Melt melt, MeltFilter meltFilter) throws MIFException {
		return getOrLoad(melt).stream().filter(mdf -> mdf.getFiltername().contentEquals(meltFilter.getFiltername())).findAny().get();
	}
}

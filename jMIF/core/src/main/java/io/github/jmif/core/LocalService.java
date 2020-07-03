
package io.github.jmif.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.entities.MIFAudio;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFImage.ImageResizeStyle;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.entities.melt.Melt;
import io.github.jmif.entities.melt.MeltFilter;
import io.github.jmif.entities.melt.MeltFilterDetails;

/**
 * @author thebrunner
 */
public class LocalService {

	private static final Logger logger = LoggerFactory.getLogger(LocalService.class);

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
	
	public void affineTextPreview(MIFProject pr, MIFTextFile textFile) throws MIFException {
		try {
			new MIFProjectExecutor(pr).affineTextPreview(pr, textFile); 
		} catch (IOException e) {
			throw new MIFException(e);
		}		
	}
	
	public void updateProfile(MIFProject project) {
		var framerate = -1;
		var height = -1;
		var width = -1;
		try {
			var process = new ProcessBuilder("bash", "-c", "melt -query \"profile\"="+project.getProfile()).start();

			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.contains("frame_rate_num:")) {
						framerate = Integer.parseInt(line.substring(line.indexOf(": ")+1).trim());
					}
					if (line.contains("width:")) {
						width = Integer.parseInt(line.substring(line.indexOf(": ")+1).trim());
					}
					if (line.contains("height:")) {
						height = Integer.parseInt(line.substring(line.indexOf(": ")+1).trim());
					}
				}
			}
			if (framerate == -1 || width == -1 || height == -1) {
				throw new IllegalArgumentException(project.getProfile()+" not found");
			}
			
			logger.info("Set profile to {} (framerate {})", project.getProfile(), framerate);
			project.setProfileFramerate(framerate);
			project.setProfileHeight(height);
			project.setProfileWidth(width);
		} catch (Exception e) {
			logger.error("Unable to extract framerate for "+project.getProfile(), e);
		}
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
	
	public MIFVideo createVideo(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		var video = new MIFVideo(file, display, frames, dim, overlay);
		copy(video, workingDir);
		var filename = video.getFile().getName();

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
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.startsWith("codec_long_name")) {
						video.setAudioCodec(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("bit_rate")) {
						var value = line.substring(line.indexOf('=')+1);
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
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					
					if (line.startsWith("width")) {
						video.setWidth(Integer.parseInt(line.substring(line.indexOf('=')+1)));
					} else if (line.startsWith("height")) {
						video.setHeight(Integer.parseInt(line.substring(line.indexOf('=')+1)));
					} else if (line.startsWith("duration")) {
						// E.g. "7.0"
						line = line.substring(line.indexOf('=')+1);
						var s = line.substring(0, line.indexOf('.') + 2);
						var v = (int)(Float.parseFloat(s) * 1000);
						video.setDuration(v);
					} else if (line.startsWith("bit_rate")) {
						video.setVideoBitrate(Integer.parseInt(line.substring(line.indexOf('=')+1)) / 1000);
					} else if (line.startsWith("codec_long_name")) {
						video.setVideoCodec(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("display_aspect_ratio")) {
						video.setAr(line.substring(line.indexOf('=')+1));
					} else if (line.startsWith("r_frame_rate")) {
						var s = line.substring(line.indexOf('=')+1, line.indexOf('/'));
						video.setFps(Integer.parseInt(s));
					}
				}
			}				
		} catch (IOException e) {
			logger.error("Unable to get video details", e);
		}

		// set images...
		for (var i = 1; i <= 10; i++) {
			video.addPreviewImagePath(Paths.get(workingDir).resolve("preview").resolve("_low_"+filename+"_"+i+".png"));
		}

		return video;
	}
	
	public MIFFile createPreview(MIFFile file, String workingDir) throws MIFException {
		if (file instanceof MIFImage) {
			return createImagePreview((MIFImage)(file), workingDir);
		} else if (file instanceof MIFVideo) {
			return createVideoPreview((MIFVideo)(file), workingDir);
		}
		throw new MIFException(new Exception("Cannot parse MIFFile"));
	}

	private MIFVideo createVideoPreview(MIFVideo video, String workingDir) throws MIFException {
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

			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
		video.getPreviewImagesPath().clear();
		var cnt = (int) (video.getDuration() / 1000d);
		for (var i = 1; i <= 10; i++) {
			var image = Paths.get(workingDir).resolve("preview").resolve("_low_"+filename+"_"+i+".png");
			if (!Files.exists(image)) {
				logger.info("Init: Create Preview-Video-Image {}", image);

				var command = "ffmpeg -y -i " + videoLowQuality + " -vf \"select=eq(n\\," + (i * cnt) + ")\" -vframes 1 "+ videoLowQuality + "_" + i + ".png";
				try {
					new ProcessBuilder("bash", "-c", command)
					.directory(new File(workingDir))
					.redirectErrorStream(true)
					.start()
					.waitFor();
					video.addPreviewImage(image, ImageIO.read(image.toFile()));
				} catch (InterruptedException | IOException e) {
					throw new MIFException(e);
				}
			} else {
				logger.debug("Init: Preview-Video-Image {} already computed", image);
			}
		}
		
		return video;
	}
	
	public void createManualPreview(MIFImage image) {
		image.setStyle(ImageResizeStyle.MANUAL);
		Process process;
		try {
			var temp = image.getManualStyleCommand();
			temp = temp.replace("geometry 1920", "geometry "+image.getPreviewWidth()+"x");

			logger.info("Execute {}", temp);

			process = new ProcessBuilder("bash", "-c", "convert "+image.getFile()+" "+temp+" "+image.getPreviewManualPath())
				.redirectErrorStream(true)
				.start();
			
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.info(line);
				}
			}
			
			process.waitFor();
			
			image.setPreviewManual(ImageIO.read(image.getPreviewManualPath().toFile()));
		} catch (Exception e) {
			logger.error("Unable to create manual style image ",e);
		}
	}
	
	public MIFImage createImage(File file, String display, int frames, String dim, int overlay, String workingDir) throws MIFException {
		var image = new MIFImage(file, display, frames, dim, overlay);
		copy(image, workingDir);
		var filename = image.getFile().getName();

		var copy = workingDir + "orig/" + filename;

		if (image.getWidth() == -1 || image.getHeight() == -1) {
			logger.debug("Init: Check Width/Height of '{}'", image);

			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "	+ copy;
			Process process;
			try {
				process = new ProcessBuilder("bash", "-c", command)
					.directory(file.getParentFile())
					.redirectErrorStream(true)
					.start();
				process.waitFor();
				String output = null;

				try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output = line;
					}
				}
				if (!output.contains("x")) {
					logger.error("Unexpected output: {}", output);
				}
				
				image.setWidth(Integer.valueOf(output.substring(0, output.indexOf('x'))));
				image.setHeight(Integer.valueOf(output.substring(output.indexOf('x') + 1)));
				
				image.setPreviewHeight(image.getHeight() / 16);
				image.setPreviewWidth((int) (image.getPreviewHeight() * 1.78));
				
				var origHeight = 3888;
				var aspectHeight = origHeight / 16;
				var estimatedWith = (int) (aspectHeight * 1.78);
				
				var wxh = estimatedWith + "x" + aspectHeight;
				var basename = FilenameUtils.getBaseName(image.getFile().getName());
				var extension = image.getFileExtension();
				image.setImagePreviewPath(Paths.get(workingDir).resolve("preview").resolve(basename + "_thumb."+wxh+"."+
						extension));
				image.setPreviewCropPath(Paths.get(workingDir).resolve("preview").resolve(basename+"_kill."+wxh+"."+extension));
				image.setPreviewManualPath(Paths.get(workingDir).resolve("preview").resolve(basename+"_manual."+wxh+"."+extension));
				logger.debug("Width/Height = {}/{}", image.getWidth(), image.getHeight());
			} catch (IOException | InterruptedException e) {
				logger.error("Unable to get image dimension", e);
			}
		} else {
			logger.debug("Init: Check Width/Height of '{}' already available", copy);
		}

		return image;
	}

	private MIFImage createImagePreview(MIFImage image, String workingDir) throws MIFException {
		var filename = image.getFile().getName();

		var original = workingDir + "orig/" + filename;

		var origHeight = 3888;
		var aspectHeight = origHeight / 16;
		var estimatedWith = (int) (aspectHeight * 1.78);

		// Create preview: convert -thumbnail 200 abc.png thumb.abc.png
		logger.info("Init: Create Preview-Image {}", image);
		var command = "convert -geometry " + image.getPreviewWidth() + "x " + original + " " + image.getImagePreviewPath();
		try {
			var process = new ProcessBuilder("bash", "-c", command)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start();
			process.waitFor();

			try (var reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					logger.error(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			image.setImagePreview(ImageIO.read(image.getImagePreviewPath().toFile()));
		} catch (IOException | InterruptedException e) {
			throw new MIFException(e);
		}
		// Preview: 324x243 (Aspect ration) from 5184/16 x 3888/16

		if (!Files.exists(image.getPreviewCropPath())) {
			logger.info("Init: Create CROP-Preview-Image {}", image.getPreviewCropPath());

		    // E.g. "convert input.jpg -geometry 1920x -crop 1920x1080+0+180 -quality 100 output.jpg"
			command = new StringBuilder()
				.append("convert ")
				.append(image.getImagePreviewPath())
				.append(" -quality 100 -geometry ")
				.append(estimatedWith)
				.append("x -crop ")
				.append(estimatedWith)
				.append("x")
				.append(aspectHeight)
				.append("+0+46 ")
				.append(image.getPreviewCropPath())
				.toString();
			try {
				new ProcessBuilder("bash", "-c", command)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();

				image.setPreviewCrop(ImageIO.read(image.getPreviewCropPath().toFile()));
			} catch (InterruptedException | IOException e) {
				throw new MIFException(e);
			}

		} else {
			logger.debug("Init: CROP-Preview-Image {} already exists", image.getPreviewCropPath());
		}

		// TODO manual preview if exists
		return image;
	}
	
	public MIFAudio createAudio(String path) throws MIFException {
		var audioFile = new MIFAudio();
		audioFile.setAudiofile(path);
		checkLengthInSeconds(audioFile);
		audioFile.setEncodeStart(0);
		audioFile.setEncodeEnde(audioFile.getLengthOfInput());
		return audioFile;
	}
	
	private void checkLengthInSeconds(MIFAudio audio) {
		Process process;
		try {
			var command = "ffprobe -v error -select_streams a:0 -show_entries stream=duration,bit_rate -of default=noprint_wrappers=1:nokey=1 "+audio.getAudiofile();

			process = new ProcessBuilder("bash", "-c", command)
					.redirectErrorStream(true)
					.start();
			process.waitFor();

			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
				var durationOutput = reader.readLine();
				var bitrateOutput = reader.readLine();

				var f = Float.parseFloat(durationOutput);
				f = f * 1000;
				
				audio.setLengthOfInput((int)f);
				audio.setBitrate(Integer.parseInt(bitrateOutput));
			}
		} catch (IOException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		} catch (InterruptedException e) {
			logger.error("Unable to check duration/bitrate of audio file", e);
		}
	}

	private void copy(MIFFile file, String workingDir) throws MIFException {
		try {
			FileUtils.copyFileToDirectory(file.getFile(), new File(workingDir+"orig/"));
		} catch (IOException e1) {
			throw new MIFException(e1);
		}
	}
	
	public List<String> getProfiles() throws MIFException {
		try {
			List<String> profiles = new ArrayList<>();
			var process = new ProcessBuilder("bash", "-c", "melt -query \"profiles\"")
				.start();
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
			var process = new ProcessBuilder("bash", "-c", "melt -query \"filters\"")
				.start();
			process.waitFor();
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
			var process = new ProcessBuilder("bash", "-c", "melt -query \"filter\"="+filter)
				.start();
			process.waitFor();
			try (var reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
			var filterNames = getFilters();
			logger.info("Filter {} loaded", filterNames.size());
			
			for (String filter: filterNames) {
				var details = getFilterDetails(filter);
				
				// E.g. oldfilm has 8 parameter...
				var meltFilter = new MeltFilterDetails(filter);
				
				var parametersStarted = false;
				var valuesStarted = false;
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
							
							var key = d.substring(0, d.indexOf(':')).trim();
							var description = d.substring(d.indexOf(':')+1).trim();
	
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
				var m = (Melt) unmarshaller.unmarshal(new File("meltfilterdetails.xml"));
			
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
	
	// TODO Rename to something like previewFilterWithMelt...
	
	public void applyFilter(MIFProject pr, MIFFile mifFile, MeltFilter meltFilter) throws MIFException {
		if (mifFile instanceof MIFImage) {
			var mifImage = MIFImage.class.cast(mifFile);
			
			var mifProjectExecutor = new MIFProjectExecutor(pr);
	
			var output = "/tmp/temp.jpg";
			
			try {
				mifProjectExecutor.createFinalImageConversion(mifImage, output);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			var sb  = new StringBuilder();
			sb.append("melt ").append(output).append(" out=50 ");
			for (MeltFilter currentlyAddedFilters : mifImage.getFilters()) {
				sb.append(" -attach-cut ");
				sb.append(currentlyAddedFilters.getFiltername());
				var filterUsage = currentlyAddedFilters.getFilterUsage();
				for (String v : filterUsage.keySet()) {
					sb.append(v).append("=").append(filterUsage.get(v)).append(" ");
				}				
			}
			sb.append(" -attach-cut ");
			sb.append(meltFilter.getFiltername())
			.append(" ");
			var filterUsage = meltFilter.getFilterUsage();
			for (String v : filterUsage.keySet()) {
				sb.append(v).append("=").append(filterUsage.get(v)).append(" ");
			}
			sb.append(" -consumer sdl2 terminate_on_pause=1");
			try {
				mifProjectExecutor.execute(sb.toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
	
	public MIFTextFile createText() {
		return new MIFTextFile();
	}
}

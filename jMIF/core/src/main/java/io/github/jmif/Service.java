package io.github.jmif;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.builder.MIFProjectExecutor;
import io.github.jmif.entities.MIFAudioFile;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFProject;
import io.github.jmif.entities.MIFVideo;

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

	public void updateFramerate(MIFProject project) throws MIFException {
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

	public void createWorkingDirs(MIFProject project) throws MIFException {
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

	public MIFVideo initVideo(String file, String display, float frames, String dim, int overlay, String workingDir, int profileFramelength) throws MIFException {
		var video = new MIFVideo(file, display, frames, dim, overlay);
		updateFile(video);
		copy(video, workingDir);
		var path = video.getFile().substring(0, video.getFile().lastIndexOf('/'));
		var filename = video.getFilename();

		if (video.getFramelength() == -1) {
			String command = "ffprobe -v quiet -of csv=p=0 -show_entries format=duration " + video.getFile();
			logger.info("Init: Extract Framelength of {}", filename);
			Process process;
			try {
				process = new ProcessBuilder("bash", "-c", command)
						.directory(new File(path))
						.redirectErrorStream(true)
						.start();
				String output = null;

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
					String line;
					while ((line = reader.readLine()) != null) {
						output = line;
					}
				}

				int index = output.indexOf('.');
				output = output.substring(0, index + 2);
				video.setFramelength(Float.parseFloat(output) * profileFramelength);
				logger.info("Init: Extract Framelength of "+filename+"=> "+video.getFramelength());
			} catch (IOException e) {
				logger.error("Unable to get length of video", e);
			}
		} else {
			logger.debug("Init: Framelength already known");
		}

		// set images...
		var previewImages = new String[10];
		for (int i = 1; i <= 10; i++) {
			previewImages[i - 1] = workingDir+"/preview/"+"_low_"+filename+"_"+i+".png";
		}
		video.setPreviewImages(previewImages);

		// TODO parse ffprobe -v error -show_format -show_streams
		// TODO ffprobe -v 0 -of csv=p=0 -select_streams v:0 -show_entries stream=r_frame_rate 2.MP4
		video.setFps(-1); 
		video.setAudioBitrate(-1);
		video.setAudioCodec("not yet extracted");
		video.setVideoCodec("not yet extracted");
		return video;
	}

	public void createPreview(MIFFile file, String workingDir) throws MIFException {
		if (file instanceof MIFImage) {
			createPreview(MIFImage.class.cast(file), workingDir);
		} else if (file instanceof MIFVideo) {
			createPreview(MIFVideo.class.cast(file), workingDir);
		}
	}

	private void createPreview(MIFVideo videoO, String workingDir) throws MIFException {
		var filename = videoO.getFile().substring(videoO.getFile().lastIndexOf('/') + 1);
		var video = workingDir+"/orig/"+filename;

		if (videoO.getWidth() == -1 || videoO.getHeight() == -1) {
			logger.info("Init: Check Width/Height");
			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 " + video;
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
			videoO.setWidth(Integer.valueOf(output.substring(0, output.indexOf('x'))));
			videoO.setHeight(Integer.valueOf(output.substring(output.indexOf('x') + 1)));
		} else {
			logger.debug("Init: Width/Height already known");
		}

		/*
		 * Preview-Video in low quality 
		 */

		var videoLowQuality = workingDir+"/preview/_low_"+filename;
		if (!new File(videoLowQuality).exists()) {
			logger.info("Init: Create Preview-Video (low quality) {}", videoLowQuality);
			var command = "ffmpeg -y -i " + video + " -vf scale=320:-1 -c:v libx264 -crf 0 -an -r 25 -preset ultrafast "+videoLowQuality;
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
		var previewImages = videoO.getPreviewImages();
		var cnt = (int) (videoO.getFramelength() / 10);
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
				previewImages[i - 1] = image;
			} else {
				logger.debug("Init: Preview-Video-Image {} already computed", image);
			}
		}
		videoO.setPreviewImages(previewImages);
	}

	public void createManualPreview(MIFImage image) throws MIFException {
		image.setStyle("MANUAL");
		Process process;
		try {
			String temp = image.getManualStyleCommand();
			temp = temp.replace("geometry 1920", "geometry "+image.getPreviewWidth()+"x");

			logger.info("Execute {}", temp);

			process = new ProcessBuilder("bash", "-c", "convert "+image.getFile()+" "+temp+" "+image.getPreviewManual())
					.redirectErrorStream(true)
					.start();
			process.waitFor();
		} catch (Exception e) {
			logger.error("Unable to create manual style image ",e);
		}
	}

	public MIFImage initImage(String file, String display, float frames, String dim, int overlay, String workingDir, int framelength) throws MIFException {
		var image = new MIFImage(file, display, frames, dim, overlay);
		updateFile(image);
		copy(image, workingDir);
		if (framelength == -1) {
			framelength = 5*framelength; // constant, default: 5 seconds...
		}

		var path = image.getFile().substring(0, image.getFile().lastIndexOf('/'));
		var filename = image.getFilename();

		var copy = workingDir + "orig/" + filename;

		if (image.getWidth() == -1 || image.getHeight() == -1) {
			logger.debug("Init: Check Width/Height of '{}'", filename);

			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 "	+ copy;
			Process process;
			try {
				process = new ProcessBuilder("bash", "-c", command).directory(new File(path)).redirectErrorStream(true)
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
		var fileWOending = filename.substring(0, filename.lastIndexOf("."));
		var fileending = filename.substring(filename.lastIndexOf(".") + 1);
		image.setImagePreview(workingDir + "preview/" + fileWOending + "_thumb." + wxh + "."
				+ fileending);
		image.setPreviewHardResize(workingDir + "preview/" + fileWOending + "_hard." + wxh + "."
				+ fileending);
		image.setPreviewFillWColor(workingDir + "preview/" + fileWOending + "_fill." + wxh + "."
				+ fileending);
		image.setPreviewCrop(workingDir + "preview/" + fileWOending + "_kill." + wxh + "." + fileending);
		image.setPreviewManual(workingDir + "preview/" + fileWOending + "_manual." + wxh + "." + fileending);
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

	public void checkLengthInSeconds(MIFAudioFile audio) throws MIFException {
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

	private void updateFile(MIFFile file) throws MIFException {
		file.setFileExists(new File(file.getFile()).exists());
		file.setFilename(file.getFile().substring(file.getFile().lastIndexOf('/')+1));
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
}

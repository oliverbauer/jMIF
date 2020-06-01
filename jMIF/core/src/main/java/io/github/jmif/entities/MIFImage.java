package io.github.jmif.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "picture")
public class MIFImage extends MIFFile {
	private static final Logger logger = LoggerFactory.getLogger(MIFImage.class);

	// CROP, HARD, FILL, MANUAL
	private String style = "CROP";
	private String manualStyleCommand = null;
	
	@XmlTransient
	private String imagePreview;
	
	@XmlTransient
	private String previewHardResize;
	
	@XmlTransient
	private String previewFillWColor;
	
	@XmlTransient
	private String previewCrop;
	
	@XmlTransient
	private String previewManual;

	@XmlTransient
	private int previewHeight;

	@XmlTransient
	private int previewWidth;

	public MIFImage() {

	}

	public MIFImage(String file, String display, float frames, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setFramelength(frames);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public String getManualStyleCommand() {
		return manualStyleCommand;
	}

	public void setManualStyleCommand(String manualStyleCommand) {
		this.manualStyleCommand = manualStyleCommand;
		this.style = "MANUAL";
		Process process;
		try {
			String temp = manualStyleCommand;
			temp = temp.replace("geometry 1920", "geometry "+previewWidth+"x");

			logger.info("Execute {}", temp);
			
			process = new ProcessBuilder("bash", "-c", "convert "+getFile()+" "+temp+" "+previewManual)
				.redirectErrorStream(true)
				.start();
			process.waitFor();
		} catch (Exception e) {
			logger.error("Unable to create manual style image ",e);
		}
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getPreviewHardResize() {
		return previewHardResize;
	}

	public String getPreviewFillWColor() {
		return previewFillWColor;
	}

	public String getPreviewCrop() {
		return previewCrop;
	}

	public String getImagePreview() {
		return imagePreview;
	}

	public String getPreviewManual() {
		return previewManual;
	}

	public void setPreviewManual(String previewManual) {
		this.previewManual = previewManual;
	}

	@Override
	public void init(String workingDir, int framelength) {
		if (framelength == -1) {
			framelength = 5*framelength; // constant, default: 5 seconds...
		}

		var path = getFile().substring(0, getFile().lastIndexOf('/'));
		var filename = getFilename();

		var copy = workingDir + "orig/" + filename;

		if (width == -1 || height == -1) {
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
				setWidth(Integer.valueOf(output.substring(0, output.indexOf('x'))));
				setHeight(Integer.valueOf(output.substring(output.indexOf('x') + 1)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.debug("Init: Check Width/Height of '{}' already available", copy);
		}

		previewHeight = getHeight() / 16;
		previewWidth = (int) (previewHeight * 1.78);

		var origHeight = 3888;
		var aspectHeight = origHeight / 16;
		var estimatedWith = (int) (aspectHeight * 1.78);

		var wxh = estimatedWith + "x" + aspectHeight;
		var fileWOending = filename.substring(0, filename.lastIndexOf("."));
		var fileending = filename.substring(filename.lastIndexOf(".") + 1);
		imagePreview = workingDir + "preview/" + fileWOending + "_thumb." + wxh + "."
				+ fileending;
		previewHardResize = workingDir + "preview/" + fileWOending + "_hard." + wxh + "."
				+ fileending;
		previewFillWColor = workingDir + "preview/" + fileWOending + "_fill." + wxh + "."
				+ fileending;
		previewCrop = workingDir + "preview/" + fileWOending + "_kill." + wxh + "." + fileending;
		previewManual = workingDir + "preview/" + fileWOending + "_manual." + wxh + "." + fileending;
	}

	@Override
	public Runnable getBackgroundRunnable(String workingDir) {
		return  () -> {
			try {
				doThreadStuff(workingDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	private void doThreadStuff(String workingDir) throws InterruptedException, IOException {
		var filename = getFilename();

		var original = workingDir + "orig/" + filename;

		if (!new File(original).exists()) {
			new ProcessBuilder("bash", "-c", "cp "+getFile()+" "+original)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();
		}
		
		var origHeight = 3888;
		var aspectHeight = origHeight / 16;
		var estimatedWith = (int) (aspectHeight * 1.78);

		if (!new File(imagePreview).exists()) {
			// Create preview: convert -thumbnail 200 abc.png thumb.abc.png
			logger.info("Init: Create Preview-Image {}", imagePreview);
			var command = "convert -geometry " + previewWidth + "x " + original + " " + imagePreview;
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
			
			
			// Preview: 324x243 (Aspect ration) from 5184/16 x 3888/16
		} else {
			logger.debug("Init: Preview-Image {} already exists", imagePreview);
		}

		if (!new File(previewHardResize).exists()) {
			logger.info("Init: Create HARD-Preview-Image {}", previewHardResize);

			var hardRescale = "convert " + imagePreview + " -quality 100 -resize " + estimatedWith + "x"
					+ aspectHeight + "! " + previewHardResize;
			var process = new ProcessBuilder("bash", "-c", hardRescale).directory(new File(workingDir))
					.redirectErrorStream(true).start();
			process.waitFor();
		} else {
			logger.debug("Init: HARD-Preview-Image {} already exists", previewHardResize);
		}

		if (!new File(previewFillWColor).exists()) {
			logger.info("Init: Create FILL-Preview-Image {}", previewFillWColor);

			var fill = "convert " + imagePreview + " -quality 100 -geometry x" + aspectHeight + " fill." + filename;
			var process = new ProcessBuilder("bash", "-c", fill).directory(new File(workingDir))
					.redirectErrorStream(true).start();
			process.waitFor();

			var fill2 = "convert fill." + filename + " \\( -clone 0 -quality 100 -blur 0x5 -resize " + estimatedWith
					+ "x" + aspectHeight + "\\! -fill black -quality 100 -colorize 100% \\) \\( -clone 0 -resize "
					+ estimatedWith + "x" + aspectHeight + " \\) -delete 0 -gravity center -composite "
					+ previewFillWColor;
			process = new ProcessBuilder("bash", "-c", fill2).directory(new File(workingDir)).redirectErrorStream(true)
					.start();
			process.waitFor();

			// rm tempfile...
			process = new ProcessBuilder("bash", "-c", "rm fill." + filename).directory(new File(workingDir))
					.redirectErrorStream(true).start();
			process.waitFor();
		} else {
			logger.debug("Init: FILL-Preview-Image {} already exists", previewFillWColor);
		}

		if (!new File(previewCrop).exists()) {
			logger.info("Init: Create CROP-Preview-Image {}", previewCrop);

			var kill = "convert " + imagePreview + " -quality 100 -geometry " + estimatedWith + "x kill." + filename;
			new ProcessBuilder("bash", "-c", kill)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();

			var kill2 = "convert kill." + filename + " -quality 100 -crop " + estimatedWith + "x" + aspectHeight
					+ "+0+46 " + previewCrop;
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

		} else {
			logger.debug("Init: CROP-Preview-Image {} already exists", previewCrop);
		}
		
		// TODO manual preview if exists
	}
}

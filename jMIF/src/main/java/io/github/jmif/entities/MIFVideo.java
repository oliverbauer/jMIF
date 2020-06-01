package io.github.jmif.entities;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XmlRootElement(name = "video")
public class MIFVideo extends MIFFile {
	private static final Logger logger = LoggerFactory.getLogger(MIFVideo.class);
	@XmlTransient
	private String previewImages[];

	public MIFVideo() {

	}

	public MIFVideo(String file, String display, float frames, String dim, int overlay) {
		setFile(file);
		setDisplayName(display);
		setFramelength(frames);
		setWidth(Integer.parseInt(dim.substring(0, dim.indexOf('x'))));
		setHeight(Integer.parseInt(dim.substring(dim.indexOf('x') + 1)));
		setOverlayToPrevious(overlay);
	}

	public String[] getPreviewImages() {
		return this.previewImages;
	}

	@Override
	public void init(String workingDir, int profileFramelength) {
		var path = getFile().substring(0, getFile().lastIndexOf("/"));
		var filename = getFilename();
		
		if (framelength == -1) {
			String command = "ffprobe -v quiet -of csv=p=0 -show_entries format=duration " + getFile();
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
				
				int index = output.indexOf(".");
				output = output.substring(0, index + 2);
				setFramelength(Float.parseFloat(output) * profileFramelength);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("Init: Extract Framelength of "+filename+"=> "+framelength);
		} else {
			logger.debug("Init: Framelength already known");
		}

		
		// set images...
		previewImages = new String[10];
		for (int i = 1; i <= 10; i++) {
			previewImages[i - 1] = workingDir+"/preview/"+"_low_"+filename+"_"+i+".png";
		}
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
	
	private void doThreadStuff(String workingDir) throws IOException, InterruptedException {
		var filename = getFile().substring(getFile().lastIndexOf('/') + 1);
		var video = workingDir+"/orig/"+filename;
		
		if (this.width == -1 || this.height == -1) {
			logger.info("Init: Check Width/Height");
			var command = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of csv=s=x:p=0 " + video;
			Process process = new ProcessBuilder("bash", "-c", command)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
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
			new ProcessBuilder("bash", "-c", command)
				.directory(new File(workingDir))
				.redirectErrorStream(true)
				.start()
				.waitFor();
		} else {
			logger.debug("Init: Preview-Video already computed");
		}

		/*
		 * 10 images from the video 
		 */
		
		var cnt = (int) (framelength / 10);
		for (int i = 1; i <= 10; i++) {
			var image = workingDir+"/preview/_low_"+filename+"_"+i+".png";
			if (!new File(image).exists()) {
				logger.info("Init: Create Preview-Video-Image {}", image);
				
				var command = "ffmpeg -y -i " + videoLowQuality + " -vf \"select=eq(n\\," + (i * cnt) + ")\" -vframes 1 "+ videoLowQuality + "_" + i + ".png";
				new ProcessBuilder("bash", "-c", command)
					.directory(new File(workingDir))
					.redirectErrorStream(true)
					.start()
					.waitFor();
				previewImages[i - 1] = image;
			} else {
				logger.debug("Init: Preview-Video-Image {} already computed", image);
			}
		}
	}
}

package io.github.jmif.gui.swing.splash;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Splashscreen {
	private static final Logger logger = LoggerFactory.getLogger(Splashscreen.class);
	
	private final int splashSleepTime = 10;
	
	private JDialog dialog;
    private JProgressBar progress;
    private JLabel lblLinux = new JLabel("Linux?");
    private JLabel lblJava = new JLabel("Java?");
    private JLabel lblMLT = new JLabel("MLT?");
    private JLabel lblFFmpeg = new JLabel("FFmpeg?");
    private JLabel lblImageMagick = new JLabel("ImageMagick?");
    
    private JTextField fldlLinux = new JTextField();
    private JTextField fldJava = new JTextField();
    private JTextField fldMLT = new JTextField();
    private JTextField fldFFmpeg = new JTextField();
    private JTextField fldImageMagick = new JTextField();
    
    private JButton button = new JButton("Please wait... checking environment");
    private boolean buttonClicked = false;
    
    private SwingWorker<Void, Integer> worker;
    
    private final Font font = new Font("SansSerif", Font.BOLD, 20);
    // Requirement fulfilled
    private final Color myGreen = new Color(0,180,0);
    // Requirement fulfilled, but a newer software version exists (only checked for melt)
    private final Color myOrange = new Color(255, 165, 0);
    
    private boolean hasMissingRequirements = false;
    
    public Splashscreen() {
    	initDialog();
    	initWorker();
    }
    
    public SwingWorker<Void, Integer> getWorker() {
    	return worker;
    }
    
    protected void initWorker() {
        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                checkLinux();
                IntStream.range(0, 19).forEach(i -> { sleep(); publish(i); }); 
                
                checkJava();                
                IntStream.range(20, 39).forEach(i -> { sleep(); publish(i); });
                
                checkMLT();
                IntStream.range(40, 59).forEach(i -> { sleep(); publish(i); });

                checkFFmpeg();
                IntStream.range(60, 79).forEach(i -> { sleep(); publish(i); });
                
                checkImageMagick();
                IntStream.range(80, 100).forEach(i -> { sleep(); publish(i); });
                
                
                if (hasMissingRequirements) {
                	// TODO Splash: Provide Hint about installing? sudo apt-get install....
                	button.setText("Exit");
                } else {
                	// TODO Splash: Configuration: Show Splash-Screen on Startup?
                	button.setText("Start");
                }
                button.setEnabled(true);
                progress.setEnabled(false);
                
                while (!buttonClicked) {
                	Thread.sleep(100);
                }
                
                return null;
            }

            @Override
            protected void process(List<Integer> chunks) {
                progress.setValue(chunks.get(chunks.size() - 1) + 1);
            }
            
        	private void sleep() {
        		try {
					Thread.sleep(splashSleepTime);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
        	}
        };
        worker.execute();
    }

    private void checkLinux() throws IOException {
         if (SystemUtils.IS_OS_LINUX) {
         	String description = null;
         	String name = null;
         	String version = null;
         	
         	for (String line : getProcessOutput("cat /etc/*release")) {
         		if (line.startsWith("DISTRIB_DESCRIPTION=")) {
         			description = line.substring(line.indexOf('=')+1);
         		} else if (line.startsWith("NAME=")) {
         			name = line.substring(line.indexOf('=')+1);
         		} else if (line.startsWith("VERSION=")) {
         			version = line.substring(line.indexOf('=')+1);
         		}                		
         	}
 			
 			if (name != null && version != null) {
 				fldlLinux.setText(name+" "+version);
 			} else if (description != null) {
 				fldlLinux.setText(description);
 			} else {
 				fldlLinux.setText(SystemUtils.OS_NAME);
 				logger.warn("Unable to determine distribution");
 			}
 			fldlLinux.setBackground(myGreen);
 			
         } else {
         	logger.error("Operation system is not Linux");
         	fldlLinux.setText(SystemUtils.OS_NAME+" - Sorry, not supported (yet?)");
         	fldlLinux.setBackground(Color.RED);
         	hasMissingRequirements = true;
         }
    }
    
    private void checkJava() {
        var javaVersion = SystemUtils.JAVA_VERSION;
        fldJava.setText(javaVersion);
        fldJava.setBackground(myGreen);
        
        // TODO Config: JDK Version-Check: Mark if someone is not using latest version...
        // 1.8.0_221
        // JDK 8: 8u251 (checked 2020.05.24)
        // 11.0.7 
        // JDF 14: 14.0.1
    }
    
    private void checkImageMagick() throws IOException {
        String imageMagickVersion = null;
    	for (String line : getProcessOutput("convert -version")) {
    		if (line.startsWith("Version: ")) {
    			imageMagickVersion = line.substring("Version: ImageMagick ".length(), line.indexOf(' ', "Version: ImageMagick ".length()));
    		}
    	}
        if (imageMagickVersion == null) {
        	fldImageMagick.setText("Not found. Check 'convert -version' on CLI");
        	fldImageMagick.setBackground(Color.RED);
        	hasMissingRequirements = true;
        } else {
        	// Newest version 7.0.10-14 (checked 2020.05.24)
        	var newestVersion = true;
        	
        	var prefix = imageMagickVersion.contains("-") ? imageMagickVersion.substring(0, imageMagickVersion.indexOf('-')) : imageMagickVersion;
        	var version = prefix.split("\\.");
        	var v = version[0]+version[1]+version[2];
        	if (Integer.parseInt(v) < 7010) {
        		newestVersion = false;
        	}
        	
        	if (newestVersion) {
        		fldImageMagick.setText(imageMagickVersion);
        		fldImageMagick.setBackground(myGreen);
        	} else {
        		fldImageMagick.setText(imageMagickVersion+" (OK, consider upgrading to 7.0.10-14 - or newer if exists)");
        		fldImageMagick.setBackground(myOrange);
        	}
        }
    }
    
    private void checkFFmpeg() throws IOException {
        String ffmpegVersion = null;
    	for (String line : getProcessOutput("ffmpeg -version")) {
    		if (line.startsWith("ffmpeg version ")) {
    			ffmpegVersion = line.substring("ffmpeg version ".length(), line.indexOf("Copyright"));
    		}
    	}                
        if (ffmpegVersion != null) {
        	// Newest version 4.2.3  (checked 2020.05.24)
        	var newestVersion = true;
        	
        	var prefix = ffmpegVersion.contains("-") ? ffmpegVersion.substring(0, ffmpegVersion.indexOf('-')) : ffmpegVersion;
        	var version = prefix.split("\\.");
        	var v = version[0]+version[1]+version[2];
        	if (Integer.parseInt(v) < 423) {
        		newestVersion = false;
        	}
        	
        	if (newestVersion) {
        		fldFFmpeg.setText(ffmpegVersion);
            	fldFFmpeg.setBackground(myGreen);
        	} else {
        		fldFFmpeg.setText(ffmpegVersion+" (OK, consider upgrading to 4.2.3 - or newer if exists)");
            	fldFFmpeg.setBackground(myOrange);
        	}
        } else {
        	fldFFmpeg.setText("Not found. Check 'ffmpeg -version' on CLI");
        	fldFFmpeg.setBackground(Color.RED);
        	hasMissingRequirements = true;
        }
    }
    
    private void checkMLT() throws IOException {
    	String mltVersion = null;
    	for (String line : getProcessOutput("melt -version")) {
    		if (line.startsWith("melt ")) {
    			mltVersion = line.substring(5);
    		}
    	}
        if (mltVersion != null) {
        	// Newest version 6.20.0  (checked 2020.05.24)
        	var newestVersion = true;
        	var version = mltVersion.split("\\.");
        	var v = version[0]+version[1]+version[2];
        	if (Integer.parseInt(v) < 6200) {
        		newestVersion = false;
        	}
        	
        	if (newestVersion) {
            	fldMLT.setText(mltVersion);
        		fldMLT.setBackground(myGreen);
        	} else {
            	fldMLT.setText(mltVersion+" (OK, consider upgrading to 6.20.0 - or newer if exists)");
        		fldMLT.setBackground(myOrange);
        	}
        } else {
        	fldMLT.setText("Not found. Check 'melt -version' on CLI");
        	fldMLT.setBackground(Color.RED);
        	hasMissingRequirements = true;
        }
    }
    
    private List<String> getProcessOutput(String command) throws IOException {
    	List<String> output = new ArrayList<>();
		
    	var process = new ProcessBuilder("bash", "-c", command)
			.redirectErrorStream(true)
			.start();
    	
		try (var reader = new BufferedReader(
			    new InputStreamReader(process.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				output.add(line);
			}
		} catch (IOException e) {
			logger.error("Unable to get process output", e);
		}
		return output;
    }
    
    protected void closeSplashScreenOrExit() {
        dialog.setVisible(false);
        dialog.dispose();
        
        if (hasMissingRequirements) {
        	System.exit(-1);
        }
    }
    
    protected void initDialog() {
        dialog = new JDialog((Frame) null);
        dialog.setModal(false);
        dialog.setUndecorated(true);
        
        var dim = new Dimension(700, 20);
        
        fldlLinux.setPreferredSize(dim);
        fldlLinux.setEditable(false);
        fldlLinux.setFont(font);
        
        fldJava.setPreferredSize(dim);
        fldJava.setEditable(false);
        fldJava.setFont(font);
        
        fldMLT.setPreferredSize(dim);
        fldMLT.setEditable(false);
        fldMLT.setFont(font);
        
        fldFFmpeg.setPreferredSize(dim);
        fldFFmpeg.setEditable(false);
        fldFFmpeg.setFont(font);
        
        fldImageMagick.setPreferredSize(dim);
        fldImageMagick.setEditable(false);
        fldImageMagick.setFont(font);
        
        var infoPanel = new JPanel(new GridBagLayout());
        var gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        var splashLabel = new JLabel("jMIF");
        splashLabel.setFont(new Font("SansSerif", Font.BOLD, 40));
        infoPanel.add(splashLabel, gbc);
               
        gbc.insets = new Insets(10,10,0,10); // Top, Left, Right
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        infoPanel.add(lblLinux, gbc);
        gbc.gridy = 2;
        infoPanel.add(lblJava, gbc);
        gbc.gridy = 3;
        infoPanel.add(lblMLT, gbc);
        gbc.gridy = 4;
        infoPanel.add(lblFFmpeg, gbc);
        gbc.gridy = 5;
        infoPanel.add(lblImageMagick, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        infoPanel.add(fldlLinux, gbc);
        gbc.gridy = 2;
        infoPanel.add(fldJava, gbc);
        gbc.gridy = 3;
        infoPanel.add(fldMLT, gbc);
        gbc.gridy = 4;
        infoPanel.add(fldFFmpeg, gbc);
        gbc.gridy = 5;
        infoPanel.add(fldImageMagick, gbc);
        
        progress = new JProgressBar();
        progress.setOpaque(true);
        progress.setStringPainted(true);
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(10,0,10,0);
        infoPanel.add(progress, gbc);
        
        button.setEnabled(false);
        button.addActionListener(l -> {
        	closeSplashScreenOrExit();
        	buttonClicked = true;
        });
        
        gbc.gridy = 7;
        infoPanel.add(button, gbc);
        
        dialog.add(infoPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }
}

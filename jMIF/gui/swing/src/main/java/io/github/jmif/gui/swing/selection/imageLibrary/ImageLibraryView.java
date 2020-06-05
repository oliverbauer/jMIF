package io.github.jmif.gui.swing.selection.imageLibrary;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.imgscalr.AsyncScalr;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.MIFException;
import io.github.jmif.Service;
import io.github.jmif.config.Configuration;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.selection.image.ImageView;

public class ImageLibraryView {
	private static final Logger logger = LoggerFactory.getLogger(ImageLibraryView.class);
	
	private Box box;
	private JScrollPane scrollPane;
	private GraphWrapper mifProject;
	
	private Box newBox;
	
	public ImageLibraryView(GraphWrapper mifProject) {
		this.mifProject = mifProject;
		box = Box.createVerticalBox();
		
		JButton chooseDirectory = new JButton("Select directory");
		chooseDirectory.addActionListener(a -> {
			JFileChooser c = new JFileChooser();
			c.setMultiSelectionEnabled(false);
			c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int returnVal = c.showOpenDialog(new JFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File directory = c.getSelectedFile();
				
				List<File> images = new ArrayList<>();
				for (File file : directory.listFiles()) {
					if (!file.isDirectory()) {
						String type = file.getName().substring(file.getName().lastIndexOf('.') + 1);
						if (Configuration.allowedImageTypes.contains(type)) {
							images.add(file);
						}
					}
				}
				
				try {
					createImageList(images);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		newBox = Box.createVerticalBox();
		scrollPane = new JScrollPane(newBox);
		
		int w = 5500;
		int h = 650;
		scrollPane.setMinimumSize(new Dimension(w, h));
		scrollPane.setPreferredSize(new Dimension(w, h));
		scrollPane.setMaximumSize(new Dimension(w, h));
		
		
		box.add(chooseDirectory);
		box.add(scrollPane);
	}
	
	private void createImageList(List<File> images) throws IOException, InterruptedException, ExecutionException {
		logger.info("Create Library for {} images", images.size());
		
		newBox.removeAll();
		
		int count = 0;
		Box subBox = Box.createHorizontalBox();
		for (final File f : images) {
			count++;
			
			JLabel imageLabel = new JLabel();
			
			SwingWorker<Void,Integer> worker = new SwingWorker<>() {
	            @Override
	            protected Void doInBackground() throws Exception {
	            	BufferedImage img = ImageIO.read(f);
	            	Future<BufferedImage> scaledImg = AsyncScalr.resize(img, Mode.AUTOMATIC, 300, 300);
	            	imageLabel.setIcon(new ImageIcon(scaledImg.get()));
	            	return null;
	            }
			};
			worker.execute();
			
			
			imageLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						var file1 = mifProject.createMIFFile(f);
						
						var executor = Executors.newWorkStealingPool();
						executor.submit(() -> {
							try {
								new Service().createPreview(file1, mifProject.getPr().getWorkingDir());
							} catch (Exception ex) {
								logger.error("", ex);
							}
						});
						
					} catch (MIFException | InterruptedException | IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			
			imageLabel.setMinimumSize(new Dimension(300,300));
			subBox.add(imageLabel);
			subBox.add(Box.createHorizontalStrut(20));
			
			if (count % 5 == 0) {
				newBox.add(subBox);
				newBox.add(Box.createVerticalStrut(10));
				subBox = Box.createHorizontalBox();
			}
		}
		if (count % 5 != 0) {
			newBox.add(subBox);
			newBox.add(Box.createVerticalStrut(10));
		}
		
		newBox.repaint();
		newBox.updateUI();
		box.repaint();
		box.updateUI();
		logger.info("done");
	}
	
	public Box getBox() {
		return box;
	}
}

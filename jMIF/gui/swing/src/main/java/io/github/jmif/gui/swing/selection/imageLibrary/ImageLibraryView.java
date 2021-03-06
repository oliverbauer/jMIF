package io.github.jmif.gui.swing.selection.imageLibrary;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;

import org.imgscalr.AsyncScalr;
import org.imgscalr.Scalr.Mode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.config.UserConfig;

public class ImageLibraryView {
	@Inject
	private UserConfig userConfig;
	
	private static final Logger logger = LoggerFactory.getLogger(ImageLibraryView.class);

	private Box box;
	private JScrollPane scrollPane;
	private GraphWrapper graphWrapper;

	private Box newBox;

	public void init(GraphWrapper graphWrapper) {
		this.graphWrapper = graphWrapper;
		box = Box.createVerticalBox();

		var chooseDirectory = new JButton("Select directory");
		chooseDirectory.addActionListener(a -> {
			var c = new JFileChooser();
			c.setMultiSelectionEnabled(false);
			c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			var returnVal = c.showOpenDialog(new JFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				var directory = c.getSelectedFile();

				List<File> images = new ArrayList<>();
				for (File file : directory.listFiles()) {
					if (!file.isDirectory()) {
						var type = file.getName().substring(file.getName().lastIndexOf('.') + 1);
						if (Configuration.allowedImageTypes.contains(type)) {
							images.add(file);
						}
					}
				}

				createImageList(images);
			}
		});
		var chooseDirectoryRec = new JButton("Select directory (recursivly)");
		chooseDirectoryRec.addActionListener(a -> {
			var c = new JFileChooser();
			c.setMultiSelectionEnabled(false);
			c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			var returnVal = c.showOpenDialog(new JFrame());
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				var directory = c.getSelectedFile();
				createImageList(collectImagesRecursivly(directory));
			}
		});

		newBox = Box.createVerticalBox();
		scrollPane = new JScrollPane(newBox);

		var w = 5500;
		var h = 450;
		scrollPane.setMinimumSize(new Dimension(w, h));
		scrollPane.setPreferredSize(new Dimension(w, h));
		scrollPane.setMaximumSize(new Dimension(w, h));


		box.add(chooseDirectory);
		box.add(chooseDirectoryRec);
		box.add(scrollPane);
	}

	private List<File> collectImagesRecursivly(File dir) {
		List<File> fileTree = new ArrayList<>();
		if(dir == null || dir.listFiles() == null){
			return fileTree;
		}
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				var type = file.getName().substring(file.getName().lastIndexOf('.') + 1);
				if (Configuration.allowedImageTypes.contains(type)) {
					fileTree.add(file);
				}
			} else {
				fileTree.addAll(collectImagesRecursivly(file));
			}
		}
		return fileTree;
	}

	private void createImageList(List<File> images) {
		logger.info("Create Library for {} images", images.size());

		newBox.removeAll();

		var count = 0;
		var subBox = Box.createHorizontalBox();
		for (final File f : images) {
			count++;

			var imageLabel = new JLabel();

			SwingWorker<Void,Integer> worker = new SwingWorker<>() {
				@Override
				protected Void doInBackground() throws Exception {
					var img = ImageIO.read(f);
					var scaledImg = AsyncScalr.resize(img, Mode.AUTOMATIC, 300, 300);
					imageLabel.setIcon(new ImageIcon(scaledImg.get()));
					return null;
				}
			};
			worker.execute();

			imageLabel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						MIFFile imageFile = graphWrapper.createMIFFile(f);
						imageFile.setDuration(userConfig.getIMAGE_DURATION());
						imageFile.setOverlayToPrevious(userConfig.getIMAGE_OVERLAY());
						
						var executor = Executors.newWorkStealingPool();
						executor.submit(() -> {
							try {
								graphWrapper.getService().createPreview(imageFile, graphWrapper.getPr().getWorkingDir());
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						});
						
					} catch (Exception ex) {
						logger.error("", ex);
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

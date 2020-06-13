package io.github.jmif.gui.swing.menu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jmif.config.Configuration;
import io.github.jmif.core.MIFException;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.listener.ProjectListener.type;
import io.github.jmif.gui.swing.menu.util.ButtonFactory;

public class MenuView {
	private static final Logger logger = LoggerFactory.getLogger(MenuView.class);

	private JPanel panel;
	
	public MenuView(final GraphWrapper graphWrapper) {
		panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBackground(Configuration.bgColor);
		
		Box verticalBox = Box.createVerticalBox();
		verticalBox.add(Box.createVerticalStrut(10));
		
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(Box.createHorizontalStrut(10));

		Box buttonBox = Box.createVerticalBox();
		JButton newButton = ButtonFactory.newButton("/images/svg/menuButtonQuadNew.svg", "/images/svg/menuButtonQuadNewHover.svg");
		newButton.addActionListener(e -> {
			JFileChooser c = new JFileChooser();
			int returnVal = c.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File project = c.getSelectedFile();
				 if(!project.exists()) {
					 graphWrapper.getPr().setFileOfProject(project.getAbsolutePath());
					 graphWrapper.informListeners(type.NEW_PROJECT);
				 }
			}
		});
		buttonBox.add(newButton);
		horizontalBox.add(buttonBox);
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		Box loadBox = Box.createVerticalBox();
		JButton loadButton = ButtonFactory.newButton("/images/svg/menuButtonQuadOpen.svg", "/images/svg/menuButtonQuadOpenHover.svg");
		loadButton.addActionListener(e -> {
			JFileChooser c = new JFileChooser();
			int returnVal = c.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File project = c.getSelectedFile();
				
				logger.info("Loading {}", project.getAbsolutePath());
				
				graphWrapper.getPr().setFileOfProject(project.getAbsolutePath());
				try {
					graphWrapper.load();
				} catch (MIFException | IOException | InterruptedException e1) {
					logger.error("Unable to load project", e1);
				}
			}
		});
		loadBox.add(loadButton);
		horizontalBox.add(loadBox);
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		Box saveBox = Box.createVerticalBox();
		JButton saveButton = ButtonFactory.newButton("/images/svg/menuButtonQuadSave.svg", "/images/svg/menuButtonQuadSaveHover.svg");
		saveButton.addActionListener(e -> { 
			graphWrapper.save();
		});
		saveBox.add(saveButton);
		horizontalBox.add(saveBox);

		horizontalBox.add(Box.createGlue());
		horizontalBox.add(new JLabel("jMIF - MLT, IMAGEMAGICK, FFMPEG"));
		horizontalBox.add(Box.createGlue());
		
		Box previewBox = Box.createVerticalBox();
		JButton previewButton = ButtonFactory.newButton("/images/svg/menuButtonQuadPreview.svg", "/images/svg/menuButtonQuadPreviewHover.svg");
		previewButton.addActionListener(e -> {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
			    public Void doInBackground() {
		        	try {
		        		graphWrapper.getService().convert(graphWrapper.getPr(), true);
		        	} catch (Exception e1) {
		        		logger.error("Unable to create preview video", e1);
		        	}
			    	
					return null;
			    }
			};
			worker.execute();
		});
		previewBox.add(previewButton);
		horizontalBox.add(previewBox);
		horizontalBox.add(Box.createHorizontalStrut(5));

		Box renderBox = Box.createVerticalBox();
		JButton renderButton = ButtonFactory.newButton("/images/svg/menuButtonQuadRender.svg", "/images/svg/menuButtonQuadRenderHover.svg");
		renderButton.addActionListener(e -> {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
			    public Void doInBackground() {
		        	try {
		        		graphWrapper.getService().convert(graphWrapper.getPr(), false);
		        	} catch (Exception e1) {
		        		logger.error("Unable to render project", e1);
		        	}
			    	
					return null;
			    }
			};
			worker.execute();
		});
		renderBox.add(renderButton);
		horizontalBox.add(renderBox);
		horizontalBox.add(Box.createHorizontalStrut(5));
		
		Box exitBox = Box.createVerticalBox();
		JButton exitButton = ButtonFactory.newButton("/images/svg/menuButtonQuadExit.svg", "/images/svg/menuButtonQuadExitHover.svg"); 
		exitButton.addActionListener(e -> System.exit(-1));
		exitBox.add(exitButton);
		horizontalBox.add(exitBox);
		horizontalBox.add(Box.createHorizontalStrut(10));
		
		verticalBox.add(horizontalBox);
		verticalBox.add(Box.createVerticalStrut(10));
		
		panel.setBackground(Configuration.menubarBackground);
		
		panel.setMaximumSize(new Dimension(5200, 75));
		
		panel.add(verticalBox, BorderLayout.CENTER);
	}
	
	public JPanel getJPanel() {
		return this.panel;
	}
}

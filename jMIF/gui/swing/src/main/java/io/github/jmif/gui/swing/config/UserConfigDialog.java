package io.github.jmif.gui.swing.config;

import java.awt.Dimension;

import javax.inject.Inject;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class UserConfigDialog {
	@Inject
	private UserConfig userConfig;
	
	public UserConfigDialog() {
		
	}
	
	public void show() {
		JDialog dialog = new JDialog();

		var box = Box.createVerticalBox();
		JLabel generalProfileLabel = new JLabel("GENERAL_PROFILE");
		JLabel generalShowSplashscreenLabel = new JLabel("GENERAL_SHOW_SPLASHSCREEN");
		JLabel imageDurationLabel = new JLabel("IMAGE_DURATION");
		JLabel imageOverlayLabel = new JLabel("IMAGE_OVERLAY");
		JLabel videoOverlayLabel = new JLabel("VIDEO_OVERLAY");
		JLabel audioFadeInLabel = new JLabel("AUDIO_FADE_IN");
		JLabel audioFadeOutLabel = new JLabel("AUDIO_FADE_OUT");
		JLabel audioNormalizeLabel = new JLabel("AUDIO_NORMALIZE");
		JLabel textDurationLabel = new JLabel("TEXT_DURATION");
		JLabel textBGLabel = new JLabel("TEXT_BG");
		JLabel textFGLabel = new JLabel("TEXT_FG");
		JLabel textOLLabel = new JLabel("TEXT_OL");

		JTextField generalProfileTextfield = new JTextField(userConfig.getGENERAL_PROFILE());
		JCheckBox generalShowSplashCB = new JCheckBox();
		generalShowSplashCB.setSelected(userConfig.isGENERAL_SHOW_SPLASHSCREEN());
		JTextField imageDurationTextfield = new JTextField(String.valueOf(userConfig.getIMAGE_DURATION()));
		JTextField imageDOVerlayTextfield = new JTextField(String.valueOf(userConfig.getIMAGE_OVERLAY()));
		JTextField videoOVerlayTextfield = new JTextField(String.valueOf(userConfig.getVIDEO_OVERLAY()));
		JTextField audioFadeInTextfield = new JTextField(String.valueOf(userConfig.getAUDIO_FADE_IN()));
		JTextField audioFadeOutTextfield = new JTextField(String.valueOf(userConfig.getAUDIO_FADE_OUT()));
		JCheckBox audioNormalizeCB = new JCheckBox();
		audioNormalizeCB.setSelected(userConfig.isAUDIO_NORMALIZE());
		JTextField textDurationTextfield = new JTextField(String.valueOf(userConfig.getTEXT_DURATION()));
		JTextField textBGTextfield = new JTextField(userConfig.getTEXT_BG());
		JTextField textFGTextfield = new JTextField(userConfig.getTEXT_FG());
		JTextField textOLTextfield = new JTextField(userConfig.getTEXT_OL());
		
		
		append(box, generalProfileLabel, generalProfileTextfield);
		append(box, generalShowSplashscreenLabel, generalShowSplashCB);
		append(box, imageDurationLabel, imageDurationTextfield);
		append(box, imageOverlayLabel, imageDOVerlayTextfield);
		append(box, videoOverlayLabel, videoOVerlayTextfield);
		append(box, audioFadeInLabel, audioFadeInTextfield);
		append(box, audioFadeOutLabel, audioFadeOutTextfield);
		append(box, audioNormalizeLabel, audioNormalizeCB);
		append(box, textDurationLabel, textDurationTextfield);
		append(box, textBGLabel, textBGTextfield);
		append(box, textFGLabel, textFGTextfield);
		append(box, textOLLabel, textOLTextfield);

		JButton saveButton = new JButton("Save");
		saveButton.addActionListener(e -> {
			// TODO Save
			userConfig.setGENERAL_PROFILE(generalProfileTextfield.getText());
			userConfig.setGENERAL_SHOW_SPLASHSCREEN(generalShowSplashCB.isSelected());
			
			userConfig.setIMAGE_DURATION(Integer.valueOf(imageDurationTextfield.getText()));
			userConfig.setIMAGE_OVERLAY(Integer.valueOf(imageDOVerlayTextfield.getText()));
			
			userConfig.setVIDEO_OVERLAY(Integer.valueOf(videoOVerlayTextfield.getText()));
			
			userConfig.setAUDIO_FADE_IN(Integer.valueOf(audioFadeInTextfield.getText()));
			userConfig.setAUDIO_FADE_OUT(Integer.valueOf(audioFadeOutTextfield.getText()));
			userConfig.setAUDIO_NORMALIZE(audioNormalizeCB.isSelected());
			
			userConfig.setTEXT_DURATION(Integer.valueOf(textDurationTextfield.getText()));
			userConfig.setTEXT_BG(textBGTextfield.getText());
			userConfig.setTEXT_FG(textFGTextfield.getText());
			userConfig.setTEXT_OL(textOLTextfield.getText());
			
			userConfig.save();
			
			dialog.dispose();
		}); 
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> {
			dialog.dispose();
		});
		
		var hBox2 = Box.createHorizontalBox();
		hBox2.add(Box.createHorizontalGlue());
		hBox2.add(saveButton);
		hBox2.add(cancelButton);
		box.add(hBox2);
		
		// Add Save and Cancel-Buttons
		box.add(Box.createVerticalGlue());
		
		dialog.setModal(true);
		dialog.add(box);
		dialog.setSize(new Dimension(450, 500));
		dialog.setResizable(false);
		dialog.setVisible(true);
		dialog.pack();
	}
	
	private void append(Box box, JComponent c1, JComponent c2) {
		Dimension labelDim = new Dimension(140,20);
		
		c1.setPreferredSize(labelDim);
		c1.setMinimumSize(labelDim);
		c1.setMaximumSize(labelDim);

		var hBox2 = Box.createHorizontalBox();
		hBox2.add(c1);
		hBox2.add(c2);
		hBox2.add(Box.createHorizontalGlue());
		
		box.add(hBox2);
		box.add(Box.createVerticalStrut(5));
	}
}

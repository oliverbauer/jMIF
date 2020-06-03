package io.github.jmif.gui.swing.menu.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;

public class ButtonFactory {
	private static final int width = 50;
	private static final int height = 50;
	
	public static JButton newButton(final String img, final String imgHover) {
		JButton newButton = new JButton();
		URL newButtonRessource = ButtonFactory.class.getResource(img+".png");
//		newButton.setIcon(SVGLoader.getImage(imgW, imgH, newButtonRessource));
		
		// PRoblems with BATIK and JDK11

		ImageIcon ic = new ImageIcon(newButtonRessource);
		ic = new ImageIcon(getScaledImage(ic.getImage(), width, height));
		newButton.setIcon(ic);
		newButton.setPreferredSize(new Dimension(width, height));
		newButton.setMargin(new Insets(0,0,0,0));
		newButton.setBorderPainted(false);  
		newButton.setFocusPainted(false);  
		newButton.setContentAreaFilled(false); 
		newButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				URL newButtonRessource = ButtonFactory.class.getResource(img+".png");
				ImageIcon ic = new ImageIcon(newButtonRessource);
				ic = new ImageIcon(getScaledImage(ic.getImage(), width, height));
				newButton.setIcon(ic);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				URL newButtonRessource = ButtonFactory.class.getResource(imgHover+".png");
				ImageIcon ic = new ImageIcon(newButtonRessource);
				ic = new ImageIcon(getScaledImage(ic.getImage(), width, height));
				newButton.setIcon(ic);
			}
		});
		return newButton;
	}
	
	private static Image getScaledImage(final Image srcImg, final int w, final int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
}

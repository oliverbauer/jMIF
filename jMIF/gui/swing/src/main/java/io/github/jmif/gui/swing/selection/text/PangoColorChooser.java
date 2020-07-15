package io.github.jmif.gui.swing.selection.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.colorchooser.AbstractColorChooserPanel;

import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.util.AspectRatioUtil;

public class PangoColorChooser extends Box {
	private static final long serialVersionUID = 5368430354551770709L;
	
	private Image backgroundImage;
	
	private MIFTextFile mifText;
	private TextDetailsView textDetailsView;
	
	private Box box;
	
	private Color bgColor = Color.WHITE;
	private Color fgColor = Color.WHITE;
	private Color olColor = Color.WHITE;

    private AbstractColorChooserPanel bgACCP = null;
    private AbstractColorChooserPanel fgACCP = null;
    private AbstractColorChooserPanel olACCP = null;
	
    private int width = 400;
    private int height = 200;
    
	private JPanel imagePreview = new JPanel() {
		private static final long serialVersionUID = -3901061582672633414L;

		@Override
		public void paint(Graphics g) {
			if (backgroundImage != null) {
				var ic = new ImageIcon(backgroundImage);
				var image = getScaledImage(ic.getImage(), width, height);
				g.drawImage(image, 0, 0, null);
			}
			
			g.setColor(bgColor);

			// size of the text
			var w = width/2;
			var h = height/4;
			
			if (mifText != null) {
				var valign = mifText.getValign();
				var halign = mifText.getHalign();
				
				var x = -1;
				switch (halign) {
					case "left":
						x = 0;
						break;
					case "center":
						x = width/2 - w/2;
						break;
					case "right":
						x = width - w;
						break;
					default: throw new RuntimeException("Unknown horizontal alignment");
				}
				var y = -1;
				switch (valign) {
					case "top":
						y = 0;
						break;
					case "middle":
						y = height/2 - h/2;
						break;
					case "bottom":
						y = height - h;
						break;
					default: throw new RuntimeException("Unknown vertical alignment");
				}
				

				
				g.fillRect(x, y, w, h);
				
				g.setColor(olColor);
				g.drawRect(x, y, width, height);
				
				g.setColor(fgColor);
				g.setFont(new Font("TimesRoman", Font.BOLD, 20));
				g.drawString("example", x, y+25);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(width, height);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
	};

	private static BufferedImage getScaledImage(final Image srcImg, final int w, final int h){
	    var resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    var g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	public PangoColorChooser(Image backgroundImage, MIFTextFile mifText) {
		super(BoxLayout.X_AXIS);
		
		this.backgroundImage = backgroundImage;
		this.mifText = mifText;

		// Background
		/*
		 * Hackish but works for me: https://stackoverflow.com/questions/44233428/how-do-i-show-only-the-hsv-box-of-a-jcolorchooser
		 */
		JColorChooser bgColorChooser = new JColorChooser();
		bgColorChooser.setPreviewPanel(new JPanel());
        AbstractColorChooserPanel[] bgPanels = bgColorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : bgPanels) {
            if(!accp.getDisplayName().equals("RGB")) {
                bgColorChooser.removeChooserPanel(accp);
            } else {
            	bgACCP = accp;
            	
            	accp.getColorSelectionModel().addChangeListener(l -> {
            		Color c = accp.getColorSelectionModel().getSelectedColor();
            		bgColor = c;
            		if (this.mifText != null) {
	            		var r = String.format("%02X", bgColor.getRed());
	            		var g = String.format("%02X", bgColor.getGreen());
	            		var b = String.format("%02X", bgColor.getBlue());
	            		var a = String.format("%02X", bgColor.getAlpha());
	            		this.mifText.setBgcolour("0x"+r+g+b+a); // E.g. 0xff000080
	            		
	            		imagePreview.repaint();
	            		if (textDetailsView != null) {
	            			textDetailsView.setDetails(this.mifText);
	            		}
            		}
            	});
            }
        }
		
		JColorChooser fgColorChooser = new JColorChooser();
		fgColorChooser.setPreviewPanel(new JPanel());
        AbstractColorChooserPanel[] fgPanels = fgColorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : fgPanels) {
            if(!accp.getDisplayName().equals("RGB")) {
                fgColorChooser.removeChooserPanel(accp);
            } else {
            	fgACCP = accp;
            	
            	accp.getColorSelectionModel().addChangeListener(l -> {
            		Color c = accp.getColorSelectionModel().getSelectedColor();
            		fgColor = c;
            		if (this.mifText != null) {
	            		var r = String.format("%02X", fgColor.getRed());
	            		var g = String.format("%02X", fgColor.getGreen());
	            		var b = String.format("%02X", fgColor.getBlue());
	            		var a = String.format("%02X", fgColor.getAlpha());
	            		this.mifText.setFgcolour("0x"+r+g+b+a); // E.g. 0xff000080
	            		
	            		imagePreview.repaint();
	            		if (textDetailsView != null) {
	            			textDetailsView.setDetails(this.mifText);
	            		}
            		}
            	});
            }
        }
        
		JColorChooser olColorChooser = new JColorChooser();
		olColorChooser.setPreviewPanel(new JPanel());
        AbstractColorChooserPanel[] olPanels = olColorChooser.getChooserPanels();
        for (AbstractColorChooserPanel accp : olPanels) {
            if(!accp.getDisplayName().equals("RGB")) {
                olColorChooser.removeChooserPanel(accp);
            } else {
            	olACCP = accp;
            	
            	accp.getColorSelectionModel().addChangeListener(l -> {
            		Color c = accp.getColorSelectionModel().getSelectedColor();
            		olColor = c;
            		if (this.mifText != null) {
	            		var r = String.format("%02X", olColor.getRed());
	            		var g = String.format("%02X", olColor.getGreen());
	            		var b = String.format("%02X", olColor.getBlue());
	            		var a = String.format("%02X", olColor.getAlpha());
	            		this.mifText.setOlcolour("0x"+r+g+b+a); // E.g. 0xff000080
	            		
	            		imagePreview.repaint();
	            		if (textDetailsView != null) {
	            			textDetailsView.setDetails(this.mifText);
	            		}
            		}
            	});
            }
        }
		
		
		box = Box.createVerticalBox();
		
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("BG", bgACCP);
		tabPane.addTab("FG", fgACCP);
		tabPane.addTab("OL", olACCP);
		box.add(tabPane);

		add(imagePreview);
		add(Box.createHorizontalStrut(5));
		add(box);
	}
	
	public void updateImagePreview() {
		imagePreview.updateUI();
	}

	public void setMIFTextFile(MIFTextFile mifText2) {
		this.mifText = mifText2;
		
    	if (mifText != null) {
    		var bgcolour = mifText.getBgcolour(); // E.g. 0xff000080
    		bgcolour = bgcolour.substring(2);
    		var bgR = Integer.parseInt(bgcolour.substring(0, 2), 16);
    		var bgG = Integer.parseInt(bgcolour.substring(2, 4), 16);
    		var bgB = Integer.parseInt(bgcolour.substring(4, 6), 16);
    		var bgA = Integer.parseInt(bgcolour.substring(6, 8), 16);
    		bgColor = new Color(bgR, bgG, bgB, bgA);
    		bgACCP.getColorSelectionModel().setSelectedColor(bgColor);
    		
    		var fgcolour = mifText.getFgcolour(); // E.g. 0xff000080
    		fgcolour = fgcolour.substring(2);
    		var fgR = Integer.parseInt(fgcolour.substring(0, 2), 16);
    		var fgG = Integer.parseInt(fgcolour.substring(2, 4), 16);
    		var fgB = Integer.parseInt(fgcolour.substring(4, 6), 16);
    		var fgA = Integer.parseInt(fgcolour.substring(6, 8), 16);
    		fgColor = new Color(fgR, fgG, fgB, fgA);
    		fgACCP.getColorSelectionModel().setSelectedColor(fgColor);
    		
    		var olcolour = mifText.getOlcolour(); // E.g. 0xff000080
    		olcolour = olcolour.substring(2);
    		var olR = Integer.parseInt(olcolour.substring(0, 2), 16);
    		var olG = Integer.parseInt(olcolour.substring(2, 4), 16);
    		var olB = Integer.parseInt(olcolour.substring(4, 6), 16);
    		var olA = Integer.parseInt(olcolour.substring(6, 8), 16);
    		olColor = new Color(olR, olG, olB, olA);
    		olACCP.getColorSelectionModel().setSelectedColor(olColor);
    	}
	}
	
	public void setBackgroundImage(MIFFile mifFile, Image image) {
		this.backgroundImage = image;
		// TODO 4:3? What if 16:9 is selected as profile?
		height = (int)new AspectRatioUtil().getHeight(mifFile.getWidth(), mifFile.getHeight(), width);
		imagePreview.updateUI();
	}

	public void setTextDetailsView(TextDetailsView textDetailsView) {
		this.textDetailsView = textDetailsView;
	}
}
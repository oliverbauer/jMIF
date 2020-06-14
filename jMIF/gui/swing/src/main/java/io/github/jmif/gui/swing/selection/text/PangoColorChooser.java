package io.github.jmif.gui.swing.selection.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import io.github.jmif.entities.MIFTextFile;

public class PangoColorChooser extends Box {
	private static final long serialVersionUID = 5368430354551770709L;
	private File backgroundImage;
	private MIFTextFile mifText;
	private TextDetailsView textDetailsView;
	
	// Just text to indicate RGBA
	private JLabel bgLabelR = new JLabel("R");
	private JLabel bgLabelG = new JLabel("G");
	private JLabel bgLabelB = new JLabel("B");
	private JLabel bgLabelA = new JLabel("A");

	private JLabel fgLabelR = new JLabel("R");
	private JLabel fgLabelG = new JLabel("G");
	private JLabel fgLabelB = new JLabel("B");
	private JLabel fgLabelA = new JLabel("A");

	private JLabel olLabelR = new JLabel("R");
	private JLabel olLabelG = new JLabel("G");
	private JLabel olLabelB = new JLabel("B");
	private JLabel olLabelA = new JLabel("A");

	// Slider for changing the RGBA values
	private JSlider bgSliderR = new JSlider(0, 255);
	private JSlider bgSliderG = new JSlider(0, 255);
	private JSlider bgSliderB = new JSlider(0, 255);
	private JSlider bgSliderA = new JSlider(0, 255);

	private JSlider fgSliderR = new JSlider(0, 255);
	private JSlider fgSliderG = new JSlider(0, 255);
	private JSlider fgSliderB = new JSlider(0, 255);
	private JSlider fgSliderA = new JSlider(0, 255);

	private JSlider olSliderR = new JSlider(0, 255);
	private JSlider olSliderG = new JSlider(0, 255);
	private JSlider olSliderB = new JSlider(0, 255);
	private JSlider olSliderA = new JSlider(0, 255);

	// Label to show the currently selected value
	private JLabel bgValueR = new JLabel();
	private JLabel bgValueG = new JLabel();
	private JLabel bgValueB = new JLabel();
	private JLabel bgValueA = new JLabel();

	private JLabel fgValueR = new JLabel();
	private JLabel fgValueG = new JLabel();
	private JLabel fgValueB = new JLabel();
	private JLabel fgValueA = new JLabel();

	private JLabel olValueR = new JLabel();
	private JLabel olValueG = new JLabel();
	private JLabel olValueB = new JLabel();
	private JLabel olValueA = new JLabel();
	
	private Box box;
	
	private Color bgColor = Color.WHITE;
	private Color fgColor = Color.WHITE;
	private Color olColor = Color.WHITE;

	private JPanel imagePreview = new JPanel() {
		private static final long serialVersionUID = -3901061582672633414L;

		@Override
		public void paint(Graphics g) {
			if (backgroundImage != null) {
				ImageIcon ic = new ImageIcon(backgroundImage.getAbsolutePath());
				BufferedImage image = getScaledImage(ic.getImage(), 200, 200);
				g.drawImage(image, 0, 0, null);
			}
			
			g.setColor(bgColor);
			
			if (mifText != null) {
				String valign = mifText.getValign();
				String halign = mifText.getHalign();
				
				int x = -1;
				switch (halign) {
					case "left":
						x = 0;
						break;
					case "center":
						x = 50; // [50 to 150]
						break;
					case "right":
						x = 100; // [100 to 200]
						break;
					default: throw new RuntimeException("Unknown horizontal alignment");
				}
				int y = -1;
				switch (valign) {
					case "top":
						y = 0;
						break;
					case "middle":
						y = 75;
						break;
					case "bottom":
						y = 150;
						break;
					default: throw new RuntimeException("Unknown vertical alignment");
				}
				
				int w = 100;
				int h = 50;
				
				g.fillRect(x, y, w, h);
				
				g.setColor(olColor);
				g.drawRect(x, y, 200, 200);
				
				g.setColor(fgColor);
				g.setFont(new Font("TimesRoman", Font.BOLD, 20));
				g.drawString("example", x, y+25);
			}
		}

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200, 200);
		}

		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}
	};

	private static BufferedImage getScaledImage(final Image srcImg, final int w, final int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	public PangoColorChooser(File backgroundImage, MIFTextFile mifText) {
		super(BoxLayout.X_AXIS);
		
		this.backgroundImage = backgroundImage;
		this.mifText = mifText;

		// Background
		Box bgRedBox = Box.createHorizontalBox();
		bgRedBox.add(bgLabelR);
		bgRedBox.add(bgSliderR);
		bgRedBox.add(bgValueR);
		Box bgGreenBox = Box.createHorizontalBox();
		bgGreenBox.add(bgLabelG);
		bgGreenBox.add(bgSliderG);
		bgGreenBox.add(bgValueG);
		Box bgBlueBox = Box.createHorizontalBox();
		bgBlueBox.add(bgLabelB);
		bgBlueBox.add(bgSliderB);
		bgBlueBox.add(bgValueB);
		Box bgAlphaBox = Box.createHorizontalBox();
		bgAlphaBox.add(bgLabelA);
		bgAlphaBox.add(bgSliderA);
		bgAlphaBox.add(bgValueA);
		Box bgTextBox = Box.createHorizontalBox();
		bgTextBox.add(new JLabel("Background-Color"));
		bgTextBox.add(Box.createHorizontalGlue());
		bgSliderR.setValue(bgColor.getRed());
		bgValueR.setText(String.valueOf(bgColor.getRed()));
		bgSliderG.setValue(bgColor.getGreen());
		bgValueG.setText(String.valueOf(bgColor.getGreen()));
		bgSliderB.setValue(bgColor.getBlue());
		bgValueB.setText(String.valueOf(bgColor.getBlue()));
		bgSliderA.setValue(bgColor.getAlpha());
		bgValueA.setText(String.valueOf(bgColor.getAlpha()));
		bgSliderR.addChangeListener(e -> updateBgColorStateChanged());
		bgSliderG.addChangeListener(e -> updateBgColorStateChanged());
		bgSliderB.addChangeListener(e -> updateBgColorStateChanged());
		bgSliderA.addChangeListener(e -> updateBgColorStateChanged());

		// Foreground
		Box fgRedBox = Box.createHorizontalBox();
		fgRedBox.add(fgLabelR);
		fgRedBox.add(fgSliderR);
		fgRedBox.add(fgValueR);
		Box fgGreenBox = Box.createHorizontalBox();
		fgGreenBox.add(fgLabelG);
		fgGreenBox.add(fgSliderG);
		fgGreenBox.add(fgValueG);
		Box fgBlueBox = Box.createHorizontalBox();
		fgBlueBox.add(fgLabelB);
		fgBlueBox.add(fgSliderB);
		fgBlueBox.add(fgValueB);
		Box fgAlphaBox = Box.createHorizontalBox();
		fgAlphaBox.add(fgLabelA);
		fgAlphaBox.add(fgSliderA);
		fgAlphaBox.add(fgValueA);
		Box fgTextBox = Box.createHorizontalBox();
		fgTextBox.add(new JLabel("Foreground-Color"));
		fgTextBox.add(Box.createHorizontalGlue());
		fgSliderR.setValue(fgColor.getRed());
		fgValueR.setText(String.valueOf(fgColor.getRed()));
		fgSliderG.setValue(fgColor.getGreen());
		fgValueG.setText(String.valueOf(fgColor.getGreen()));
		fgSliderB.setValue(fgColor.getBlue());
		fgValueB.setText(String.valueOf(fgColor.getBlue()));
		fgSliderA.setValue(fgColor.getAlpha());
		fgValueA.setText(String.valueOf(fgColor.getAlpha()));
		fgSliderR.addChangeListener(e -> updateFgColorStateChanged());
		fgSliderG.addChangeListener(e -> updateFgColorStateChanged());
		fgSliderB.addChangeListener(e -> updateFgColorStateChanged());
		fgSliderA.addChangeListener(e -> updateFgColorStateChanged());


		// Outline
		Box olRedBox = Box.createHorizontalBox();
		olRedBox.add(olLabelR);
		olRedBox.add(olSliderR);
		olRedBox.add(olValueR);
		Box olGreenBox = Box.createHorizontalBox();
		olGreenBox.add(olLabelG);
		olGreenBox.add(olSliderG);
		olGreenBox.add(olValueG);
		Box olBlueBox = Box.createHorizontalBox();
		olBlueBox.add(olLabelB);
		olBlueBox.add(olSliderB);
		olBlueBox.add(olValueB);
		Box olAlphaBox = Box.createHorizontalBox();
		olAlphaBox.add(olLabelA);
		olAlphaBox.add(olSliderA);
		olAlphaBox.add(olValueA);
		Box olTextBox = Box.createHorizontalBox();
		olTextBox.add(new JLabel("Outline-Color"));
		olTextBox.add(Box.createHorizontalGlue());
		olSliderR.setValue(olColor.getRed());
		olValueR.setText(String.valueOf(olColor.getRed()));
		olSliderG.setValue(olColor.getGreen());
		olValueG.setText(String.valueOf(olColor.getGreen()));
		olSliderB.setValue(olColor.getBlue());
		olValueB.setText(String.valueOf(olColor.getBlue()));
		olSliderA.setValue(olColor.getAlpha());
		olValueA.setText(String.valueOf(olColor.getAlpha()));
		olSliderR.addChangeListener(e -> updateOlColorStateChanged());
		olSliderG.addChangeListener(e -> updateOlColorStateChanged());
		olSliderB.addChangeListener(e -> updateOlColorStateChanged());
		olSliderA.addChangeListener(e -> updateOlColorStateChanged());


		box = Box.createVerticalBox();
		box.add(bgTextBox);
		box.add(bgRedBox);
		box.add(bgGreenBox);
		box.add(bgBlueBox);
		box.add(bgAlphaBox);
		box.add(Box.createVerticalStrut(10));
		box.add(fgTextBox);
		box.add(fgRedBox);
		box.add(fgGreenBox);
		box.add(fgBlueBox);
		box.add(fgAlphaBox);
		box.add(Box.createVerticalStrut(10));
		box.add(olTextBox);
		box.add(olRedBox);
		box.add(olGreenBox);
		box.add(olBlueBox);
		box.add(olAlphaBox);

		add(imagePreview);
		add(Box.createHorizontalStrut(5));
		add(box);
	}
	
	public void updateImagePreview() {
		imagePreview.updateUI();
	}

	private void updateBgColor() {
		this.bgColor = new Color(bgSliderR.getValue(), bgSliderG.getValue(), bgSliderB.getValue(), bgSliderA.getValue());

		bgSliderR.setValue(bgColor.getRed());
		bgSliderG.setValue(bgColor.getGreen());
		bgSliderB.setValue(bgColor.getBlue());
		bgSliderA.setValue(bgColor.getAlpha());

		bgValueR.setText(String.valueOf(bgColor.getRed()));
		bgValueG.setText(String.valueOf(bgColor.getGreen()));
		bgValueB.setText(String.valueOf(bgColor.getBlue()));
		bgValueA.setText(String.valueOf(bgColor.getAlpha()));
		
		String r = String.format("%02X", bgColor.getRed());
		String g = String.format("%02X", bgColor.getGreen());
		String b = String.format("%02X", bgColor.getBlue());
		String a = String.format("%02X", bgColor.getAlpha());
		mifText.setBgcolour("0x"+r+g+b+a); // E.g. 0xff000080
	}

	private void updateFgColor() {
		this.fgColor = new Color(fgSliderR.getValue(), fgSliderG.getValue(), fgSliderB.getValue(), fgSliderA.getValue());

		fgSliderR.setValue(fgColor.getRed());
		fgSliderG.setValue(fgColor.getGreen());
		fgSliderB.setValue(fgColor.getBlue());
		fgSliderA.setValue(fgColor.getAlpha());

		fgValueR.setText(String.valueOf(fgColor.getRed()));
		fgValueG.setText(String.valueOf(fgColor.getGreen()));
		fgValueB.setText(String.valueOf(fgColor.getBlue()));
		fgValueA.setText(String.valueOf(fgColor.getAlpha()));
		
		String r = String.format("%02X", fgColor.getRed());
		String g = String.format("%02X", fgColor.getGreen());
		String b = String.format("%02X", fgColor.getBlue());
		String a = String.format("%02X", fgColor.getAlpha());
		mifText.setFgcolour("0x"+r+g+b+a);
	}
	
	private void updateOlColor() {
		this.olColor = new Color(olSliderR.getValue(), olSliderG.getValue(), olSliderB.getValue(), olSliderA.getValue());

		olSliderR.setValue(olColor.getRed());
		olSliderG.setValue(olColor.getGreen());
		olSliderB.setValue(olColor.getBlue());
		olSliderA.setValue(olColor.getAlpha());

		olValueR.setText(String.valueOf(olColor.getRed()));
		olValueG.setText(String.valueOf(olColor.getGreen()));
		olValueB.setText(String.valueOf(olColor.getBlue()));
		olValueA.setText(String.valueOf(olColor.getAlpha()));
		
		String r = String.format("%02X", olColor.getRed());
		String g = String.format("%02X", olColor.getGreen());
		String b = String.format("%02X", olColor.getBlue());
		String a = String.format("%02X", olColor.getAlpha());
		mifText.setOlcolour("0x"+r+g+b+a);
	}
	
	private void updateBgColorStateChanged() {
		updateBgColor();
		imagePreview.repaint();
		if (textDetailsView != null) {
			textDetailsView.setDetails(mifText);
		}
	}
	
	private void updateFgColorStateChanged() {
		updateFgColor();
		imagePreview.repaint();
		if (textDetailsView != null) {
			textDetailsView.setDetails(mifText);
		}
	}
	
	private void updateOlColorStateChanged() {
		updateOlColor();
		imagePreview.repaint();
		if (textDetailsView != null) {
			textDetailsView.setDetails(mifText);
		}
	}

	public void setMIFTextFile(MIFTextFile mifText2) {
		this.mifText = mifText2;
		
		String bgcolour = mifText2.getBgcolour(); // E.g. 0xff000080
		bgcolour = bgcolour.substring(2);
		int bgR = Integer.parseInt(bgcolour.substring(0, 2), 16);
		int bgG = Integer.parseInt(bgcolour.substring(2, 4), 16);
		int bgB = Integer.parseInt(bgcolour.substring(4, 6), 16);
		int bgA = Integer.parseInt(bgcolour.substring(6, 8), 16);
		bgSliderR.setValue(bgR);
		bgSliderG.setValue(bgG);
		bgSliderB.setValue(bgB);
		bgSliderA.setValue(bgA);
		updateBgColor();
		
		// Initial: 0x000000ff
		String fgcolour = mifText2.getFgcolour();
		fgcolour = fgcolour.substring(2);
		int fgR = Integer.parseInt(fgcolour.substring(0, 2), 16);
		int fgG = Integer.parseInt(fgcolour.substring(2, 4), 16);
		int fgB = Integer.parseInt(fgcolour.substring(4, 6), 16);
		int fgA = Integer.parseInt(fgcolour.substring(6, 8), 16);
		fgSliderR.setValue(fgR);
		fgSliderG.setValue(fgG);
		fgSliderB.setValue(fgB);
		fgSliderA.setValue(fgA);
		updateFgColor();
		
		String olcolour = mifText2.getOlcolour();
		olcolour = olcolour.substring(2);
		int olR = Integer.parseInt(olcolour.substring(0, 2), 16);
		int olG = Integer.parseInt(olcolour.substring(2, 4), 16);
		int olB = Integer.parseInt(olcolour.substring(4, 6), 16);
		int olA = Integer.parseInt(olcolour.substring(6, 8), 16);
		olSliderR.setValue(olR);
		olSliderG.setValue(olG);
		olSliderB.setValue(olB);
		olSliderA.setValue(olA);
		updateOlColor();
	}

	public void setTextDetailsView(TextDetailsView textDetailsView) {
		this.textDetailsView = textDetailsView;
	}
}
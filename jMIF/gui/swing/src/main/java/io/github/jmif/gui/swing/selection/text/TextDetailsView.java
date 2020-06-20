package io.github.jmif.gui.swing.selection.text;

import java.awt.Dimension;
import java.io.File;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;

import io.github.jmif.config.Configuration;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFImageWrapper;
import io.github.jmif.gui.swing.entities.MIFTextFileWrapper;

public class TextDetailsView {
	private static final Logger logger = LoggerFactory.getLogger(TextDetailsView.class);
	
	private Box panel;
	
	private JLabel labelText = new JLabel("Text");
	private JLabel labelLength = new JLabel("Length [ms]");
	private JLabel labelSize = new JLabel("Size");
	private JLabel labelWeight = new JLabel("Weight");
	private JLabel labelFgColor = new JLabel("Foreground color");
	private JLabel labelBgColor = new JLabel("Background color");
	private JLabel labelOlColor = new JLabel("Outline color");
	
	private JLabel labelVAlign = new JLabel("Vertical alignment");
	private JLabel labelHAlign = new JLabel("Horizontal alignment");
	
	private JTextField text = new JTextField();
	private JTextField length = new JTextField();
	private JTextField size = new JTextField();
	private JTextField weight = new JTextField();
	private JTextField fgColor = new JTextField();
	private JTextField bgColor = new JTextField();
	private JTextField olColor = new JTextField();
	private JComboBox<String> valign;
	private JComboBox<String> halign;
	
	private PangoColorChooser chooser;
	private MIFTextFileWrapper mifText;
	
	public TextDetailsView(GraphWrapper graphwrapper) {
		var box = Box.createVerticalBox();
		
	    wrap(box, labelText, text);
	    wrap(box, labelLength, length);
	    wrap(box, labelSize, size);
	    wrap(box, labelWeight, weight);
	    wrap(box, labelBgColor, bgColor);
	    wrap(box, labelFgColor, fgColor);
	    wrap(box, labelOlColor, olColor);
	    
	    bgColor.addActionListener(e -> {
	    	logger.info("Changed bgcolor to {}", bgColor.getText());
	    	mifText.setBgcolour(bgColor.getText());
	    	chooser.setMIFTextFile(mifText);
	    });
	    fgColor.addActionListener(e -> {
	    	logger.info("Changed fgcolor to {}", fgColor.getText());
	    	mifText.setFgcolour(fgColor.getText());
	    	chooser.setMIFTextFile(mifText);
	    });
	    olColor.addActionListener(e -> {
	    	logger.info("Changed olcolor to {}", olColor.getText());
	    	mifText.setOlcolour(olColor.getText());
	    	chooser.setMIFTextFile(mifText);
	    });
	    
	    // -attach affine transition.valign=top transition.halign=center
		var valignValues = Arrays.asList("top", "middle", "bottom").toArray(new String[3]);
		valign = new JComboBox<>(valignValues);
		valign.addItemListener(e -> {
			var value = (String)valign.getSelectedItem();
			mifText.setValign(value);
			chooser.updateImagePreview();
		});
		wrap(box, labelVAlign, valign);
		
		// -attach affine transition.valign=top transition.halign=center
		var halignValues = Arrays.asList("left", "center", "right").toArray(new String[3]);
		halign = new JComboBox<>(halignValues);
		halign.addItemListener(e -> {
			var value = (String)halign.getSelectedItem();
			mifText.setHalign(value);
			chooser.updateImagePreview();
		});
		wrap(box, labelHAlign, halign);

		
		File file = null;
		for (mxCell c : graphwrapper.getCells()) {
			var f = graphwrapper.get(c);
			if (f instanceof MIFImageWrapper) {
				file = f.getFile();
				break;
			}
		}
		
		chooser = new PangoColorChooser(file, mifText);
	    var boxFilename = Box.createHorizontalBox();
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    var label = new JLabel();
	    label.setMinimumSize(new Dimension(140, 20));
	    label.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(label);
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFilename.add(chooser);
	    boxFilename.add(Box.createHorizontalGlue());
	    box.add(boxFilename);
		
	    text.addActionListener(e -> { mifText.setText(text.getText()); });
	    length.addActionListener(e -> { mifText.setLength(Integer.parseInt(length.getText())); graphwrapper.redrawGraph();});
	    size.addActionListener(e -> { mifText.setSize(Integer.parseInt(size.getText())); });
	    weight.addActionListener(e -> { mifText.setWeight(Integer.parseInt(weight.getText())); });
	    fgColor.addActionListener(e -> { 
	    	mifText.setFgcolour(fgColor.getText());
	    	chooser.updateImagePreview();
	    });
	    bgColor.addActionListener(e -> { 
	    	mifText.setBgcolour(bgColor.getText());
	    	chooser.updateImagePreview();
	    });
	    olColor.addActionListener(e -> { 
	    	mifText.setOlcolour(olColor.getText());
	    	chooser.updateImagePreview();
	    });
	    
	    // TODO Audio: Alignment
	    
	    box.add(Box.createRigidArea(new Dimension(0, 10)));
		
		panel = Box.createHorizontalBox();
		panel.add(box);
		panel.setBackground(Configuration.bgColor);
		panel.add(Box.createHorizontalGlue());
	}

	private void wrap(Box box, JComponent c1, JComponent c2) {
	    var boxFilename = Box.createHorizontalBox();
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    c1.setMinimumSize(new Dimension(140, 20));
	    c1.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(c1);
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFilename.add(c2);
	    boxFilename.add(Box.createHorizontalGlue());
	    
	    box.add(boxFilename);
	}
	
	public Box getBox() {
		return panel;
	}
	
	public void setDetails(MIFTextFileWrapper mifText) {
		this.mifText = mifText;
		chooser.setMIFTextFile(mifText);
		chooser.setTextDetailsView(this);
		chooser.updateImagePreview();
		
		text.setText(mifText.getText());
		length.setText(String.valueOf(mifText.getLength()));
		size.setText(String.valueOf(mifText.getSize()));
		weight.setText(String.valueOf(mifText.getWeight()));
		fgColor.setText(mifText.getFgcolour());
		bgColor.setText(mifText.getBgcolour());
		olColor.setText(mifText.getOlcolour());
		valign.setSelectedItem(mifText.getValign());
		halign.setSelectedItem(mifText.getHalign());
		
		panel.updateUI();
	}
}

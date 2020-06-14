package io.github.jmif.gui.swing.selection.text;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import io.github.jmif.config.Configuration;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.gui.swing.GraphWrapper;

public class TextDetailsView {
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
	
	private MIFTextFile mifText;
	
	public TextDetailsView(GraphWrapper graphwrapper) {
		Box box = Box.createVerticalBox();
		
	    wrap(box, labelText, text);
	    wrap(box, labelLength, length);
	    wrap(box, labelSize, size);
	    wrap(box, labelWeight, weight);
	    wrap(box, labelFgColor, fgColor);
	    wrap(box, labelBgColor, bgColor);
	    wrap(box, labelOlColor, olColor);
	    
	    // -attach affine transition.valign=top transition.halign=center
		String[] valignValues = Arrays.asList("top", "middle", "bottom").toArray(new String[3]);
		valign = new JComboBox<>(valignValues);
		valign.addItemListener(e -> {
			String value = (String)valign.getSelectedItem();
			mifText.setValign(value);
		});
		wrap(box, labelVAlign, valign);
		
		// -attach affine transition.valign=top transition.halign=center
		String[] halignValues = Arrays.asList("left", "center", "right").toArray(new String[3]);
		halign = new JComboBox<>(halignValues);
		halign.addItemListener(e -> {
			String value = (String)halign.getSelectedItem();
			mifText.setHalign(value);
		});
		wrap(box, labelHAlign, halign);
	    
	    text.addActionListener(e -> { mifText.setText(text.getText()); });
	    length.addActionListener(e -> { mifText.setLength(Integer.parseInt(length.getText())); graphwrapper.redrawGraph();});
	    size.addActionListener(e -> { mifText.setSize(Integer.parseInt(size.getText())); });
	    weight.addActionListener(e -> { mifText.setWeight(Integer.parseInt(weight.getText())); });
	    fgColor.addActionListener(e -> { mifText.setFgcolour(fgColor.getText()); });
	    bgColor.addActionListener(e -> { mifText.setBgcolour(bgColor.getText()); });
	    olColor.addActionListener(e -> { mifText.setOlcolour(olColor.getText()); });
	    
	    // TODO Audio: Alignment
	    
	    box.add(Box.createRigidArea(new Dimension(0, 10)));
		
		panel = Box.createHorizontalBox();
		panel.add(box);
		panel.setBackground(Configuration.bgColor);
		panel.add(Box.createHorizontalGlue());
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.RED, 2, true));
		}
		
		Dimension dim = new Dimension(5200, 200);
		panel.setPreferredSize(dim);
		panel.setMinimumSize(dim);
		panel.setMaximumSize(dim);
	}

	private void wrap(Box box, JComponent c1, JComponent c2) {
	    Box boxFilename = Box.createHorizontalBox();
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
	
	public void setDetails(MIFTextFile mifText) {
		this.mifText = mifText;
		
		text.setText(mifText.getText());
		length.setText(String.valueOf(mifText.getLength()));
		size.setText(String.valueOf(mifText.getSize()));
		weight.setText(String.valueOf(mifText.getWeight()));
		fgColor.setText(mifText.getFgcolour());
		bgColor.setText(mifText.getBgcolour());
		olColor.setText(mifText.getOlcolour());
		valign.setSelectedItem(mifText.getValign());
		halign.setSelectedItem(mifText.getHalign());
	}
}

package io.github.jmif.gui.swing.selection.text;

import java.awt.Dimension;
import java.awt.Image;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.mxgraph.model.mxCell;

import io.github.jmif.core.LocalService;
import io.github.jmif.core.MIFException;
import io.github.jmif.entities.MIFFile;
import io.github.jmif.entities.MIFImage;
import io.github.jmif.entities.MIFTextFile;
import io.github.jmif.entities.MIFVideo;
import io.github.jmif.gui.swing.GraphWrapper;

public class TextDetailsView {
	private GraphWrapper graphwrapper;
	
	private Box box;
	
	private JTextField text = new JTextField();
	private JTextField length = new JTextField();
	private JTextField size = new JTextField();
	private JTextField weight = new JTextField();
	private JTextField fgColor = new JTextField();
	private JTextField bgColor = new JTextField();
	private JTextField olColor = new JTextField();
	private JComboBox<String> valign = new JComboBox<>(Arrays.asList("top", "middle", "bottom").toArray(new String[3]));
	private JComboBox<String> halign = new JComboBox<>(Arrays.asList("left", "center", "right").toArray(new String[3]));;
	
	private JButton previewButton = new JButton("Preview");
	
	private PangoColorChooser chooser;
	private MIFTextFile mifText;
	
	private JCheckBox affineTransitonCB = new JCheckBox("Add affine transition?");
	private JTextArea affineTransition = new JTextArea(8,40);
	private String affineGeometry = "";
	
	public TextDetailsView(GraphWrapper graphwrapper) {
		this.graphwrapper = graphwrapper;
		
		var vBox = Box.createVerticalBox();
		
	    wrap(vBox, new JLabel("Text"), text);
	    wrap(vBox, new JLabel("Length [ms]"), length);
	    wrap(vBox, new JLabel("Size"), size);
	    wrap(vBox, new JLabel("Weight"), weight);
	    wrap(vBox, new JLabel("Background color"), bgColor);
	    wrap(vBox, new JLabel("Foreground color"), fgColor);
	    wrap(vBox, new JLabel("Outline color"), olColor);
	    wrap(vBox, new JLabel("Vertical alignment"), valign);
	    wrap(vBox, new JLabel("Horizontal alignment"), halign);

		chooser = new PangoColorChooser(null, mifText);
	    var boxFilename = Box.createHorizontalBox();
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    var label = new JLabel();
	    label.setMinimumSize(new Dimension(140, 20));
	    label.setPreferredSize(new Dimension(140, 20));
	    boxFilename.add(label);
	    boxFilename.add(Box.createRigidArea(new Dimension(10, 0)));
	    boxFilename.add(chooser);
	    boxFilename.add(Box.createHorizontalGlue());
	    vBox.add(boxFilename);
	    vBox.add(Box.createRigidArea(new Dimension(0, 10)));
	    
	    wrap(vBox, affineTransitonCB, new JLabel());
	    wrap(vBox, new JLabel(""), affineTransition);
	    wrap(vBox, new JLabel(""), previewButton);
	    
		box = Box.createHorizontalBox();
		box.add(vBox);
		
		affineTransition.setVisible(false); // initial: do not add affine transition
		affineTransition.setWrapStyleWord(true);
		affineTransition.setLineWrap(true);

		initListeners();
		
		box.add(Box.createHorizontalGlue());
	}
	
	private void initListeners() {
		valign.addItemListener(e -> updateValueAndChooser(mifText::setValign, valign::getSelectedItem, Object::toString));
		halign.addItemListener(e -> updateValueAndChooser(mifText::setHalign, halign::getSelectedItem, Object::toString));
		
	    text.addActionListener(e -> { mifText.setText(text.getText()); });
	    length.addActionListener(e -> { 
	    	updateValueAndChooser(mifText::setLength, length::getText, Integer::parseInt);

	    	setDetails(mifText); // to update affine transition TODO user should be asked for that!
	    	graphwrapper.redrawGraph();
	    });
	    size.addActionListener(e -> updateValueAndChooser(mifText::setSize, size::getText, Integer::parseInt));
	    weight.addActionListener(e -> updateValueAndChooser(mifText::setWeight, weight::getText, Integer::parseInt));
	    fgColor.addActionListener(e -> updateValueAndChooser(mifText::setFgcolour, fgColor::getText));
	    bgColor.addActionListener(e -> updateValueAndChooser(mifText::setBgcolour, bgColor::getText));
	    olColor.addActionListener(e -> updateValueAndChooser(mifText::setOlcolour, olColor::getText));
		
		previewButton.addActionListener(e -> createAffineTextPreview(LocalService::new, mifText));
		
		affineTransitonCB.addActionListener(e -> {
			updateValueAndChooser(affineTransition::setVisible, affineTransitonCB::isSelected);
			updateValueAndChooser(mifText::setUseAffineTransition, affineTransitonCB::isSelected);
			box.updateUI();
		});
		
		affineTransition.getDocument().addDocumentListener(new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) {
	        	updateValueAndChooser(mifText::setAffineTransition, affineTransition::getText);
	        }

	        @Override
	        public void insertUpdate(DocumentEvent e) {
	        	updateValueAndChooser(mifText::setAffineTransition, affineTransition::getText);
	        }

	        @Override
	        public void changedUpdate(DocumentEvent e) {
	        	updateValueAndChooser(mifText::setAffineTransition, affineTransition::getText);
	        }
	    });
	}
	
	private void createAffineTextPreview(Supplier<LocalService> supplier, MIFTextFile text) {
		try {
			supplier.get().affineTextPreview(graphwrapper.getPr(), text);
		} catch (MIFException e1) {
			e1.printStackTrace();
		}
	}
	
	private <S,T> void updateValueAndChooser(Consumer<T> consumer, Supplier<S> producer, Function<S,T> function) {
		consumer.accept(function.apply(producer.get()));
		chooser.setMIFTextFile(mifText);
    	chooser.updateImagePreview();
    	graphwrapper.redrawGraph();
	}
	
	private <T> void updateValueAndChooser(Consumer<T> consumer, Supplier<T> producer) {
    	consumer.accept(producer.get());
    	chooser.setMIFTextFile(mifText);
    	chooser.updateImagePreview();
    	graphwrapper.redrawGraph();
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
		return box;
	}
	
	public void setDetails(MIFTextFile mifText) {
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
		
		if (mifText.isUseAffineTransition()) {
			affineTransition.setVisible(true);
			affineTransitonCB.setSelected(true);
		} else {
			affineTransition.setVisible(false);
			affineTransitonCB.setSelected(false);
		}
		
	    affineGeometry = mifText.getAffineTransition();
		affineTransition.setText(affineGeometry);
		mifText.setAffineTransition(affineGeometry);
		
		var startFrameOfText = 0;
		for (mxCell c : graphwrapper.getTextCells()) {
			MIFTextFile text = graphwrapper.getText(c);
			if (text == mifText) {
				break;
			}
			startFrameOfText += (text.getLength()/1000)*graphwrapper.getPr().getProfileFramerate();
		}
		var currentFrame = 0;
		MIFFile file = null;
		Image backgroundImage = null;
		var isFirst = true;
		for (mxCell c : graphwrapper.getCells()) {
			var f = graphwrapper.get(c);
				
			var duration = (f.getDuration()/1000)*graphwrapper.getPr().getProfileFramerate();
			var overlay = (f.getOverlayToPrevious()/1000)*graphwrapper.getPr().getProfileFramerate();
			if (isFirst) {
				overlay = 0;
			}
				
			if (f instanceof MIFImage) {
				if (currentFrame+duration-overlay > startFrameOfText) {
					currentFrame += startFrameOfText;
					backgroundImage = ((MIFImage) f).getPreviewCrop();
					file = f;
					break;
				}
				currentFrame += duration - overlay;
					
			} else if (f instanceof MIFVideo) {
				if (currentFrame+duration-overlay > startFrameOfText) {
					currentFrame += startFrameOfText;
					backgroundImage = ((MIFVideo)f).getPreviewImages().iterator().next();
					file = f;
					break;
				}
				
				currentFrame += duration - overlay;
			}
			isFirst = false;
		}
		
		chooser.setBackgroundImage(file, backgroundImage);
		chooser.updateUI();
		box.updateUI();
	}
}

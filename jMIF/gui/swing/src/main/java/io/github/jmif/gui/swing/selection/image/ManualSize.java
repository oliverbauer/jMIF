package io.github.jmif.gui.swing.selection.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import io.github.jmif.core.MIFException;
import io.github.jmif.gui.swing.GraphWrapper;
import io.github.jmif.gui.swing.entities.MIFImageWrapper;

public class ManualSize {
	private static final Logger logger = LoggerFactory.getLogger(ManualSize.class);

	private String command = null;
	
	public void showFrame(final GraphWrapper graphWrapper, MIFImageWrapper mifImage, ImageView imageView) {
		var frame = new JFrame();
		var panel = new JPanel(new BorderLayout());

		var ic = new ImageIcon(mifImage.getFile().getAbsolutePath());

		var w = ic.getIconWidth() / 5;
		var h = ic.getIconHeight() / 5;
		var resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		var g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(ic.getImage(), 0, 0, w, h, null);
		g2.dispose();

		var ratio = 1920d / 1080d;
		var displayratio = (double) w / (double) h;

		var input = ic.getIconWidth() + "x" + ic.getIconHeight();
		var inputRadio = (double) ic.getIconWidth() / (double) ic.getIconHeight();
		var output = w + "x" + h;

		logger.info("Ratio " + ratio + " 1920x1080 scaled from " + input + " (ASP " + inputRadio + ") to " + output
				+ " (ASP " + displayratio + ")");
		mxGraph graph = new mxGraph() {
			@Override
			public Object resizeCell(Object cell, mxRectangle bounds) {
				var r = bounds.getWidth() / bounds.getHeight();
				logger.debug("Try to scale to " + bounds.getWidth() + "," + bounds.getHeight() + " = " + r);

				if (r < 1) {
					var targetWidth = bounds.getHeight() * ratio;
					bounds.setWidth(targetWidth);
				} else {
					var targetHeight = bounds.getWidth() / ratio;
					bounds.setHeight(targetHeight);
				}
				r = bounds.getWidth() / bounds.getHeight();
				logger.debug("  changed to " + bounds.getWidth() + "x" + bounds.getHeight() + " ASR " + r);
				logger.debug("  xpos " + bounds.getX() + " to " + (bounds.getX() + bounds.getWidth()) + " remains "
						+ (w - bounds.getX() - bounds.getWidth()));
				logger.debug("  ypos " + bounds.getY() + " to " + (bounds.getY() + bounds.getHeight()) + " remains "
						+ (h - bounds.getY() - bounds.getHeight()));

				logger.debug(
						"  Originalimage " + bounds.getX() * 5 + " to " + (bounds.getX() + bounds.getWidth()) * 5
								+ " remains " + 5 * (w - bounds.getX() - bounds.getWidth()));
				logger.debug(
						"  Originalimage " + bounds.getY() * 5 + " to " + (bounds.getY() + bounds.getHeight()) * 5
								+ " remains " + 5 * (h - bounds.getY() - bounds.getHeight()));

				var ws = bounds.getWidth();
				var hs = bounds.getHeight();

				var wr = bounds.getWidth() * 5;
				var hr = bounds.getHeight() * 5;

				var arS = ws / hs;
				var arR = wr / hr;
				logger.debug("  AR displayed " + arS + " (" + ws + "x" + hs + "), AR real " + arR + " (" + wr + "x"
						+ hr + ")");

				command = "-quality 100 -crop " + wr + "x" + hr	+ "+" + 5 * bounds.getX() + "+" + 5 * bounds.getY() + " -geometry 1920";

				return super.resizeCell(cell, bounds);
			}

		};

		var x = 1920 / 4;
		var y = 1080 / 4;
		var cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "Process", 0, 0, x, y, "process");
		cell.setStyle("fillOpacity=90;opacity=90;");

		mxGraphComponent graphComponent = new mxGraphComponent(graph) {
			private static final long serialVersionUID = -7004590790552267843L;
			
			@Override
			public mxInteractiveCanvas createCanvas() {
				return new mxInteractiveCanvas() {

					@Override
					public Object drawCell(mxCellState state) {
						var style = state.getStyle();
						var shape = getShape(style);

						if (g != null && shape != null) {
							// Creates a temporary graphics instance for drawing this shape
							var opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY, 50);
							var previousGraphics = g;
							g = createTemporaryGraphics(style, opacity, state);

							// Paints the shape and restores the graphics object
							shape.paintShape(this, state);
							g.dispose();
							g = previousGraphics;
						}

						return shape;
					}
				};
			}
		};
		graphComponent.setConnectable(false); // Inhibit edge creation in the graph.
		graph.addListener(mxEvent.CELLS_MOVED, (sender, evt) -> {

			var bounds = graph.getModel().getGeometry(cell);

			var wr = bounds.getWidth() * 5;
			var hr = bounds.getHeight() * 5;

			command = "-quality 100 -crop " + wr + "x" + hr + "+" + 5 * bounds.getX() + "+" + 5 * bounds.getY() + " -geometry 1920";
		});

		graph.setCellsEditable(false); // Want to edit the value of a cell in the graph?
		graph.setCellsMovable(true); // Moving cells in the graph. Note that an edge is also a cell.
		graph.setCellsResizable(true); // Inhibit cell re-sizing.

		var ok = new JButton("OK");
		var cancel = new JButton("Cancel");
		ok.addActionListener(event -> {
			try {
				mifImage.setManualStyleCommand(command);
				
				graphWrapper.getService().createManualPreview(mifImage);
				imageView.refreshFromManualSize();
			} catch (MIFException e) {
				logger.error("", e);
			}
			frame.dispose();
		});
		cancel.addActionListener(event -> frame.dispose());
		
		panel.add(graphComponent, BorderLayout.CENTER);
		
		var buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(cancel);
		panel.add(buttonBox, BorderLayout.SOUTH);
		
		var dim = new Dimension(w, h);
		graphComponent.setMinimumSize(dim);
		graphComponent.setMaximumSize(dim);
		graphComponent.setPreferredSize(dim);
		graphComponent.setBackgroundImage(new ImageIcon(resizedImg));
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}
}

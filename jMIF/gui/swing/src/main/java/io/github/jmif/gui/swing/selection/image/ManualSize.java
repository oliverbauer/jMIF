package io.github.jmif.gui.swing.selection.image;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.shape.mxIShape;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.view.mxInteractiveCanvas;
import com.mxgraph.util.mxConstants;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxRectangle;
import com.mxgraph.util.mxUtils;
import com.mxgraph.view.mxCellState;
import com.mxgraph.view.mxGraph;

import io.github.jmif.Service;
import io.github.jmif.entities.MIFImage;

public class ManualSize {
	private static final Logger logger = LoggerFactory.getLogger(ManualSize.class);
	
	private String command = null;
	
	public void showFrame(MIFImage mifImage, ImageView imageView) {
		JFrame frame = new JFrame();
		JPanel panel = new JPanel(new BorderLayout());

		ImageIcon ic = new ImageIcon(mifImage.getFile());

		int w = ic.getIconWidth() / 5;
		int h = ic.getIconHeight() / 5;
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(ic.getImage(), 0, 0, w, h, null);
		g2.dispose();

		double ratio = 1920d / 1080d;
		double displayratio = (double) w / (double) h;

		String input = ic.getIconWidth() + "x" + ic.getIconHeight();
		double inputRadio = (double) ic.getIconWidth() / (double) ic.getIconHeight();
		String output = w + "x" + h;

		logger.info("Ratio " + ratio + " 1920x1080 scaled from " + input + " (ASP " + inputRadio + ") to " + output
				+ " (ASP " + displayratio + ")");
		mxGraph graph = new mxGraph() {
			@Override
			public Object resizeCell(Object cell, mxRectangle bounds) {
				double r = bounds.getWidth() / bounds.getHeight();
				logger.debug("Try to scale to " + bounds.getWidth() + "," + bounds.getHeight() + " = " + r);

				if (r < 1) {
					double targetWidth = bounds.getHeight() * ratio;
					bounds.setWidth(targetWidth);
				} else {
					double targetHeight = bounds.getWidth() / ratio;
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

				double ws = bounds.getWidth();
				double hs = bounds.getHeight();

				double wr = bounds.getWidth() * 5;
				double hr = bounds.getHeight() * 5;

				double arS = ws / hs;
				double arR = wr / hr;
				logger.debug("  AR displayed " + arS + " (" + ws + "x" + hs + "), AR real " + arR + " (" + wr + "x"
						+ hr + ")");

				command = "-quality 100 -crop " + wr + "x" + hr	+ "+" + 5 * bounds.getX() + "+" + 5 * bounds.getY() + " -geometry 1920";

				return super.resizeCell(cell, bounds);
			}

		};

		int x = 1920 / 4;
		int y = 1080 / 4;
		mxCell cell = (mxCell) graph.insertVertex(graph.getDefaultParent(), null, "Process", 0, 0, x, y, "process");
		cell.setStyle("fillOpacity=90;opacity=90;");

		mxGraphComponent graphComponent = new mxGraphComponent(graph) {
			private static final long serialVersionUID = -7004590790552267843L;
			
			@Override
			public mxInteractiveCanvas createCanvas() {
				return new mxInteractiveCanvas() {

					@Override
					public Object drawCell(mxCellState state) {
						Map<String, Object> style = state.getStyle();
						mxIShape shape = getShape(style);

						if (g != null && shape != null) {
							// Creates a temporary graphics instance for drawing this shape
							float opacity = mxUtils.getFloat(style, mxConstants.STYLE_OPACITY, 50);
							Graphics2D previousGraphics = g;
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

			mxGeometry bounds = graph.getModel().getGeometry(cell);

			double wr = bounds.getWidth() * 5;
			double hr = bounds.getHeight() * 5;

			command = "-quality 100 -crop " + wr + "x" + hr + "+" + 5 * bounds.getX() + "+" + 5 * bounds.getY() + " -geometry 1920";
		});

		graph.setCellsEditable(false); // Want to edit the value of a cell in the graph?
		graph.setCellsMovable(true); // Moving cells in the graph. Note that an edge is also a cell.
		graph.setCellsResizable(true); // Inhibit cell re-sizing.

		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		ok.addActionListener(event -> {
			mifImage.setManualStyleCommand(command);
			// TODO Service
			new Service().createManualPreview(mifImage);
			imageView.refreshFromManualSize();
			frame.dispose();
		});
		cancel.addActionListener(event -> frame.dispose());
		
		panel.add(graphComponent, BorderLayout.CENTER);
		
		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(Box.createHorizontalGlue());
		buttonBox.add(ok);
		buttonBox.add(cancel);
		panel.add(buttonBox, BorderLayout.SOUTH);
		
		Dimension dim = new Dimension(w, h);
		graphComponent.setMinimumSize(dim);
		graphComponent.setMaximumSize(dim);
		graphComponent.setPreferredSize(dim);
		graphComponent.setBackgroundImage(new ImageIcon(resizedImg));
		frame.setContentPane(panel);
		frame.pack();
		frame.setVisible(true);
	}
}

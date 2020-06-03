package io.github.jmif.gui.swing.logger;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import ch.qos.logback.classic.Level;
import io.github.jmif.config.Configuration;

public class LogView {
	private Box panel;
	private DefaultTableModel model;
	
	@SuppressWarnings("serial")
	public LogView(JFrame frame) {
		LogAppender.mifLogView = this;
		panel = Box.createHorizontalBox();
		panel.setBackground(Configuration.bgColor);
		
		String[] columnNames = {"Date", "Thread", "Level", "Logger", "Message"};
		
		JTable table = new JTable();
		
		model= new DefaultTableModel();
		model.setColumnIdentifiers(columnNames);
		table.setModel(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		TableColumnModel colModel=table.getColumnModel();
		colModel.getColumn(0).setPreferredWidth(200);
		colModel.getColumn(1).setPreferredWidth(150);    
		colModel.getColumn(2).setPreferredWidth(50);
		colModel.getColumn(3).setPreferredWidth(200);
		colModel.getColumn(4).setPreferredWidth(1000);
	    table.addComponentListener(new ComponentAdapter() {
	    	@Override
	        public void componentResized(ComponentEvent e) {
	            int lastIndex = table.getRowCount()-1;
	            table.changeSelection(lastIndex, 0,false,false);
	        }
	    });
	    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
	        @SuppressWarnings("unchecked")
			@Override
	        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	            final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	            
	            Vector<Object> object = model.getDataVector().get(row);
	            
	            Level level = (Level)object.get(2);
	            if (level == Level.INFO) {
	            	c.setBackground(Color.WHITE);
	            	c.setForeground(Color.BLUE);
	            } else if (level == Level.DEBUG) {
	            	c.setBackground(Color.LIGHT_GRAY);
	            	c.setForeground(Color.BLACK);
	            } else {
	            	c.setBackground(Color.BLACK);
	            	c.setForeground(Color.RED);
	            }
	            
	            return c;
	        }
	    });
	    
	    
		JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		panel.add(scrollPane);
		
		
		Dimension dim = new Dimension(5200, 150);
		scrollPane.setPreferredSize(dim);
		scrollPane.setMaximumSize(dim);
//		scrollPane.setMinimumSize(dim);
		
		if (Configuration.useBorders) {
			panel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
		}
	}
	
	public void newMessage(long timestamp, String thread, Level level, String logger, String message) {
		String date = new SimpleDateFormat("dd MM yyyy HH:mm:ss.SSS").format(new Date(timestamp));
		Object[] row = {date, thread, level, logger.substring(logger.lastIndexOf('.')+1), message};
		model.addRow(row);
	}
	
	public Box getPanel() {
		return this.panel;
	}
}

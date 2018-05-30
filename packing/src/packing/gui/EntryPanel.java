
package packing.gui;


// Packing imports
import packing.data.*;


// Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import java.awt.Insets;


public class EntryPanel
        extends JPanel {
    // The parent container.
    final private Container container;
    
    // The rectangle to be shown.
    final private Rectangle rec;
    
    // The size of the total dataset.
    final private int dataWidth;
    final private int dataHeight;
    
    
    /* 
     * @param entry the entry to represent.
     * @param container the parent container.
     * @param fieldWidth the maximum width of the dataset.
     * @param fieldHeight the maximum height of the dataset.
     */
    public EntryPanel(CompareEntry entry, Container container,
            int dataWidth, int dataHeight) {
        // Create JPanel with null layout.
        super(null);
        
        // Obtain the rectangle.
        rec = entry.getRec();
        
        // Set container.
        this.container = container;
        
        // Set the data size.
        this.dataWidth = dataWidth;
        this.dataHeight = dataHeight;
        
        // Set the default background.
        setBackground(Color.GREEN);
    }
    
    public void update() {
        // Calculate the available width.
        Insets in = container.getInsets();
        int containerWidth = container.getWidth() - in.left - in.right;
        int containerHeight = container.getHeight() - in.top - in.bottom;
        
        // Set the size and location of the rectangle.
        setSize((int) (((long) containerWidth) * ((long) rec.width) / ((long) dataWidth)),
                (int) (((long) containerHeight) * ((long) rec.height) / ((long) dataHeight)));
        setLocation((int) (((long) containerWidth) * ((long) rec.x) / ((long) dataWidth)),
                (int) (((long) containerHeight) - ((long) getHeight())
                            - ((long) containerHeight) * ((long) rec.y) / ((long) dataHeight)));
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        int barWidth = (int) (0.1 * getWidth() + 0.5);
        int barHeight = (int) (0.1 * getHeight() + 0.5);
        
        // Draw gray bars around the panel.
        g2d.setPaint(new Color(150, 150, 150));
        g2d.fillRect(0, 0, getWidth(), barHeight);
        g2d.fillRect(0, 0, barWidth, getHeight());
        g2d.fillRect(getWidth() - barWidth, 0, getWidth(), getHeight());
        g2d.fillRect(0, getHeight() - barHeight, getWidth(), getHeight());
        
        // Paint a single lined rectangle as outline of the panel.
        g2d.setPaint(Color.BLACK);
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        
        // Dispose the graphics object to prevent any further changes.
        g2d.dispose();
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(getWidth(), getHeight());
    }
    
}
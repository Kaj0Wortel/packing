
// Java imports
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import java.awt.Insets;


public class EntryPanel
        extends JPanel {
    /* 
     * @param entry the entry to represent.
     * @param container the parent container.
     * @param fieldWidth the maximum width of the dataset.
     * @param fieldHeight the maximum height of the dataset.
     */
    public EntryPanel(Dataset.Entry entry, Container container,
                      int fieldWidth, int fieldHeight) {
        // Set null layout.
        super(null);
        
        // Obtain the rectangle.
        Rectangle rec = entry.getRec();
        
        // Set the default background.
        setBackground(Color.GREEN);
        
        // Calculate the available width.
        Insets in = container.getInsets();
        int containerWidth = container.getWidth() - in.left - in.right;
        int containerHeight = container.getHeight() - in.top - in.bottom;
        
        // Set the size and location of the rectangle.
        setSize(containerWidth  * rec.width  / fieldWidth,
                containerHeight * rec.height / fieldHeight);
        //System.err.println(rec.x);
        //System.out.println(rec.y);
        setLocation(containerWidth * rec.x  / fieldWidth,
                    containerHeight - getHeight()
                            - containerHeight * rec.y  / fieldHeight);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        
        int barWidth = (int) (0.1 * getWidth());
        int barHeight = (int) (0.1 * getHeight());
        
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
    
}
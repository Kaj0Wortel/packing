
package packing.gui;


// Packing imports
import packing.data.Dataset;
import packing.data.TestDataset;


// Java imports
import java.awt.Color;
import java.awt.Rectangle;

import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;


/* 
 * GUI interface showing a visualisation of a {@link packing.data.Dataset}.
 */
public class ShowDataset
        extends JFrame {
    
    public ShowDataset(Dataset data, int width, int height) {
        super("Show solution");
        setLayout(null);
        setSize(1000, 1000);
        setLocation(10, 10);
        
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        
        // Create all entry panels.
        Iterator<Dataset.Entry> it = data.iterator();
        while (it.hasNext()) {
            new EntryPanel(it.next(), this, width, height);
        }
        
        // Set the background.
        getContentPane().setBackground(Color.RED);
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            repaint();
        });
    }
    
    
    // tmp
    public static void main(String[] args) {
        Dataset dataset = new TestDataset(false, -1, 5);
        
        dataset.add(new Rectangle(0, 0, 100, 100));
        dataset.add(new Rectangle(100, 0, 100, 50));
        dataset.add(new Rectangle(150, 50, 50, 50));
        dataset.add(new Rectangle(100, 75, 50, 25));
        dataset.add(new Rectangle(100, 50, 25, 25));
        
        new ShowDataset(dataset, 200, 100);
    }
    
    
}

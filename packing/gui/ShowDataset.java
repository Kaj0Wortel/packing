
package packing.gui;


// Packing imports
import packing.data.Dataset;


// Java imports
import java.awt.Color;
import java.awt.Rectangle;

import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;


// tmp
import java.util.Set;
import java.util.HashSet;


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
        Dataset dataset = new Dataset(false, -1) {
            Set<Dataset.Entry> set = new HashSet<>();
            
            @Override
            public void add(Rectangle rec) {
                set.add(new Entry(rec));
            }
            
            @Override
            public Object getEntries() {
                return null;
            }
            
            @Override
            public Iterator<Dataset.Entry> iterator() {
                return set.iterator();
            }
        };
        
        dataset.add(new Rectangle(0, 0, 100, 100));
        dataset.add(new Rectangle(100, 0, 100, 50));
        dataset.add(new Rectangle(150, 50, 50, 50));
        dataset.add(new Rectangle(100, 75, 50, 25));
        dataset.add(new Rectangle(100, 50, 25, 25));
        
        new ShowDataset(dataset, 200, 100);
    }
    
    
}


package packing.gui;


// Packing imports
import packing.data.*;
import packing.tools.MultiTool;


// Java imports
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/* 
 * GUI interface showing a visualisation of a {@link packing.data.Dataset}.
 */
public class ShowDataset
        extends JFrame {
    
    final private JScrollPane scrollPane;
    final private JPanel contentPane;
    
    // Static counter for the total number of alive frames.
    private static int counter = 0;
    
    public ShowDataset(Dataset data) {
        super("Show solution");
        
        // Increase counter.
        counter++;
        
        //setLayout(null);
        
        contentPane = new JPanel(null) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(super.getWidth(), super.getHeight());
            }
            
            @Override
            public void setBounds(int x, int y, int width, int height) {
                super.setBounds(x, y, data.getWidth() * 10, data.getHeight() * 10);
            }
        };
        
        contentPane.setSize(data.getWidth() * 5, data.getHeight() * 5);
        //contentPane.setSize(1000, 1000);
        scrollPane = new JScrollPane(contentPane);
        add(scrollPane);
        //add(contentPane);
        
        setSize(1000, 1000);
        setLocation(10, 10);
        
        // Create all entry panels.
        Iterator<Dataset.Entry> it = data.iterator();
        while (it.hasNext()) {
            contentPane.add(new EntryPanel(it.next(), contentPane,
                            data.getWidth(), data.getHeight()));
        }
        
        // Set the background.
        contentPane.setBackground(Color.RED);
        scrollPane.getViewport().setBackground(Color.GREEN);
        //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (--counter == 0) {
                    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    
                } else {
                    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                }
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                update();
            }
        });
        
        setVisible(true);
        
        SwingUtilities.invokeLater(() -> {
            repaint();
        });
    }
    
    private Thread updateThread = null;
    private boolean waiting = false;
    
    public void update() {
        synchronized(this) {
            if (updateThread == null) {
                updateThread = createUpdateThread();
                updateThread.start();
                
            } else if (!waiting) {
                waiting = true;
                while (updateThread == null) {
                    MultiTool.sleepThread(10);
                };
                waiting = false;
                
            } else {
                updateThread.interrupt();
                updateThread = null;
                return;
            }
            
            updateThread = createUpdateThread();
            updateThread.start();
        }
        System.out.println(contentPane.getSize());
    }
    
    public Thread createUpdateThread() {
        return new Thread() {
            @Override
            public void run() {
                if (contentPane != null) {
                    Insets in = getInsets();
                    scrollPane.setSize(
                            getWidth() - in.left - in.right,
                            getHeight() - in.top - in.bottom);
                }
                
                for (Component comp : contentPane.getComponents()) {
                    if (comp instanceof EntryPanel) {
                        EntryPanel panel = (EntryPanel) comp;
                        panel.update();
                    }
                }
                
                synchronized(ShowDataset.this) {
                    updateThread = null;
                }
            }
        };
    }
    
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        update();
    }
    
    
    // tmp
    public static void main(String[] args) {
        Dataset dataset = new TestDataset(-1, false, 5);
        
        dataset.add(new Rectangle(0, 0, 100, 100));
        dataset.add(new Rectangle(100, 0, 100, 50));
        dataset.add(new Rectangle(150, 50, 50, 50));
        dataset.add(new Rectangle(100, 75, 50, 25));
        dataset.add(new Rectangle(100, 50, 25, 25));
        dataset.setSize(200, 100);
        
        new ShowDataset(dataset);
    }
    
    
}
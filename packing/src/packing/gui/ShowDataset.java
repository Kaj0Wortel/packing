
package packing.gui;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.tools.MultiTool;


//##########
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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;


/**
 * GUI interface showing a visualisation of a {@link packing.data.Dataset}.
 */
public class ShowDataset
        extends JFrame {
    final private static int ZOOM_BUTTON_HEIGHT = 40;
    
    final private JScrollPane scrollPane;
    private AntiLayoutManagerJPanel contentPane;
    final private JButton zoomInButton;
    final private JButton zoomOutButton;
    
    private Thread updateThread = null;
    private boolean waiting = false;
    
    // Static counter for the total number of alive frames.
    private static int counter = 0;
    
    /**
     * Class for ignoring annoying layout manager calls from the jscrollpane.
     */
    private class AntiLayoutManagerJPanel extends JPanel {
        final private Dataset data;
        AntiLayoutManagerJPanel(Dataset data) {
            super(null);
            this.data = data;

            if (data != null) {
                setSize(data.getWidth() * 10, data.getHeight() * 10);
                overrideSize(data.getWidth() * 10, data.getHeight() * 10);

                // Create all entry panels.
                for (CompareEntry entry : data) {
                    add(new EntryPanel(entry, this,
                            data.getWidth(), data.getHeight()));
                }
            }

            // Set the background.
            setBackground(Color.RED);
        }
        
        @Override
        public Dimension getPreferredSize() {
            return new Dimension(super.getWidth(), super.getHeight());
        }
        
        // Changed setBounds method to only accept changes in location.
        // Note that this is NOT GOOD PROGRAMMING PRACTICE!
        // Just to shut the layout managers up.
        @Override
        public void setBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, getWidth(), getHeight());
        }
        
        public void overrideBounds(int x, int y, int width, int height) {
            super.setBounds(x, y, width, height);
        }
        
        public void overrideSize(int width, int height) {
            super.setBounds(getX(), getY(), width, height);
        }
        
        public void overrideLocation(int x, int y) {
            super.setBounds(x, y, getWidth(), getHeight());
        }
    }
    
    
    public ShowDataset(Dataset data) {
        super("Show solution");
        
        // Increase counter.
        counter++;
        
        // Set null layout.
        setLayout(null);

        // Setup contentpane and scroll pane.
        contentPane = new AntiLayoutManagerJPanel(data);

        scrollPane = new JScrollPane(contentPane);
        add(scrollPane);

        //scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        //scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(28);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(28);
        
        // Setup the zoom buttons
        zoomInButton = new JButton("+");
        zoomOutButton = new JButton("-");
        
        zoomInButton.addActionListener((e) -> {
            AntiLayoutManagerJPanel panel = ShowDataset.this.contentPane;
            if (panel != null) {
                int scalingFactor = panel.getWidth() / data.getWidth();
                scalingFactor = Math.max((int) (scalingFactor * 1.25), scalingFactor + 1);
                panel.overrideSize(scalingFactor * data.getWidth(),
                        scalingFactor * data.getHeight());
                update();
            }
        });
        
        zoomOutButton.addActionListener((e) -> {
            AntiLayoutManagerJPanel panel = ShowDataset.this.contentPane;
            if (panel != null) {
                int scalingFactor = (int) (panel.getWidth() / data.getWidth() * 0.8);
                if (scalingFactor > 0) {
                    panel.overrideSize(scalingFactor * data.getWidth(),
                            scalingFactor * data.getHeight());
                    update();
                }
            }
        });
        
        add(zoomInButton);
        add(zoomOutButton);
        
        setSize(1000, 1000);
        setLocation(10, 10);
        
        // Set the background.
        scrollPane.getViewport().setBackground(Color.BLUE);
        
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
            update();
            repaint();
        });
    }

    public void setDataset(Dataset data) {
        contentPane = new AntiLayoutManagerJPanel(data);
        scrollPane.setViewportView(contentPane);

        SwingUtilities.invokeLater(() -> {
            update();
            repaint();
        });
    }
    
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
    }
    
    public Thread createUpdateThread() {
        return new Thread() {
            @Override
            public void run() {
                if (zoomInButton == null || zoomOutButton == null) return;

                zoomInButton.setSize(getWidth() / 2, ZOOM_BUTTON_HEIGHT);
                zoomOutButton.setSize(getWidth() / 2, ZOOM_BUTTON_HEIGHT);
                zoomInButton.setLocation(0, 0);
                zoomOutButton.setLocation(getWidth() / 2, 0);
                
                if (contentPane != null) {
                    Insets in = getInsets();
                    scrollPane.setSize(
                            getWidth() - in.left - in.right,
                            getHeight() - in.top - in.bottom
                                    - ZOOM_BUTTON_HEIGHT);
                    scrollPane.setLocation(0, ZOOM_BUTTON_HEIGHT);
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
        Dataset dataset = new Dataset(-1, false, 5);
        
        dataset.add(new Rectangle(0, 0, 100, 100));
        dataset.add(new Rectangle(100, 0, 100, 50));
        dataset.add(new Rectangle(150, 50, 50, 50));
        dataset.add(new Rectangle(100, 75, 50, 25));
        dataset.add(new Rectangle(100, 50, 25, 25));
        dataset.setSize(200, 100);
        
        new ShowDataset(dataset);
    }
    
    
}
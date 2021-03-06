
package packing.io;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;


//##########
// Java imports
import java.awt.Rectangle;

import java.io.IOException;
import java.io.OutputStream;

import java.nio.charset.Charset;


/**
 * Writer used to output text to a stream. Also formats datasets to a
 * correct output format.
 */
public class OutputWriter {
    // The used charset.
    final private Charset CHARSET = Charset.forName("UTF-8");
    
    // The used stream.
    final private OutputStream stream;
    
    // Whether to use rotation or not.
    private boolean useRotation;
    
    
    /**-------------------------------------------------------------------------
     * Constructor
     * -------------------------------------------------------------------------
     */
    /* 
     * @param stream the output stream to use.
     */
    public OutputWriter(OutputStream stream) {
        this.stream = stream;
    }
    
    /**
     * Writes all entries of a data set according the the format
     * to the output stream.
     * 
     * @param dataset the dataset of which the entries are used.
     */
    public void writeEntries(Dataset dataset) throws IOException {
        println("placement of rectangles");
        
        if (dataset == null) {
            System.err.println("Unable to process null dataset!");
            return;
        }
        
        useRotation = dataset.allowRotation();

        dataset.setOrdering(CompareEntry.SORT_ID);
        
        for (CompareEntry entry : dataset) {
            write(entry);
        }
    }
    
    /**
     * Writes a single entry to the output stream.
     */
    private void write(CompareEntry entry) throws IOException {
        StringBuilder sb = new StringBuilder();
        
        if (useRotation) {
            sb.append(entry.useRotation() ? "yes " : "no ");
        }
        
        Rectangle rec = entry.getRec();
        sb.append(rec.x + " " + rec.y);
        
        sb.append(System.getProperty("line.separator"));
        
        print(sb.toString());
    }
    
    /**
     * @param text to be printed to the output stream.
     */
    void print(String text) throws IOException {
        stream.write(text.getBytes(CHARSET));
    }
    
    /**
     * @param text line of text to be printed to the output stream.
     */
    void println(String text) throws IOException {
        print(text + System.getProperty("line.separator"));
    }
}
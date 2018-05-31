
package packing.io;


// Packing imports
import packing.data.Dataset;


//##########
// Java imports
import java.io.IOException;


/* 
 * Abstract class for reading the input files.
 */
public abstract class AbstractReader {
    final protected OutputWriter ow;
    
    protected AbstractReader(OutputWriter ow) {
        this.ow = ow;
    }
    
    /* 
     * @return the dataset from {@code file}.
     */
    public abstract Dataset readEntries();
    
    protected void outputLine(String line) {
        try {
            if (ow != null) ow.println(line);
            
        } catch (IOException e) {
            System.err.println(e);
        }
    }
    
}

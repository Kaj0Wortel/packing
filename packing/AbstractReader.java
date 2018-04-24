
package packing;


// Packing imports
import packing.data.Dataset;


// Java imports
import java.io.File;


/* 
 * Abstract class for reading the input files.
 */
public abstract class AbstractReader {
    
    public AbstractReader(File file) { }
    
    /* 
     * @return the dataset from {@code file}.
     */
    public abstract <D extends Dataset> D readEntries(D dataset);
    
}

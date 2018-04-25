
package packing.data;


// Packing imports
import packing.data.Dataset;


// Java imports
import java.io.File;


/* 
 * Abstract class for reading the input files.
 */
public abstract class AbstractReader {
    final protected File file;
    
    public AbstractReader(File file) {
        if (file == null)
            throw new NullPointerException("File was null!");
        this.file = file;
    }
    
    /* 
     * @return the dataset from {@code file}.
     */
    public abstract <D extends Dataset> D readEntries(Class<? extends D> type);
    
}

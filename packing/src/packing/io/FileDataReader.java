
package packing.io;


// Packing imports
import packing.data.*;


// Java imports
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;


/* 
 * Reads data from a file and provides a Dataset for this data.
 * Also outputs the 
 */
public class FileDataReader
        extends AbstractReader {
    final protected File file;
    
    /* 
     * @param fileName the name of the input file.
     * @param file the input file.
     * @param ow the output writer.
     */
    public FileDataReader(String fileName, OutputWriter ow) {
        this(new File(fileName), ow);
    }
    
    public FileDataReader(File file, OutputWriter ow) {
        super(ow);
        
        if (file == null) throw new NullPointerException("File was null!");
        this.file = file;
    }
    
    @Override
    public Dataset readEntries() {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> data = new ArrayList<String>();
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    data.add(line);
                    super.outputLine(line);
                }
            }
            
            return DatasetFactory.process(data);
            
        } catch (IOException e) {
            System.err.println(e);
        }
        
        return null;
    }
    
}
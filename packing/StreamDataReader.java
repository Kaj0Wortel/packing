
// Java imports
import java.io.InputStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/* 
 * Reads data from a stream and provides a Dataset for this data.
 */
public class StreamDataReader
        extends AbstractReader {
    final private InputStream stream;
    
    
    public StreamDataReader(InputStream stream, OutputWriter ow) {
        super(ow);
        this.stream = stream;
    }
    
    
    @Override
    public Dataset readEntries() {
        Scanner sc = new Scanner(stream);
        
        List<String> data = new ArrayList<String>();
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            data.add(line);
            super.outputLine(line);
        }
        
        return DatasetFactory.process(data);
    }
    
}
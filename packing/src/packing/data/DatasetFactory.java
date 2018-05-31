
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;

import java.util.List;


/**
 * Factory class for creating the initial dataset.
 */
public class DatasetFactory {
    
    
    public static Dataset process(List<String> input) {
        int height;
        boolean rotation;
        int numRect;
        
        // Determine the height
        String line = input.get(0);
        if (line.endsWith("free")) {
            height = -1;
            
        } else {
            int loc = line.length();
            while (line.charAt(--loc) != ' ') { }
            
            String number = line.substring(loc + 1, line.length());
            
            try {
                height = Integer.parseInt(number);
                
            } catch (NumberFormatException e) {
                //throw new IllegalStateException("Illegal input");
                return null;
            }
        }
        
        // Determine whether rotations are allowed or not.
        line = input.get(1);
        rotation = line.endsWith("yes");
        
        // Determine the number of rectangles.
        line = input.get(2);
        int loc = line.length();
        while (line.charAt(--loc) != ' ') { }
        
        String number = line.substring(loc + 1, line.length());
        
        try {
            numRect = Integer.parseInt(number);
            
        } catch (NumberFormatException e) {
            //throw new IllegalStateException("Illegal input: " + line);
            return null;
        }
        
        // Create the dataset.
        Dataset dataset = createDataset(height, rotation, numRect, input);
        
        // Fill the dataset.
        for (int i = 3; i < input.size(); i++) {
            line = input.get(i);
            
            if (!line.equals("")) {
                String[] coords = line.split(" ");
                
                try {
                    int recWidth  = Integer.parseInt(coords[0]);
                    int recHeight = Integer.parseInt(coords[1]);
                    dataset.add(new Rectangle(recWidth, recHeight));
                    
                } catch (NumberFormatException e) {
                    System.err.println("File does not have the correct format");
                    return null;
                }
            }
        }
        
        return dataset;
    }
    
    
    private static Dataset createDataset(int height, boolean rotation,
                                         int numRect, List<String> input) {
        if (true) {// todo
            return new Dataset(height, rotation, numRect);
        }
        
        return null;
    }
    
    
}
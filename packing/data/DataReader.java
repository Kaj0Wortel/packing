
package packing.data;


// Tools imports
import tools.log.Logger;


// Packing imports
import packing.data.Dataset;


// Java imports
import java.awt.Rectangle;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;


public class DataReader
        extends AbstractReader {
    
    public DataReader(File file) {
        super(file);
    }
    
    @Override
    public <D extends Dataset> D readEntries(Class<? extends D> type) {
        D dataset = null;
        
        if (!file.exists())
            throw new IllegalStateException("The file \"" + file.toPath()
                                                + "\" does not exist");
        if (type == null)
            throw new NullPointerException("Dataset was null!");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            int height;
            boolean rotation;
            int numRect;
            
            // Determine the height
            String line = br.readLine();
            if (line.endsWith("free")) {
                height = -1;
                
            } else {
                int i = line.length();
                while (line.charAt(--i) != ' ') { }
                
                String number = line.substring(i + 1, line.length());
                
                try {
                    height = Integer.parseInt(number);
                    
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Illegal input");
                }
            }
            
            // Determine whether rotations are allowed or not.
            line = br.readLine();
            rotation = line.endsWith("yes");
            
            // Determine the number of rectangles.
            line = br.readLine();
            int i = line.length();
            while (line.charAt(--i) != ' ') { }
            
            String number = line.substring(i + 1, line.length());
            
            try {
                numRect = Integer.parseInt(number);
                
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Illegal input: " + line);
            }
            
            // Create the dataset.
            try {
                dataset = type.getConstructor(boolean.class, int.class, int.class)
                    .newInstance(rotation, height, numRect);
                
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException
                    ("Constructor missing for " + type.getName());
                
            } catch (InstantiationException e) {
                throw new IllegalStateException
                    ("No instance could be created of " + type.getName());
                
            } catch (IllegalAccessException e) {
                throw new IllegalStateException
                    ("The constructor is not public!");
                
            } catch (InvocationTargetException e) {
                throw new IllegalStateException
                    ("An exception occured in the constructor of "
                         + type.getName() + ": " + e.getMessage());
            }
            
            // Fill the dataset.
            int counter = 0;
            while ((line = br.readLine()) != null) {
                String[] coords = line.split(" ");
                
                try {
                    int recWidth  = Integer.parseInt(coords[0]);
                    int recHeight = Integer.parseInt(coords[1]);
                    dataset.add(new Rectangle(recWidth, recHeight));
                    
                } catch (NumberFormatException e) {
                    throw new IllegalStateException("Illegal input: " + line);
                }
            }
            
        } catch (IOException e) {
            Logger.write(e);
        }
        
        return dataset;
    }
    
    // tmp
    public static void main(String[] args) {
        String fs = System.getProperty("file.separator");
        Dataset dataset = new DataReader
            (new File(System.getProperty("user.dir") + fs + "testcases" + fs
                          + "03_01_h20_rn.txt")).readEntries(TestDataset.class);
        System.out.println(dataset);
    }
    
}

package packing;


// Packing imports
import packing.data.AbstractReader;
import packing.data.Dataset;
import packing.data.FileDataReader;
import packing.data.StreamDataReader;
import packing.data.OutputWriter;

import packing.generator.Generator;


// Java imports
import java.io.File;
import java.io.IOException;

import java.util.Timer;
import java.util.TimerTask;


/* 
 * The mainclass of the project.
 */
public class Packing {
    // The file separator for the current OS.
    final public static String fs = System.getProperty("file.separator");
    
    // The test file
    final public static String testFile
        = System.getProperty("user.dir") + fs + "testcases" + fs
        + "03_01_h20_rn.txt";
    
    final public static File[] testFiles
        = new File(System.getProperty("user.dir") + fs).listFiles();
    
    // The timer to keep track of the time limit.
    private Timer timer;
    
    // The generator used for calculating the solution.
    private Generator gen;
    
    
    /* 
     * Runs the application.
     */
    public void run() {
        // Start the timer.
        timer = new Timer();
        timer.schedule
            (new TimerTask() {
            @Override
            public void run() {
                Generator gen = getGenerator();
                if (gen != null) gen.interrupt();
            }
        }, 4900L);
        
        // Create the output writer.
        OutputWriter ow = new OutputWriter(System.out);
        
        // Read input.
        // Read data from file.
        AbstractReader reader = new FileDataReader(testFile, ow);
        
        // Read data from system input.
        //AbstractReader reader = new StreamDataReader(System.in);
        
        // Also possible for reading from file.
        //AbstractReader reader = new StreamDataReader(new FileInputStream(testFile));
        
        Dataset input = reader.readEntries();
        
        // Generate solution.
        gen = input.getGenerator();
        Dataset result = gen.generate(input);
        
        // Output solution.
        try {
            ow.writeEntries(result);
            
        } catch (IOException e) {
            System.err.println("an exception occured");
        }
    }
    
    /* 
     * @return the generator
     */
    public Generator getGenerator() {
        return gen;
    }
    
    public static void main(String[] args) {
        new Packing().run();
    }
}
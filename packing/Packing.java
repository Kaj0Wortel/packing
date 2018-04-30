
// Java imports
import java.io.File;
import java.io.IOException;

import java.util.Timer;
import java.util.TimerTask;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;


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
     * 
     * @param fileName the used file name of the data file.
     */
    public void run(String inputFile, String outputFile) {
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
        OutputWriter ow = null;
        if (outputFile == null) {
            ow = new OutputWriter(System.out);
            
        } else {
            try {
                ow = new OutputWriter(new FileOutputStream(outputFile));
                
            } catch (FileNotFoundException e) {
                System.err.println(e);
                System.exit(0);
            }
        }
        
        // Read the input.
        AbstractReader reader;
        if (inputFile == null) {
            reader = new StreamDataReader(System.in, ow);
            
        } else {
            reader = new FileDataReader(inputFile, ow);
        }
        
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
        String in = null;
        String out = null;
        
        if (args != null) {
            if (args.length >= 1) in = args[0];
            if (args.length >= 2) out = args[0];
        }
        
        new Packing().run(in, out);
    }
}

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
public class PackingSolver {
    // The file separator for the current OS.
    final public static String FS = System.getProperty("file.separator");
    
    
    // The test file
    final public static String testFile
        = System.getProperty("user.dir") + FS + "testcases" + FS
            //+ "03_01_h20_rn.txt";
            //+ "test_case_9_10000.txt";
            + "25_01_h19_ry.txt";
    
    final public static File[] testFiles
        = new File(System.getProperty("user.dir") + FS).listFiles();
    
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
        long startTime = System.currentTimeMillis();
        
        // Start the timer.
        timer = new Timer(true);
        timer.schedule
            (new TimerTask() {
            @Override
            public void run() {
                Generator gen = getGenerator();
                if (gen != null) gen.interrupt();
            }
        }, 300000L - 250L); // 5*60*1000 = 300 000, use 250 ms space
        
        // Create the output writer.
        OutputWriter ow = null;
        if (outputFile == null) {
            ow = new OutputWriter(System.out);
            
        } else {
            try {
                ow = new OutputWriter(new FileOutputStream(outputFile));
                
            } catch (FileNotFoundException e) {
                System.err.println(e);
            }
        }
        
        // Read the input.
        AbstractReader reader = null;
        if (inputFile == null) {
            reader = new StreamDataReader(System.in, ow);
            
        } else {
            reader = new FileDataReader(inputFile, ow);
        }
        
        Dataset input = reader.readEntries();
        if (input == null) return;
        
        // Generate solution.
        gen = new WideToHighBoundingBoxGenerator(new GreedyPackerFactory());
        Dataset result = gen.generate(input);
        
        // Output solution.
        try {
            ow.writeEntries(result);
            
        } catch (IOException e) {
            System.err.println(e);
        }
        
        // tmp
        System.err.print("Total runtime: "
                + (System.currentTimeMillis() - startTime) + " ms");
        new ShowDataset(result);
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
            if (args.length >= 2) out = args[1];
            in = testFile;
        }
        
        new PackingSolver().run(in, out);
    }
    
}

package packing;


// Package imports
import packing.data.*;
import packing.generator.*;
import packing.gui.*;
import packing.io.*;
import packing.packer.*;
import packing.tools.Logger;
import packing.tools.StreamLogger;


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
            //+ "03_02_hf_rn.txt";
            //+ "03_03_h12_ry.txt";
            //+ "03_04_hf_ry.txt";
            //+ "05_01_h7_ry.txt";
            //+ "05_02_hf_ry.txt";
            //+ "05_03_h25_rn.txt";
            //+ "05_04_hf_rn.txt";
            + "10_01_h11_rn.txt";
            //+ "10_02_h15_ry.txt";
            //+ "10_03_hf_ry.txt";
            //+ "10_04_hf_rn.txt";
            //+ "10000_01_h300_rn.txt";
            //+ "10000_02_hf_ry.txt";
            //+ "10000_03_hf_rn.txt";
            //+ "10000_04_h1315_ry.txt";
            //+ "25_01_h19_ry.txt";
            //+ "25_02_hf_ry.txt";
            //+ "25_03_hf_rn.txt";
            //+ "25_04_h74_rn.txt";
    
    final public static File[] testFiles
        = new File(System.getProperty("user.dir") + FS + "testcases")
                .listFiles();
    
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
                synchronized(PackingSolver.this) {
                    if (gen != null) {
                        gen.interrupt();

                    } else {
                        System.exit(0);
                    }
                }
            }
        }, 300000L - 2000L); // 5*60*1000 = 300 000, use 2000 ms space
        //}, 5000L); // tmp
        
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
        if (input == null) {
            timer.cancel();
            return;
        }

        // Logger.setDefaultLogger(new StreamLogger(System.err));
        
        // Generate solution.
        //according to the chart(v2)
        if (input.size() > 25) {
            if (input.isFixedHeight()) {
                gen = new FixedHeightGenerator(new GreedyPackerFactory());
            } else {
                gen = new WideToHighBoundingBoxGenerator(new GreedyPackerFactory());
            }
        } else if (input.size() > 10 && input.size() <= 25) {
            if (input.isFixedHeight()) {
                gen = new GeneticGenerator(new GreedyPackerFactory());
            } else {
                gen = new WideToHighBoundingBoxGenerator(new GreedyPackerFactory());
            }
        } else if (input.size() >= 0 && input.size() <= 10) {
            // gen = new OptimalPointGenerator(new GreedyPackerFactory());
            gen = new OptimalBoundingBoxGenerator(new OptimalPackerFactory());
        }
        
        //previous version(v1)
        /*
        if (input.isFixedHeight()) {
            if (input.size() > 1000) {
                gen = new FixedHeightGenerator(new GreedyPackerFactory());
            } else {
                gen = new GeneticGenerator(new GreedyPackerFactory());
            }
        } else {
            gen = new WideToHighBoundingBoxGenerator(new GreedyPackerFactory());
            //gen = new WideToHighBoundingBoxGenerator(new SheetPackerFactory());
        }
        */
        
        Dataset result = gen.generate(input);
        timer.cancel();
        
        // Output solution.
        try {
            ow.writeEntries(result);
            
        } catch (IOException e) {
            System.err.println(e);
        }
        
        // tmp
//        System.err.println(result);
        System.err.println("Total runtime: "
                + (System.currentTimeMillis() - startTime) + " ms");
        if (result != null) new ShowDataset(result);
        
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
        /*
        for (File file : testFiles) {
            in = file.toString();
            MultiTool.sleepThread(2000);
            System.err.println("Testfile: " + in);
            
            try {    
                new PackingSolver().run(in, out);
                
            } catch (Exception e) {
                System.err.println(e);
            }
        }/**/
    }
    
}
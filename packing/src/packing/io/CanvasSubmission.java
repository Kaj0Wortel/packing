
package packing.io;


// Packing imports
import packing.tools.MultiTool;


//##########
// Java imports
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


/**
 * Class for formatting the all current java files in the project to a
 * single list such that it can be easily uploaded to canvas.
 */
public class CanvasSubmission {
    final public static String FS = System.getProperty("file.separator");
    final public static String SOURCE
            = System.getProperty("user.dir") + FS + "src" + FS;
    final public static String DEST
            = new File(System.getProperty("user.dir"))
                    .getParentFile().getParent()
                    + FS + "_canvas_submission" + FS;
    
    
    public static void main(String[] args) {
        System.out.println("Source dir: " + SOURCE);
        System.out.println("Destination dir: " + DEST);
        new File(DEST).mkdirs();
        
        List<File[]> files = MultiTool
                .listFilesAndPathsFromRootDir(new File(SOURCE));
        
        for (File[] file : files) {
            File source = file[0];
            if (source.isDirectory()) continue;
            File dest = new File(DEST + file[0].getName());
            dest.delete();
            
            try (BufferedReader br
                    = new BufferedReader(new FileReader(source))) {
                //try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dest)))) {
                try (PrintWriter pw = new PrintWriter(
                        new BufferedWriter(new FileWriter(dest, false)))) {
                    
                    String line = br.readLine();
                    if (line == null) continue;
                    boolean hasHeader = line.startsWith("/* * * * * * *");
                    if (hasHeader) {
                        pw.println(line);
                    }
                    
                    while ((line = br.readLine()) != null) {
                        if (line.equals("//##########")) break;
                        if (hasHeader) {
                            pw.println(line);
                        }
                        if (line.endsWith("* * * */")) hasHeader = false;
                    }
                    
                    while ((line = br.readLine()) != null) {
                        pw.println(line.replaceAll("packing.tools.Cloneable", "Cloneable"));
                    }
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}

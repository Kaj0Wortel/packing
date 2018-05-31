
package packing.io;


// Packing imports
import packing.tools.MultiTool;


//##########
// Java imports
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
            try {
                if (file[0].isDirectory()) continue;
                Files.copy(file[0].toPath(),
                        new File(DEST + file[0].getName()).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}

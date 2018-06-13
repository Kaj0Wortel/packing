
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Mover {
    public static void main(String[] args) {
        String dir = System.getProperty("user.dir");
        File[] files = new File(dir).listFiles();
        for (File file : files) {
            if (file.isDirectory()) continue;
            System.out.println("processing: " + file);
            
            String text = file.toString().replaceAll(">", "geq");
            text = text.replaceAll("<", "leq");
            try {
                Files.move(file.toPath(), new File(text).toPath());
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
}

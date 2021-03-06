
package packing.generator;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;
import packing.tools.ThreadMonitor;


//##########
// Java imports
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;


/* 
 * Abstract generator class.
 */
public abstract class Generator {
    protected PackerFactory packerFactory;
    protected Dataset best = null;
    private volatile Thread genThread;

    protected static String name;

    public Generator(PackerFactory factory) {
        this.packerFactory = factory;
    }
    
    public Dataset generate(Dataset dataset) {
        genThread = Thread.currentThread();

        System.err.printf("Algorithm: %s\n", name);
        
        try {
            generateSolution(dataset);
            
        } catch (ThreadDeath e) {
            // tmp
            //System.err.println("TERMINATED BY TIME-OUT!");
            
            // Clear the interrupted flag.
            try {
                Thread.sleep(0);
            } catch (InterruptedException e2) { } 
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
            
        } finally {
            ThreadMonitor.killAll();
            return best;
        }
    }
    
    /**
     * Generates a solution for the given dataset.
     */
    public abstract void generateSolution(Dataset dataset);
    
    /**
     * %%explaination needed%%
     * 
     * @param dataset %%explaination needed%%
     * @return %%explaination needed%%
     */
    public Dataset generateUpperBound(Dataset dataset) {
        int height = dataset.getHeight();
        int width = Integer.MAX_VALUE;

        if (height == 0 && !dataset.isFixedHeight()) {
            for (CompareEntry entry : dataset) {
                height = Math.max(height, entry.getRec().height);
            }
        }

        for (Predicate<CompareEntry> predicate :
                Arrays.asList(CompareEntry.NO_ROTATION,
                        CompareEntry.LONGEST_SIDE_VERTIAL)) {
            dataset.setRotation(predicate);
            
            for (Comparator<CompareEntry> comparator :
                    Arrays.asList(CompareEntry.SORT_HEIGHT,
                            CompareEntry.SORT_AREA,
                            CompareEntry.SORT_WIDTH, 
                            CompareEntry.SORT_LONGEST_SIDE)) {
                dataset.setOrdering(comparator);
                Packer packer = packerFactory.create(width, height);
                Dataset packed = packer.pack(dataset);
                
                if (packed != null &&
                        (best == null ||
                         packed.getArea() < best.getArea())) {
                    best = packed;
                    width = packed.getWidth();
                }
            }
        }
        
        return best;
    }
    
    /**
     * Interrupts the generator when the time is up.
     * 
     * NOTE OF CAUTION:
     * The {@link Thread.stop()} method is deprecated because it is unsafe
     * since it unlocks all monitors it has locked. However, this is in
     * this case not a problem since no other threads are using any data
     * from that calculation thread, or at least not the active part.
     */
    @SuppressWarnings("deprecation")
    public void interrupt() {
        //System.err.println("INTERRUPT!");
        genThread.stop();
    }
    
}
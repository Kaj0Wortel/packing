
package packing.generator;


// Packing imports
import packing.tools.ThreadMonitor;
import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;


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

    public Generator(PackerFactory factory) {
        this.packerFactory = factory;
    }
    
    public Dataset generate(Dataset dataset) {
        genThread = Thread.currentThread();
        
        try {
            generateSolution(dataset);
            
        } catch (ThreadDeath e) {
            System.err.println("TERMINATED BY TIME-OUT!");
            
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
        
        Dataset upperBound = null;
        
        for (Predicate<Dataset.Entry> predicate :
                Arrays.asList(Dataset.NO_ROTATION,
                        Dataset.LONGEST_SIDE_VERTIAL)) {
            dataset.setRotation(predicate);
            
            for (Comparator<Dataset.Entry> comparator :
                    Arrays.asList(Dataset.SORT_HEIGHT, Dataset.SORT_AREA,
                            Dataset.SORT_WIDTH, Dataset.SORT_LONGEST_SIDE)) {
                dataset.setOrdering(comparator);
                Packer packer = packerFactory.create(width, height);
                Dataset packed = packer.pack(dataset);
                
                if (packed != null &&
                        (upperBound == null ||
                         packed.getArea() < upperBound.getArea())) {
                    upperBound = packed;
                    width = packed.getWidth();
                }
            }
        }
        
        return upperBound;
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
        System.err.println("INTERRUPT!");
        genThread.stop();
    }
    
}
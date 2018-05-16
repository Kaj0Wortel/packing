
package packing.generator;


// Packing imports
import packing.data.*;
import packing.packer.*;


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
            
        } finally {
            return best;
        }
    }
    
    /**
     * Generates a solution for the given dataset.
     */
    public abstract void generateSolution(Dataset dataset);
    
    /**
     * Interrupts the generator when the time is up.
     * 
     * NOTE OF CAUTION:
     * The {@link Thread.stop()} method is deprecated because it is unsafe
     * since it unlocks all monitors it has locked. However, this is in
     * this case not a problem since no other threads are using any data
     * from that calculation thread, or at least not the active part.
     */
    @SuppressWarnings("Deprecated")
    public void interrupt() {
        System.err.println("INTERRUPT!");
        genThread.stop();
    }
    
}
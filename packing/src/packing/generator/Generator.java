
package packing.generator;


// Packing imports
import packing.data.*;
import packing.packer.*;


/* 
 * Abstract generator class.
 */
public abstract class Generator {
    protected PackerFactory packerFactory;

    public Generator(PackerFactory factory) {
        this.packerFactory = factory;
    }

    /* 
     * Generates a solution for the given dataset.
     */
    public abstract Dataset generate(Dataset dataset);
    
    /* 
     * Interrupts the generator.
     */
    public abstract void interrupt();
    
}
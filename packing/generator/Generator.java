
package packing.generator;


// Packing imports
import packing.data.Dataset;


/* 
 * Abstract generator class.
 */
public abstract class Generator {
    /* 
     * Generates a solution for the given dataset.
     */
    public abstract Dataset generate(Dataset dataset);
    
}
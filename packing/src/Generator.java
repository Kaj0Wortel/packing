
/* 
 * Abstract generator class.
 */
public abstract class Generator {
    /* 
     * Generates a solution for the given dataset.
     */
    public abstract Dataset generate(Dataset dataset);
    
    /* 
     * Interrupts the generator.
     */
    public abstract void interrupt();
    
}
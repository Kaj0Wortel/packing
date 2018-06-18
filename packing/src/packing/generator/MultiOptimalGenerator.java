
package packing.generator;


// Packing imports.
import packing.data.Dataset;
import packing.packer.PackerFactory;


//##########


/**
 * 
 */
public class MultiOptimalGenerator
        extends Generator {
    final private Generator gen1;
    final private Generator gen2;
    
    public MultiOptimalGenerator(Generator gen1, Generator gen2) {
        super(null);
        this.gen1 = gen1;
        this.gen2 = gen2;
    }
    
    @Override
    public void generateSolution(Dataset dataset) {
        best = gen1.generate(dataset);
        System.err.println("finished gen1");
        Dataset probBest = gen2.generate(dataset);
        System.err.println("finished gen2");
        
        best.calcEffectiveSize();
        probBest.calcEffectiveSize();
        
        if (best.getArea() > probBest.getArea()) {
            best = probBest;
        }
    }
    
}

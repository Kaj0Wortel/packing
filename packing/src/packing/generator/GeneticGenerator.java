
package packing.generator;

import packing.data.Dataset;
import packing.packer.PackerFactory;

/**
 * Generator for the genetic variant.
 */
public class GeneticGenerator
        extends Generator {
    final private static int BATCH_SIZE = 20;
    
    public GeneticGenerator(PackerFactory factory) {
        super(factory);
    }
    
    @Override
    public void generateSolution(Dataset data) {
        best = null;
    }
    
}

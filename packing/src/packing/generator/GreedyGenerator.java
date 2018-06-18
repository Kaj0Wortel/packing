
package packing.generator;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.packer.PackerFactory;

//##########

/**
 * Generates a greedy solution.
 */
public class GreedyGenerator extends Generator {

    static {
        name = "greedy";
    }

    public GreedyGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public void generateSolution(Dataset dataset) {
        dataset.setRotation(CompareEntry.NO_ROTATION);
        best = generateUpperBound(dataset);
    }
}

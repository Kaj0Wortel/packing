
package packing.generator;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.packer.PackerFactory;

//##########

/**
 * Class description here.
 */
public class FixedHeightGenerator extends Generator {
    public FixedHeightGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public void generateSolution(Dataset dataset) {
        dataset.setRotation(CompareEntry.NO_ROTATION);
        best = generateUpperBound(dataset);
    }
}

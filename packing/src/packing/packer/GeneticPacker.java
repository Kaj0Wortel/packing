
package packing.packer;

import packing.data.CompareEntry;
import packing.data.Dataset;

/**
 * The genetic packer variant.
 */
public class GeneticPacker extends Packer {
    
    public GeneticPacker(int width, int height) {
        // TODO
    }
    
    @Override
    public Dataset pack(Dataset data) {
        Dataset clone = data.clone();
        
        for (CompareEntry entry : clone) {
            // TODO
        }
        
        return null;
    }
}

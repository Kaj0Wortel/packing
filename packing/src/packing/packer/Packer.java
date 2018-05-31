
package packing.packer;


// Packing imports
import packing.data.Dataset;

//##########

public abstract class Packer {
    abstract public Dataset pack(Dataset dataset);

    public int getMinHeightIncrease() {
        return 1;
    }
}

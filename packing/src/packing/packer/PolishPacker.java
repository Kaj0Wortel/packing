
package packing.packer;

// Packing imports
import packing.data.Dataset;

//##########
// Java imports
import java.util.LinkedList;
import java.util.List;


/**
 *
 */
public class PolishPacker extends Packer {
    
    protected List<Elem> list = new LinkedList<>();
    
    protected interface Elem {
        
    }
    
    
    
    public PolishPacker() {
        
    }
    
    
    @Override
    public Dataset pack(Dataset dataset) {
        return null;
    }
    
    
}

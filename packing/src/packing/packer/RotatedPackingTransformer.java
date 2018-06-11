
package packing.packer;

// Packing packages
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.tools.Logger;


//##########
// Java imports
import java.awt.Rectangle;


/**
 * TODO
 */
public class RotatedPackingTransformer extends Packer {

    Packer wrapped;

    public RotatedPackingTransformer(Packer packer) {
        wrapped = packer;
    }

    @Override
    public Dataset pack(Dataset dataset) {
        Logger.write("Rotating bounding box...");
        Dataset rotated = dataset.clone();
        rotated.setSize(dataset.getHeight(), dataset.getWidth());

        for (CompareEntry entry : rotated) {
            Rectangle rec = entry.getNormalRec();
            entry.setSize(rec.height, rec.width);
        }

        Dataset solution = wrapped.pack(rotated);

        if (solution == null) return null;

        solution.setSize(dataset.getWidth(), dataset.getHeight());
        for (CompareEntry entry : solution) {
            Rectangle rec = entry.getNormalRec();
            entry.setSize(rec.height, rec.width);
            entry.setLocation(rec.y, rec.x);
        }
        return solution;
    }
}

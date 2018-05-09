
package packing.generator;


// Packing imports
import packing.data.*;
import packing.packer.*;


// Java imports
import java.awt.Rectangle;

/**
 * Generates bounding boxes starting at a wide, low bounding box,
 * and incrementally decreases the width and increases the height.
 * Stops when the width is less than the width of the widest rectangle,
 * or when an interrupt is received.
 */
public class WideToHighBoundingBoxGenerator extends Generator {
    private boolean stopped = false;

    public WideToHighBoundingBoxGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public Dataset generate(Dataset dataset) {
        int width = 0,
            height = 0,
            minWidth = 0,
            minHeight = 0,
            minArea = 0;
        for (Dataset.Entry entry : dataset) {
            Rectangle rect = entry.getRec();
            width += rect.width;
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);
            minArea += rect.width * rect.height;
        }
        height = minHeight;

        Dataset best = null;

        while (!stopped && width >= minWidth) {
            if (width * height < minArea) {
                ++height;
                continue;
            } else if (best != null && width * height > best.getArea()) {
                --width;
                continue;
            }
//            System.out.printf("Packing into [%d x %d] bounding box\n", width, height);
            Packer packer = packerFactory.create(width, height);
            Dataset packed = packer.pack(dataset);

            if (packed != null) {
                packed.setWidth(packed.getEffectiveWidth());
                if (best == null || packed.getArea() < best.getArea()) {
                    best = packed;
                }
                width = best.getEffectiveWidth();
                --width;
            } else {
                ++height;
            }
        }
        return best;
    }
    
    @Override
    public void interrupt() {
        //System.err.println("interrupted!");
        stopped = true;
    }
    
}
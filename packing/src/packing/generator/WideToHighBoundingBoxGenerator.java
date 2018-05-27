
package packing.generator;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;


// Java imports
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Comparator;


/**
 * Generates bounding boxes starting at a wide, low bounding box,
 * and incrementally decreases the width and increases the height.
 * Stops when the width is less than the width of the widest rectangle,
 * or when an interrupt is received.
 */
public class WideToHighBoundingBoxGenerator extends Generator {
    
    public WideToHighBoundingBoxGenerator(PackerFactory factory) {
        super(factory);
    }
    
    @Override
    public void generateSolution(Dataset dataset) {
        // By default set the best solution to null.
        best = null;

        dataset.setRotation(CompareEntry.NO_ROTATION);

        int width = 0;
        int height = 0;
        int minWidth = 0;
        int minHeight = 0;
        int minArea = 0;
        int numPacked = 0, numSkipped = 0;

        // Initialize the local variables.
        for (Dataset.Entry entry : dataset) {
            Rectangle rect = entry.getRec();
            width += rect.width;
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);
            minArea += rect.width * rect.height;
        }
        
        // At the start, set the current height to the minimal height.
        height = minHeight;
        
        // Loop until the time has run out, or the minimal
        // width has been reached.
        try {
            Comparator<CompareEntry> bestOrdering = null;

            for (Comparator<CompareEntry> comparator : Arrays.asList(
                    CompareEntry.SORT_HEIGHT,
                    CompareEntry.SORT_AREA,
                    CompareEntry.SORT_WIDTH,
                    CompareEntry.SORT_LONGEST_SIDE)) {
                dataset.setOrdering(comparator);
                Packer packer = packerFactory.create(width, height);
                Dataset packed = packer.pack(dataset);
                numPacked++;

                if (best == null || packed.getArea() < best.getArea()) {
//                    System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", packed.getWidth(), packed.getHeight(),
//                            100 * (packed.getArea() - minArea) / (double) packed.getArea());
                    best = packed;
                    bestOrdering = comparator;
                }
            }

            dataset.setOrdering(bestOrdering);

            while (width >= minWidth) {
                if (width * height < minArea) {
                    // If the current area is less then the minimal solution area,
                    // increase the height until the area is more then the minimal
                    // solution area.
                    height = Math.max((int) Math.ceil(((double) minArea) / width),
                            height + 1);
                    numSkipped++;
                    continue;
                }

                // If there is already a solution available, and that area is
                // already smaller then the area we want to try, then we simply
                // skip all these configurations.
                if (best != null && width * height >= best.getArea()) {
                    width = Math.min(best.getArea() / height,
                            width - 1);
                    numSkipped++;
                    continue;
                }

//            System.err.printf("Packing into [%d x %d] bounding box\n", width, height);

                // Obtains a packer, and pack the dataset with this packer.
                Packer packer = packerFactory.create(width, height);
                Dataset packed = packer.pack(dataset);
                numPacked++;

                if (packed != null) {
                    // Update the width of the solution to the effective width
                    // (so cropping unused space of), and set this width
                    // as the width of the solution.
                    packed.setWidth(width = packed.getEffectiveWidth());

                    // Update the best solution if nessecary.
                    if (best == null || packed.getArea() < best.getArea()) {
//                    System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", packed.getWidth(), packed.getHeight(),
//                            100 * (packed.getArea() - minArea) / (double) packed.getArea());
                        best = packed;
                    }
                    --width;
                }
                height += packer.getMinHeightIncrease();
            }
        } finally {
//            System.err.printf("Generated %d packings, skipped %d...\n", numPacked, numSkipped);
        }
    }
}

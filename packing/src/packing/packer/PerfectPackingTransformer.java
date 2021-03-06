
package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.tools.Logger;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;


/**
 * Transforms a dataset into a perfect packing instance, such that any
 * solution perfectly fills in the bounding box.
 * <p>
 * Used in the absolute placement approach.
 */
public class PerfectPackingTransformer extends Packer {
    private Packer wrappedPacker;

    public PerfectPackingTransformer(Packer packer) {
        this.wrappedPacker = packer;
    }

    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Clone the dataset, then add 1x1 rectangles until the total area
        of the rectangles is equal to the total are of the bounding box.

        Call wrappedPacker.pack() and return the result.
         */

        // keep track of area per column of 1 width
        int[] columns = new int[dataset.getWidth()];
        Set<Integer> original = new HashSet<>();

        Dataset perfectDataSet = dataset.clone();
        for (CompareEntry entry : dataset) {
            original.add(entry.getId());
            Rectangle rec = entry.getRec();
            for (int i = rec.x; i < (rec.x + rec.width); i++) {
                columns[i] += rec.height;
            }
        }

        int created = 0;
       
       /*
        adds 1x1 rectangles to every column whose total height is not yet the
        height of the bounding box. Doing this for every column is equivalent 
        to adding 1x1 rectangles until total rectangle area = total area of bounding box.
        Every rectangle has a fixed x coordinate this way which means we do not 
        need to distinguish between fixed x and not when placing the y-coordinates
        */
        for (int i = 0; i < columns.length; i++) {
            while (columns[i] < dataset.getHeight()) {
                created++;
                Rectangle rec = new Rectangle(i, 0, 1, 1);
                perfectDataSet.add(rec);
                columns[i]++;
            }
        }

        Logger.write(String.format("Created %,d new rectangles", created));


        Dataset wrappedDataSet = wrappedPacker.pack(perfectDataSet);

        if (wrappedDataSet == null) {
            return null;
        }

        Dataset Solved = wrappedDataSet.clone();

        for (CompareEntry placed : wrappedDataSet) {
            if (!original.contains(placed.getId())) {
                Solved.remove(placed);
            }
            // remove rec from original
            // this is to avoid cases in which the original has a 1x1 rec, and it adds all 1x1 recs
            // instead of only the ones in original
            original.remove(placed.getId());
        }

        return Solved;
    }
}

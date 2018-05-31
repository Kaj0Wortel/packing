
package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


/**
 * Transforms a dataset into a perfect packing instance, such that any
 * solution perfectly fills in the bounding box.
 *
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
        List<Rectangle> original = new ArrayList<Rectangle>();
        
        Dataset perfectDataSet = dataset.clone();
        for(CompareEntry entry: dataset){
            Rectangle rec = entry.getRec();
            original.add(rec);
            for(int i = rec.x; i <(rec.x + rec.width ); i++){
                columns[i] += rec.height;
            }
        }
       
       /*
        adds 1x1 rectangles to every column whose total height is not yet the
        height of the bounding box. Doing this for every column is equivalent 
        to adding 1x1 rectangles until total rectangle area = total area of bounding box.
        Every rectangle has a fixed x coordinate this way which means we do not 
        need to distinguish between fixed x and not when placing the y-coordinates
        */
       for(int i = 0; i < columns.length; i++){
            while(columns[i] < dataset.getHeight()){
                Rectangle rec = new Rectangle(i,0,1,1);
                perfectDataSet.add(rec);
                columns[i]++;
            }
        }
        
        
        Dataset wrappedDataSet = wrappedPacker.pack(perfectDataSet);
        
        Dataset Solved = wrappedDataSet.clone();
        
        for(CompareEntry placed: wrappedDataSet){
            Rectangle placedRec = placed.getRec();
            Rectangle compareRec = new Rectangle(placedRec.x, 0, placedRec.width, placedRec.height);
            if(!original.contains(compareRec)){
                Solved.remove(placed);
            }
            // remove rec from original
            // this is to avoid cases in which the original has a 1x1 rec, and it adds all 1x1 recs
            // instead of only the ones in original
            original.remove(compareRec);
        }
        
        return Solved;
    }
}

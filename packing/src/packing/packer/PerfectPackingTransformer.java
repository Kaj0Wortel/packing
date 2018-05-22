
package packing.packer;


// Packing imports
import java.awt.Rectangle;
import packing.data.*;


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
        int rectArea = 0;
        int area = 0;
        int[] columns = new int[dataset.getWidth()-1]; // columns[0] being the colums from x = 0 to x = 1
        
        area = dataset.getArea();
        Dataset perfectDataSet = dataset.clone();
        for(Dataset.Entry entry: dataset){
            Rectangle rec = entry.getRec();
            for(int i = rec.x; i <(rec.x + rec.width -1); i++){
                columns[i] += rec.height;
            }
            rectArea += entry.area();
        }
       
       /*
        adds 1x1 rectangles to every column whose total height is not yet the
        height of the bounding box. Doing this for every column is equivalent 
        to adding 1x1 rectangles until total rectangle area = total area of bounding box.
        Every rectangle has a fixed x coordinate this way which means we do not 
        need to distinguish between fixed x and not when placing the y-coordinates
        */
       for(int i : columns){
            while(columns[i] < dataset.getHeight()){
                Rectangle rec = new Rectangle(i,0,1,1);
                perfectDataSet.add(rec);
                columns[i]++;
            }
        }
        
        
        Dataset wrappedDataSet = wrappedPacker.pack(perfectDataSet);
        
        return wrappedDataSet;
    }
}


package packing.packer;


// Packing imports
import java.awt.Rectangle;
import java.lang.reflect.Array;
import packing.data.*;


/**
 * Assign the X-coordinate to every rectangle in the dataset, then call an inner packer
 * to assign the Y-coordinate.
 *
 * Used in the absolute placement approach.
 */
public class XCoordinatePacker extends Packer {
    private Packer yPacker;

    public XCoordinatePacker(Packer packer) {
        this.yPacker = packer;
    }

    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Calculate an X-placement for every rectangle, then call this.yPacker.pack()
        to calculate the Y-placement. If both are successful, return the packing,
        otherwise try next X-placement.

        Use a backtracking algorithm to try every possible placement. To optimize, we can
        use the following pruning techniques as described in Algorithms Overview:
        - Wasted space pruning
        - Empty-strip dominance
         */
        
        // create solution as an empty boundingBox with the same values as the input
        Dataset solution = new Dataset(dataset.getWidth(),dataset.allowRotation(), dataset.getHeight());
        
        solution = backtracker(dataset, solution, 0);
        
        return null;
    }
    /**
     * 
     * @param input the input dataset
     * @param solution the current solution dataset, added to allow for pruning
     * since otherwise we will start with a dataset of all rectangles placed 
     * at (0,0) which is most likely immediately invalid. 
     * @param current index of rectangle currently being placed
     * @return 
     */
    public Dataset backtracker(Dataset input, Dataset solution, int current){
        int width = input.getWidth();
        
        
        if(solution.size()<input.size()){
            Dataset.Entry entry = input.get(current);
            for(int j = 0; j<width; j++){
                Rectangle rec = entry.getRec();
                rec.setLocation(j, 0);
                solution.add(rec);
                if(heightPruning(input, solution)){ // if solution is still viable continue backtracking
                    current++;
                    Dataset backtrackSolution = backtracker(input, solution, current);
                    if(backtrackSolution != null){
                        return backtrackSolution;
                    }
                    current--;
                    solution.remove(rec);
                } else { // otherwise remove placed rectangle and try next position
                    solution.remove(rec);
                }
                
            } 
        } else {
            return yPacker.pack(solution);
        }        
        return null;
    }
    
    /**
     * 
     * @param dataset current configuration
     * @return false if current configuration does not fit
     */
    public boolean heightPruning(Dataset input, Dataset solution){
        int[] height = new int[input.getWidth()]; // height of every column of width 1
        // e.g height[0] is the height of the column with x-coordinate 0
        
        for(Dataset.Entry entry: solution){
            Rectangle rec = entry.getRec();
            for(int i = (int)rec.getX(); i<(int)rec.getWidth(); i++){
                height[i] += (int)rec.getHeight();
                // height of all rectangles in the column x=i is more than the height of the bounding box
                if(height[i] > input.getHeight()){
                    return false;
                }
            }
        }
        
        return true;
    }
}

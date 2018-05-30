
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
        Dataset solution = Dataset.createEmptyDataset(dataset);
        
        solution = backtracker(dataset.clone(), solution, 0);
        //System.out.println("Backtrack finished");
        return solution;
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
    public Dataset backtracker(Dataset input, Dataset solution, int current) {
        int width = input.getWidth();
        
        
            System.out.println("started");
        if (solution.size() < input.size()) {
            CompareEntry entry = input.get(current);
            for (int j = 0; j < width; j++) {
                Rectangle rec = entry.getRec();
                if (j + rec.width > solution.getWidth()) {
                    continue;
                }
                rec.setLocation(j, 0);
                CompareEntry addedEntry = solution.add(new Rectangle(rec));
                if (heightPruning(input, solution)) { // if solution is still viable continue backtracking
                    current++;
                    //System.out.println("Backtracking");
                    Dataset backtrackSolution = backtracker(input, solution, current);
                    if (backtrackSolution != null) {
                        return backtrackSolution;
                    }
                    current--;
                    
                }
                solution.remove(addedEntry);
                
            } 
        } else {
            //System.out.println("Making perfect packing");
            return yPacker.pack(solution);
        }        
        return null;
    }
    
    /**
     * 
     * @param dataset current configuration
     * @return false if current configuration does not fit
     */
    public boolean heightPruning(Dataset input, Dataset solution) {
        int[] height = new int[solution.getWidth()]; // height of every column of width 1
        // e.g height[0] is the height of the column with x-coordinate 0 to x =1
        
        //System.out.println("call");
        
        for (CompareEntry entry : solution) {
            Rectangle rec = entry.getRec();
            //System.out.println("next rect");
            //System.out.println(rec.width);
            for (int i = rec.x; i < (rec.x + rec.width); i++) {
              //  System.out.println(solution.getWidth() + " last index of array and " + i);
                height[i] += rec.height;
                System.out.println("height: " + height[i] + " at " + i);
                // height of all rectangles in the column x=i is more than the height of the bounding box
                if (height[i] > input.getHeight()) {
                    //System.out.println("return");
                    return false;
                }
            }
        }
        
        return true;
    }
}

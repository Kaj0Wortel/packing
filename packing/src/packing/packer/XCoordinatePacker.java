
package packing.packer;


// Packing imports
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import packing.data.*;


/**
 * Assign the X-coordinate to every rectangle in the dataset, then call an inner packer
 * to assign the Y-coordinate.
 *
 * Used in the absolute placement approach.
 */
public class XCoordinatePacker extends Packer {
    private Packer yPacker;

    public static int recursions = 0;

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

        Stack<CompareEntry> entries = new Stack<>();

        for (CompareEntry entry : dataset) {
            entries.push(entry.clone());
        }
        
        // create solution as an empty boundingBox with the same values as the input
        Dataset solution = Dataset.createEmptyDataset(dataset);

        int[] columns = new int[dataset.getWidth()];

        solution = backtrack(entries, solution, columns);
        
//        solution = backtracker(dataset.clone(), solution, 0);
        System.err.printf("X-packer: %,d recursions\n", recursions);
        recursions = 0;
        //System.out.println("Backtrack finished");
        return solution;
    }

    public boolean addRectangle(Rectangle rec, int[] columns, int height) {
        for (int i = rec.x; i < rec.x + rec.width; i++) {
            if (columns[i] + rec.height > height) {
                for (i--; i >= rec.x; i--) {
                    columns[i] -= rec.height;
                }
                return false;
            }

            columns[i] += rec.height;
        }
        return true;
    }

    public void removeRectangle(Rectangle rec, int[] columns) {
        for (int i = rec.x; i < rec.x + rec.width; i++) {
            columns[i] -= rec.height;
        }
    }

    public boolean pruneWastedSpace(List<CompareEntry> entries, int[] columns, int height) {
        int[] emptySpace = new int[height + 1];
        for (int j = 0; j < columns.length; j++){
            int columnHeight = height - columns[j];
            emptySpace[columnHeight] += columnHeight;
        }

        for(CompareEntry entry: entries){
            Rectangle rect = entry.getRec();
            int areaToBeFilled = entry.area();
            for (int k = rect.height; k < height; k++){
                while (emptySpace[k] > 0 && areaToBeFilled > 0) {
                    emptySpace[k]--;
                    areaToBeFilled--;
                }
            }
            if (areaToBeFilled > 0) {
                return false;
            }
        }
        return true;
    }

    public Dataset backtrack(Stack<CompareEntry> entries, Dataset solution, int[] columns) {
        recursions++;
        if (!entries.isEmpty()) {
            int width = solution.getWidth();
            int height = solution.getHeight();

            CompareEntry entry = entries.pop();
            Rectangle rec = entry.getRec();
            for (int j = 0; j + rec.width <= width; j++) {
                entry.setLocation(j, 0);
                CompareEntry added = solution.add(new Rectangle(rec));
                if (addRectangle(rec, columns, height)) {
                    if (pruneWastedSpace(entries, columns, height)) {
                        Dataset backtrackSolution = backtrack(entries, solution, columns);
                        if (backtrackSolution != null) {
                            return backtrackSolution;
                        }
                    }
                    removeRectangle(rec, columns);
                }
                solution.remove(added);
            }
            entries.push(entry);
            return null;
        } else {
            long startTime = System.currentTimeMillis();
            solution = yPacker.pack(solution);
            System.out.println("Runtime (Y-packer): " + (System.currentTimeMillis() - startTime) + " ms");
            return solution;
        }
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
        recursions++;
        int width = input.getWidth();
        
        
           // System.out.println("started");
        if (solution.size() < input.size()) {
            CompareEntry entry = input.get(current);
            Rectangle rec = entry.getRec();
            for (int j = 0; j < width; j++) {
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
            /*for(CompareEntry rectEntry: solution){
                Rectangle rects = rectEntry.getRec();
                System.out.println(rects);
            }*/
            return yPacker.pack(solution);
        }        
        return null;
    }
    
    /**
     *
     * @param input full dataset
     * @param solution current configuration
     * @return false if current configuration does not fit
     */
    public boolean heightPruning(Dataset input, Dataset solution) {
        // height of every column of width 1
        // e.g height[0] is the height of the column with x-coordinate 0 to x =1
        int[] height = new int[solution.getWidth()]; 
        
        // clone of input
        Dataset toBePlaced = input.clone(); 
        
        for (CompareEntry entry : solution) {
            Rectangle rec = entry.getRec();
            // clone entry to remove it from the toBePlaced dataset
            CompareEntry cloneEntry = entry.clone();
            // set location of cloneEntry to that of its duplicate in the original dataset
            cloneEntry.setLocation(0, 0);
            toBePlaced.remove(cloneEntry);
            //System.out.println("next rect");
            //System.out.println(rec.width);
            for (int i = rec.x; i < (rec.x + rec.width); i++) {
              //  System.out.println(solution.getWidth() + " last index of array and " + i);
                height[i] += rec.height;
                //System.out.println("height: " + height[i] + " at " + i);
                // height of all rectangles in the column x=i is more than the height of the bounding box
                if (height[i] > input.getHeight()) {
                    //System.out.println("return");
                    return false;
                }
            }
        }
        
        /* create an array with the following info:
         for every column of height i, indicate how many empty cells there are
        e.g {3,0,9} means there are 3 empty cells in columns of height 1,
        0 in columns of height 2 and 9 in clumns of height 9
        */
        int[] emptySpace = new int[solution.getHeight()];
        for(int j = 0; j < solution.getWidth(); j++){
            // height of the empty column
            int columnHeight = solution.getHeight() - height[j];
            
            /*
            amount of empty cells in column of height columnHeight
            increases by columnHeight
            */
            if(columnHeight > 0){
                emptySpace[columnHeight-1] += columnHeight;
            }         
        }
        
        /*
        Wasted space pruning
        if there are not enough cells of height > rect.height for 
        every rectangle still to be placed. Not all the rectangles 
        can be placed, thus we prune
        */
        for(CompareEntry entry1: toBePlaced){
            Rectangle rect = entry1.getRec();
            int areaToBeFilled = entry1.area();
            boolean rectFits = false;
            for(int k = rect.height-1; k < solution.getHeight(); k++){
                if(areaToBeFilled > 0){
                    while(emptySpace[k] > 0 && areaToBeFilled > 0)
                    emptySpace[k] --;
                    areaToBeFilled --;                            
                } else {
                    rectFits = true;
                    break;
                }
            }
            // if there is a rect that does not fit, we prune
            if(!rectFits){
                return false;
            }
        }
        return true;
    }
}


package packing.packer;


// Packing imports
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import packing.data.*;
import packing.tools.Logger;


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
        - Column height
        - Wasted space pruning
        - Empty-strip dominance
         */

        // All entries that have to be placed.
        Stack<CompareEntry> entries = new Stack<>();

        for (CompareEntry entry : dataset) {
            entries.push(entry.clone());
        }
        
        // Create solution as an empty boundingBox with the same values as the input.
        Dataset solution = Dataset.createEmptyDataset(dataset);

        // The empty space in each column, initialized to the height of the bounding box.
        int[] columns = new int[dataset.getWidth()];
        Arrays.fill(columns, dataset.getHeight());

        solution = backtrack(entries, solution, columns);
        
        Logger.write(String.format("X-packer: %,d recursions", recursions));
        recursions = 0;
        return solution;
    }

    /**
     * Check if {@code rec} can be placed in its current X-position, i.e.
     * each column has enough empty space left.
     *
     * @param rec The rectangle to be placed.
     * @param columns The empty space in each column.
     * @return Whether the rectangle can be placed.
     */
    private boolean canPlaceRectangle(Rectangle rec, int[] columns) {
        for (int i = rec.x; i < rec.x + rec.width; i++) {
            if (rec.height > columns[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Add {@code rec} to its current X-position, decrease the empty
     * space in {@code columns[rec.x:rec.x + rec.width]} by {@code rec.height}.
     *
     * @param rec The rectangle to be placed.
     * @param columns The empty space in each column.
     */
    private void placeRectangle(Rectangle rec, int[] columns) {
        for (int i = rec.x; i < rec.x + rec.width; i++) {
            columns[i] -= rec.height;
        }
    }

    /**
     * Remove {@code rec} from the occupied space in {@code columns}
     * after it has been added with {@code placeRectangle()}.
     *
     * @param rec The rectangle to be removed.
     * @param columns The empty space in each column.
     */
    private void removeRectangle(Rectangle rec, int[] columns) {
        for (int i = rec.x; i < rec.x + rec.width; i++) {
            columns[i] += rec.height;
        }
    }

    /**
     * Prune based on wasted space. For every entry that still has to be placed,
     * there has to be at least {@code entry.area} space left in columns that have
     * at least {@code entry.height} unoccupied space. If that's not the case, we
     * cannot get a valid configuration and we can prune this branch.
     *
     * @param entries The entries that still have to be placed
     * @param columns The empty space in each column.
     * @param maxHeight The maximum height of each column.
     * @return Whether the current configuration can still provide a valid configuration
     *         according to wasted-space pruning.
     */
    private boolean pruneWastedSpace(List<CompareEntry> entries, int[] columns, int maxHeight) {
        int[] emptySpace = new int[maxHeight + 1];
        for (int j = 0; j < columns.length; j++){
            int columnHeight = columns[j];
            emptySpace[columnHeight] += columnHeight;
        }

        for(CompareEntry entry: entries){
            Rectangle rect = entry.getRec();
            int areaToBeFilled = entry.area();
            for (int k = rect.height; k < maxHeight; k++){
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

    /**
     * Place {@code entry} on every X-coordinate and apply pruning to filter
     * out invalid solutions.
     * Currently pruning happens based on:
     *   - Column height
     *   - Wasted space
     *
     * @param entries The entries that still have to be placed.
     * @param solution The current (partial) solution.
     * @param columns The empty space in each column.
     * @param entry The entry that has to be placed.
     * @return A valid and complete solution, or null.
     */
    private Dataset placeEntry(Stack<CompareEntry> entries, Dataset solution, int[] columns, CompareEntry entry) {
        int width = solution.getWidth();
        int height = solution.getHeight();
        Rectangle rec = entry.getRec();

        Dataset backtrackSolution = null;

        for (int j = 0; backtrackSolution == null && j + rec.width <= width; j++) {
            entry.setLocation(j, 0);
            if (canPlaceRectangle(rec, columns)) {
                placeRectangle(rec, columns);
                if (pruneWastedSpace(entries, columns, height)) {
                    backtrackSolution = backtrack(entries, solution, columns);
                }
                removeRectangle(rec, columns);
            }
        }
        return backtrackSolution;
    }

    /**
     * Place entries using backtracking. If there are entries left, pop the
     * first one and try to place it in the solution. If rotations are allowed,
     * also try to place the rotated rectangle. If no entries are left, call
     * {@code yPacker} to find the complete solution.
     *
     * @param entries The entries that still have to be placed.
     * @param solution The current (partial) solution.
     * @param columns The empty space in each column.
     * @return A valid and complete solution, or null.
     */
    private Dataset backtrack(Stack<CompareEntry> entries, Dataset solution, int[] columns) {
        recursions++;
        if (!entries.isEmpty()) {
            CompareEntry entry = entries.pop();
            CompareEntry newEntry = solution.add(new Rectangle(entry.getNormalRec()));

            Dataset backtrackSolution = placeEntry(entries, solution, columns, newEntry);

            if (backtrackSolution == null && solution.allowRotation()) {
                newEntry.rotate();
                backtrackSolution = placeEntry(entries, solution, columns, newEntry);
            }

            if (backtrackSolution != null) {
                return backtrackSolution;
            }

            solution.remove(newEntry);
            entries.push(entry);
            return null;
        } else {
            long startTime = System.currentTimeMillis();
            solution = yPacker.pack(solution);
            Logger.write("Runtime (Y-packer): " + (System.currentTimeMillis() - startTime) + " ms");
            return solution;
        }
    }
}

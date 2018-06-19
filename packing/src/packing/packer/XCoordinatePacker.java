
package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.tools.Logger;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;


/**
 * Assign the X-coordinate to every rectangle in the dataset, then call an inner packer
 * to assign the Y-coordinate.
 *
 * Used in the absolute placement approach.
 */
public class XCoordinatePacker extends Packer {
    private Packer yPacker;

    public long recursions = 0;
    public long places = 0;
    public long pruned = 0;
    public long yNumCalls = 0;
    public long yTime = 0;

    // Positions at which rectangles can be placed satisfying the bottom-left stability property.
    private List<Integer> positions;
    // The empty space in each column.
    private int[] columns;
    // Histogram of empty space, such that emptySpace[i] is the number of empty cells in empty columns of height i.
    private int[] emptySpace;
    // The sum of areas of rectangles, grouped by height.
    private int[] rectangleAreaByHeight;

    private boolean allowRotation;

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

        allowRotation = dataset.allowRotation();

        dataset.setOrdering(Collections.reverseOrder(CompareEntry.SORT_WIDTH));

        positions = calculateSubsetSums(dataset, Arrays.asList(0));

        // All entries that have to be placed.
        Stack<CompareEntry> entries = new Stack<>();

        for (CompareEntry entry : dataset) {
            entries.push(entry.clone());
        }
        
        // Create solution as an empty boundingBox with the same values as the input.
        Dataset solution = Dataset.createEmptyDataset(dataset);

        // The empty space in each column, initialized to the height of the bounding box.
        columns = new int[dataset.getWidth()];
        Arrays.fill(columns, dataset.getHeight());

        // Histogram of number of empty cells in empty columns of height i.
        emptySpace = new int[dataset.getHeight() + 1];
        emptySpace[dataset.getHeight()] = dataset.getArea();

        rectangleAreaByHeight = new int[dataset.getHeight() + 1];
        for (CompareEntry entry : entries) {
            Rectangle rec = entry.getRec();
            int side = allowRotation ? Math.min(rec.width, rec.height) : rec.height;
            rectangleAreaByHeight[side] += rec.width * rec.height;
        }

        solution = backtrack(entries, solution);

        Logger.write(String.format("X-packer: %,d recursions, %,d branches pruned, %,d places", recursions, pruned, places));
        Logger.write(String.format("Y-packer: %,d calls, %,d ms", yNumCalls, yTime));
        return solution;
    }

    /**
     * Calculate the subset sum of width (and heights, if rotations are allowed)
     * of every entry in the dataset, up to the dataset's width.
     *
     * @param dataset The dataset for which to calculate the sum.
     * @return Sorted list of integers in the subset sum.
     */
    public List<Integer> calculateSubsetSums(Dataset dataset, List<Integer> initial) {
        Set<Integer> positionSet = new HashSet<>(initial);
        int maxWidth = dataset.getWidth();

        for (CompareEntry entry : dataset) {
            Rectangle rec = entry.getNormalRec();
            Set<Integer> newPositions = new HashSet<>();
            for (int position : positionSet) {
                if (position + rec.width < maxWidth) {
                    newPositions.add(position + rec.width);
                }
                if (dataset.allowRotation() && position + rec.height < maxWidth) {
                    newPositions.add(position + rec.height);
                }
            }
            positionSet.addAll(newPositions);
        }
        List<Integer> positions = new ArrayList<>(positionSet);
        positions.sort(Integer::compare);
        return positions;
    }

    /**
     * Check if {@code rec} can be placed in its current X-position, i.e.
     * each column has enough empty space left.
     *
     * @param rec The rectangle to be placed.
     * @return Whether the rectangle can be placed.
     */
    private int canPlaceRectangle(Rectangle rec, int x) {
        if (x + rec.width > columns.length) {
            return rec.width + columns.length;
        }

        for (int i = x; i < x + rec.width; i++) {
            if (rec.height > columns[i]) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Add {@code rec} to its current X-position, decrease the empty
     * space in {@code columns[rec.x:rec.x + rec.width]} by {@code rec.height}.
     *
     * @param rec The rectangle to be placed.
     */
    private void placeRectangle(Rectangle rec) {
        int side = allowRotation ? Math.min(rec.width, rec.height) : rec.height;
        rectangleAreaByHeight[side] -= rec.width * rec.height;

        for (int i = rec.x; i < rec.x + rec.width; i++) {
            emptySpace[columns[i]] -= columns[i];
            columns[i] -= rec.height;
            emptySpace[columns[i]] += columns[i];
        }
    }

    /**
     * Remove {@code rec} from the occupied space in {@code columns}
     * after it has been added with {@code placeRectangle()}.
     *
     * @param rec The rectangle to be removed.
     */
    private void removeRectangle(Rectangle rec) {
        int side = allowRotation ? Math.min(rec.width, rec.height) : rec.height;
        rectangleAreaByHeight[side] += rec.width * rec.height;

        for (int i = rec.x; i < rec.x + rec.width; i++) {
            emptySpace[columns[i]] -= columns[i];
            columns[i] += rec.height;
            emptySpace[columns[i]] += columns[i];
        }
    }

    /**
     * Prune based on wasted space. For every entry that still has to be placed,
     * there has to be at least {@code entry.area} space left in columns that have
     * at least {@code entry.height} unoccupied space. If that's not the case, we
     * cannot get a valid configuration and we can prune this branch.
     *
     * @param solution The current (partial) solution.
     * @return Whether the current configuration can still provide a valid configuration
     * according to wasted-space pruning.
     */
    private boolean pruneWastedSpace(Dataset solution) {
        int height = solution.getHeight();
        int rectangles = 0;
        int free = 0;

        for (int i = height; i >= 0; i--) {
            rectangles += rectangleAreaByHeight[i];
            free += emptySpace[i];
            if (rectangles > free) {
                pruned++;
                return false;
            }
        }
        return true;
    }

    /**
     * Place {@code entry} on every X-coordinate and apply pruning to filter
     * out invalid solutions.
     * Currently pruning happens based on:
     * - Column height
     * - Wasted space
     *
     * @param entries  The entries that still have to be placed.
     * @param solution The current (partial) solution.
     * @param entry    The entry that has to be placed.
     * @return A valid and complete solution, or null.
     */
    private Dataset placeEntry(Stack<CompareEntry> entries, Dataset solution, CompareEntry entry) {
        Rectangle rec = entry.getRec();

        Dataset backtrackSolution;

        int min = -1;
        for (int j : positions) {
            if (j <= min) continue;
            places++;
            if ((min = canPlaceRectangle(rec, j)) == -1) {
                entry.setLocation(j, 0);
                placeRectangle(rec);

                if (pruneWastedSpace(solution)) {
                    backtrackSolution = backtrack(entries, solution);
                    if (backtrackSolution != null) {
                        return backtrackSolution;
                    }
                }

                removeRectangle(rec);
            }
        }
        return null;
    }

    /**
     * Place entries using backtracking. If there are entries left, pop the
     * first one and try to place it in the solution. If rotations are allowed,
     * also try to place the rotated rectangle. If no entries are left, call
     * {@code yPacker} to find the complete solution.
     *
     * @param entries  The entries that still have to be placed.
     * @param solution The current (partial) solution.
     * @return A valid and complete solution, or null.
     */
    private Dataset backtrack(Stack<CompareEntry> entries, Dataset solution) {
        recursions++;
        if (!entries.isEmpty()) {
            CompareEntry entry = entries.pop();
            Rectangle rec = entry.getRec();
            CompareEntry newEntry = solution.push(entry);

            Dataset backtrackSolution = placeEntry(entries, solution, newEntry);

            if (backtrackSolution == null && solution.allowRotation() && rec.width != rec.height) {
                newEntry.rotate();
                backtrackSolution = placeEntry(entries, solution, newEntry);
            }

            if (backtrackSolution != null) {
                return backtrackSolution;
            }

            solution.pop(newEntry);
            entries.push(entry);
            return null;
        } else {
            long startTime = System.currentTimeMillis();
            yNumCalls++;
            solution = yPacker.pack(solution);
            yTime += System.currentTimeMillis() - startTime;
            return solution;
        }
    }
}


package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;


//##########
// Java imports
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;


/**
 * Given a perfect packing instance with assigned X-coordinates, calculate a solution
 * to assign the Y-coordinates.
 *
 * Used in the absolute placement approach.
 */
public class YCoordinatePacker extends Packer {
    public int recursions = 0;
    public int numCalls = 0;

    private Deque<Point> corners;
    private int[] emptySquares;
    private int[] lastEmptySquare;
    private boolean[][] cells;
    private NavigableSet<Integer> positions;

    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Keep track of corners (starting with just (0,0) as the initial corner),
        and use a backtracking algorithm to fill rectangles with the correct
        X-coordinate.
         */
        numCalls++;
        corners = new ArrayDeque<>();
        corners.add(new Point(0,0));

        emptySquares = new int[dataset.getWidth()];
        Arrays.fill(emptySquares, dataset.getHeight());

        lastEmptySquare = new int[dataset.getWidth()];

        positions = calculateSubsetSums(dataset);

        Dataset solution = Dataset.createEmptyDataset(dataset);

        // entryList.get(x) contains all rectangles placed at coordinate x
        List<List<CompareEntry>> entryLists = new ArrayList<>();

        for (int i = 0; i < dataset.getWidth(); i++) {
            entryLists.add(new ArrayList<>());
        }

        for (CompareEntry entry : dataset) {
            Rectangle rec = entry.getRec();
            List<CompareEntry> entryList = entryLists.get(rec.x);
            entryList.add(entry);
            for (int i = rec.x; i < rec.x + rec.width; i++) {
                emptySquares[i] -= rec.height;
            }
        }

        // cells[x][y] is true if (x, y) is filled by some rectangle
        cells = new boolean[dataset.getWidth()][dataset.getHeight()];

        solution =  backtrack(entryLists, solution);
        //Logger.write(String.format("Y-packer: %,d recursions", recursions));
        return solution;
    }

    /**
     * Calculate the subset sum of heights of every entry in the dataset.
     *
     * @param dataset The dataset for which to calculate the sum.
     * @return Sorted list of integers in the subset sum.
     */
    public NavigableSet<Integer> calculateSubsetSums(Dataset dataset) {
        Set<Integer> positionSet = new HashSet<>();
        positionSet.add(0);
        int maxHeight = dataset.getHeight();

        for (CompareEntry entry : dataset) {
            Rectangle rec = entry.getRec();
            Set<Integer> newPositions = new HashSet<>();
            for (int position : positionSet) {
                if (position + rec.height < maxHeight) {
                    newPositions.add(position + rec.height);
                }
            }
            positionSet.addAll(newPositions);
        }
        return new TreeSet<>(positionSet);
    }

    /**
     * Check to see if {@code rec} can be placed into {@code solution} at
     * position {@code p}.
     *
     * @param solution The current solution.
     * @param cells Cells in the bounding box and whether they've been filled.
     * @param rec The rectangle to be placed.
     * @param p Position to place rectangle in.
     * @return Whether the rectangle fits at the specified position.
     */
    private boolean canPlaceRectangle(Dataset solution, boolean[][] cells, Rectangle rec, Point p) {
        if (p.x + rec.width > solution.getWidth() || p.y + rec.height > solution.getHeight()) {
            return false;
        }

        for (int i = p.x; i < p.x + rec.width; i++) {
            for (int j = p.y; j < p.y + rec.height; j++) {
                if (cells[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Place {@code rec} at location {@code p}, and fill in the space in {@code cells}.
     *
     * @param cells Cells in the bounding box and whether they've been filled.
     * @param rec The rectangle to place.
     * @param p The location to place {@code rec}.
     */
    private void placeRectangle(boolean[][] cells, Rectangle rec, Point p) {
        for (int i = rec.x; i < (rec.x + rec.width ); i++) {
            for(int j = rec.y; j < (rec.y + rec.height ); j++){
                cells[i][j] = true;
            }
        }
    }

    /**
     * Clear the space of {@code rec} in {@code cells}.
     *
     * @param cells Cells in the bounding box and whether they've been filled.
     * @param rec The rectangle to remove.
     */
    private void removeRectangle(boolean[][] cells, Rectangle rec) {
        for (int i = rec.x; i < (rec.x + rec.width ); i++) {
            for(int j = rec.y; j < (rec.y + rec.height ); j++){
                cells[i][j] = false;
            }
        }
    }

    /**
     * Calculate the set of new corners created by placing {@code rec} into
     * {@code solution} at point {@code p}. A corner must be a lower-left
     * corner formed by other rectangles and/or the sides of the bounding box.
     *
     * @param solution The current (partial) solution.
     * @param cells Cells in the bounding box and whether they've been filled.
     * @param rec The last placed rectangle.
     * @param p The point where {@code rec} was placed.
     * @return Set of rectangles to be added.
     */
    private List<Point> getNewCorners(Dataset solution, boolean[][] cells, Rectangle rec, Point p){
        List<Point> updatedCorners = new ArrayList<>();

        int width = solution.getWidth();
        int height = solution.getHeight();

        if (p.x + rec.width < width) {
            int x = p.x + rec.width;
            int y = p.y;

            // Get the lowest empty cell along the rectangle's right side.
            while (y < p.y + rec.height && cells[x][y]) {
                y++;
            }

            // Check that (x, y) is along the rectangle's right side, and
            // either (x, y) is on the bounding box's bottom border or the
            // cell to the bottom is filled.
            if (y < p.y + rec.height && (y == 0 || cells[x][y-1])) {
                updatedCorners.add(new Point(x, y));
            }
        }

        if (p.y + rec.height < height) {
            int x = p.x;
            int y = p.y + rec.height;

            // Get the left-most empty cell along the rectangle's top side.
            while (x < width && cells[x][y] && x < p.x + rec.width) {
                x++;
            }

            // Check that (x, y) is along the rectangle's top side, and
            // either (x, y) is on the bounding box's left border or the
            // cell to the left is filled.
            if (x < p.x + rec.width && (x == 0 || cells[x-1][y])) {
                updatedCorners.add(new Point(x, y));
            }
        }

        return updatedCorners;
    }

    /**
     * Place entries using backtracking. Get the first empty corner, and try
     * every entry that is assigned to that corner's X-coordinate, then recurse
     * to find a solution.
     *
     * @param entryLists Entries to be placed.
     * @param solution The current (partial) solution.
     * @param cells Cells in the bounding box and whether they've been filled.
     * @param corners Empty lower-left corners where a rectangle can be placed.
     * @return A valid and complete solution, or null.
     */
    private Dataset backtrack(List<List<CompareEntry>> entryLists, Dataset solution){
        recursions++;
        if (corners.isEmpty()) {
            if (entryLists.stream().allMatch(List::isEmpty)) {
                return solution;
            }
            return null;
        }

        Point p = corners.removeFirst();

        Set<Rectangle> seen = new HashSet<>();

        List<CompareEntry> entryList = entryLists.get(p.x);
        for (int k = 0; k < entryList.size(); k++) {
            CompareEntry entry = entryList.get(k);
            Rectangle rec = entry.getRec();

            if (seen.contains(rec)) {
                continue;
            }

            seen.add(new Rectangle(rec));

            if (canPlaceRectangle(solution, cells, rec, p)) {
                List<Point> updatedCorners = getNewCorners(solution, cells, rec, p);

                entryList.remove(k);

                entry.setLocation(p.x, p.y);
                placeRectangle(cells, rec, p);
                CompareEntry addedEntry = solution.add(entry);
                corners.addAll(updatedCorners);

                Dataset possibleSolution = backtrack(entryLists, solution);
                if (possibleSolution != null) {
                    return possibleSolution;
                }

                corners.removeAll(updatedCorners);
                solution.remove(addedEntry);
                removeRectangle(cells, rec);
                entry.setLocation(p.x, 0);

                entryList.add(k, entry);
            }
        }

        if (lastEmptySquare[p.x] != p.y) {
            for (int i : positions.tailSet(p.y + 1)) {
                Rectangle rec = new Rectangle(p.x, p.y, 1, i - p.y);
                List<Point> updatedCorners = getNewCorners(solution, cells, rec, p);

                emptySquares[p.x] -= i;
                placeRectangle(cells, rec, p);
                corners.addAll(updatedCorners);

                int last = lastEmptySquare[p.x];
                lastEmptySquare[p.x] = p.y + i;

                Dataset possibleSolution = backtrack(entryLists, solution);
                if (possibleSolution != null) {
                    return possibleSolution;
                }

                lastEmptySquare[p.x] = last;

                corners.removeAll(updatedCorners);
                removeRectangle(cells, rec);
                emptySquares[p.x] += i;
            }
        }

        corners.addFirst(p);
        return null;
    }
}

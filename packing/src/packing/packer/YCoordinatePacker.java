
package packing.packer;


// Packing imports
import java.awt.Point;
import java.util.*;

import packing.data.*;
import packing.gui.ShowDataset;
import packing.tools.MultiTool;

import java.awt.Rectangle;


/**
 * Given a perfect packing instance with assigned X-coordinates, calculate a solution
 * to assign the Y-coordinates.
 *
 * Used in the absolute placement approach.
 */
public class YCoordinatePacker extends Packer {
    public static int recursions = 0;

    private boolean[][] entries;
    private int inputSize;
    private List<List<CompareEntry>> entryLists;

    @Override
    public Dataset pack(Dataset dataset) {
        //System.out.println("Packing Y");
        /*
        Keep track of corners (starting with just (0,0) as the initial corner,
        and use a backtracking algorithm to fill rectangles with the correct
        X-coordinate.
         */
        //System.out.println("started Y");
        Deque<Point> corners = new ArrayDeque<>();
        corners.add(new Point(0,0));

        Dataset solution = Dataset.createEmptyDataset(dataset);
        IgnoreDoubleDataset doubleDataset = new IgnoreDoubleDataset(dataset);

        entryLists = new ArrayList<>();

        for (int i = 0; i < dataset.getWidth(); i++) {
            entryLists.add(new ArrayList<>());
        }

        for (CompareEntry entry : dataset) {
            List<CompareEntry> entryList = entryLists.get(entry.getRec().x);
            entryList.add(entry);
        }

        entries = new boolean[dataset.getWidth()][dataset.getHeight()];
        inputSize = dataset.size();
        solution =  backtracker(doubleDataset, solution, corners);
        System.err.printf("Y-packer: %,d recursions\n", recursions);
        recursions = 0;
        return solution;
    }

    public Dataset backtracker(Dataset input, Dataset solution, Deque<Point> corners){
        recursions++;
        if (solution.size() == inputSize) {
            return solution;
        }

        Point p = corners.removeFirst();

        List<CompareEntry> entryList = entryLists.get(p.x);
        for (int k = 0; k < entryList.size(); k++) {
            CompareEntry entry = entryList.get(k);
            Rectangle rec = entry.getRec();

            if (checkIfFits(rec, p, input)) {
                entryList.remove(k);
                rec.y = p.y;
                CompareEntry addedEntry = solution.add(new Rectangle(rec));
                for (int i = rec.x; i < (rec.x + rec.width ); i++) {
                    for(int j = rec.y; j < (rec.y + rec.height ); j++){
                        entries[i][j] = true;
                    }
                }
                List<Point> updatedCorners = updateCorners(solution, rec, p);

                for (Point point : updatedCorners) {
                    corners.addLast(point);
                }

                Dataset possibleSolution = backtracker(input, solution, corners);
                if (possibleSolution != null) {
                    return possibleSolution;
                }
                for (Point point : updatedCorners) {
                    corners.removeLast();
                }
                for (int i = rec.x; i < (rec.x + rec.width ); i++) {
                    for(int j = rec.y; j < (rec.y + rec.height ); j++){
                        entries[i][j] = false;
                    }
                }
                solution.remove(addedEntry);
                rec.y = 0;
                entryList.add(k, entry);
            }
        }

        corners.addFirst(p);
        return null;
    }

    public boolean checkIfFits(Rectangle rec, Point p, Dataset input){
        for (int i = p.x; i < (p.x + rec.width ); i++) {
            for (int j = p.y; j < (p.y + rec.height ); j++) {
                if (i > input.getWidth() - 1 || j > input.getHeight() - 1) {
                    return false;
                }
                if (entries[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     *
     * @param solution current solution
     * @param rec last placed rectangle
     * @param p point to be removed
     * @return updated list of corners
     */
    public List<Point> updateCorners(Dataset solution, Rectangle rec, Point p){
        List<Point> updatedCorners = new ArrayList<>();

        int width = solution.getWidth();
        int height = solution.getHeight();

        if (p.x + rec.width < width) {
            int x = p.x + rec.width, y = p.y;
            while (y < height && entries[x][y] && y < p.y + rec.height) y++;
            if (y < p.y + rec.height && !entries[x][y] && (y - 1 < 0 || entries[x][y-1])) {
                updatedCorners.add(new Point(x, y));
            }
        }

        if (p.y + rec.height < height) {
            int x = p.x, y = p.y + rec.height;
            while (x < width && entries[x][y] && x < p.x + rec.width) x++;
            if (x < p.x + rec.width && !entries[x][y] && (x - 1 < 0 || entries[x-1][y])) {
                updatedCorners.add(new Point(x, y));
            }
        }

        return updatedCorners;
    }
}

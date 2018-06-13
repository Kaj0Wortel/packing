
package packing.generator;


// Packing imports

import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.gui.ShowDataset;
import packing.packer.*;
import packing.tools.Logger;
import packing.tools.MultiTool;
import packing.tools.StreamLogger;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.*;


/**
 * Generates bounding boxes by increasing area, starting at a lower bound
 * for the Dataset instance. When used with an optimal packer, this ensures
 * the final solution is optimal.
 */
public class OptimalBoundingBoxGenerator extends Generator {

    static {
        name = "optimal";
    }

    public OptimalBoundingBoxGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public void generateSolution(Dataset dataset) {
        best = null;
        /** Calculate the total area of the rectangles as a lower bound.
         Try packing the rectangles into every possible bounding box of
         that size, increasing the area if it doesn't fit. Return when a
         solution is found.
         See Algorithms Overview for ways to generate the bounding boxes. */
        int minArea = 0;
        int minWidth = 0;       // width of the widest rectanlge
        int minHeight = 0;
        int maxArea;            // area of the greedy solution
        int maxWidth;           // width of the greedy solution
        int maxHeight;
        int greedyWidth = 0;    // height to create greedy solution
        int greedyHeight = 0;   // width to create greedy solution
        int width = 0;
        int height = 0;
        int area = 0;

        PriorityQueue<Rectangle> boundingBoxHeap;

        // Determine minArea, greedyWidth, greedyHeight, minWidth, minHeight.
        for (CompareEntry entry : dataset) {
            Rectangle rect = entry.getRec();
            minArea += rect.width * rect.height;
            greedyWidth += rect.width;
            greedyHeight = Math.max(greedyHeight, rect.height);
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);
        }

        if (dataset.allowRotation()) {
            minWidth = Math.min(minWidth, minHeight);
        }

        // Determine maxArea and maxWidth.
        Packer greedyPacker = new GreedyPackerFactory().create(
                greedyWidth,
                dataset.isFixedHeight() ? dataset.getHeight() : greedyHeight
        );
        Dataset greedyPacked = greedyPacker.pack(dataset);
        maxArea = greedyPacked.getArea();
        maxWidth = greedyPacked.getWidth();

        NavigableSet<Integer> widths = calculateSubsetSums(dataset, true);
        NavigableSet<Integer> heights = calculateSubsetSums(dataset, false);

        boundingBoxHeap = createInitialHeap(dataset, widths, heights, minArea);

        //REMOVE AFTER
        //Rectangle rectangle = new Rectangle(4,13);
        //boundingBoxHeap.insert(rectangle);
        while (best == null) {
            Rectangle rect = boundingBoxHeap.poll();// get minimum boundingbox
            Logger.write(rect + " BoundingBox");

            if ((rect.width * rect.height) >= greedyPacked.getArea()) {
                Logger.write(String.format("Using greedy solution... [%dx%d]", greedyPacked.getWidth(), greedyPacked.getHeight()));
                best = greedyPacked;
            } else {
                //System.out.println(rect.width + "width");
                width = rect.width;
                height = rect.height;
                dataset.setWidth(width);
                dataset.setHeight(height);
                //System.out.println("" + dataset.getArea());
                //System.out.println(dataset.getHeight() + " height and width " + dataset.getWidth());
                Packer packer = packerFactory.create(width, height); //create packing instance for said box
                //System.out.println("tst");
                Dataset packed = packer.pack(dataset); // try to pack the box

                //if possible, than this is the optimal solution
                if (packed != null) {
                    best = packed;
                } else if (!dataset.isFixedHeight()) {
                    // else increase height and put the new boundingBox in the heap
                    //System.out.println("Nope");
                    Integer h = heights.higher(height);
                    if (h != null) {
                        rect.setSize(width, h);
                        boundingBoxHeap.add(rect);
                    }
                }
            }
        }
        Logger.write("Finished");
    }

    /**
     * Calculate the subset sum of width (and heights, if rotations are allowed)
     * of every entry in the dataset, up to the dataset's width.
     *
     * @param dataset The dataset for which to calculate the sum.
     * @return Sorted list of integers in the subset sum.
     */
    public NavigableSet<Integer> calculateSubsetSums(Dataset dataset, boolean horizontal) {
        Set<Integer> positionSet = new HashSet<>();
        positionSet.add(0);

        for (CompareEntry entry : dataset) {
            Rectangle rec = entry.getNormalRec();
            Set<Integer> newPositions = new HashSet<>();
            for (int position : positionSet) {
                if (dataset.allowRotation()) {
                    newPositions.add(position + rec.width);
                    newPositions.add(position + rec.height);
                } else if (horizontal) {
                    newPositions.add(position + rec.width);
                } else {
                    newPositions.add(position + rec.height);
                }
            }
            positionSet.addAll(newPositions);
        }
        return new TreeSet<>(positionSet);
    }

    /**
     * Determine minHeight for current box.
     *
     * @param dataset input set
     * @param width   width of current boundingBox
     * @param minArea minimum needed area
     * @return minimum height required for this box
     */
    public int determineHeight(Dataset dataset, int width, int minArea) {
        if (width == 0) {
            //System.err.println("width: " + width);
            return Integer.MAX_VALUE;
        }
        if (dataset.allowRotation()) {
            return minArea / width;
        }

        int minHeight = 0;
        int possibleHeight = 0;
        int minHeightHalfWidth = Integer.MAX_VALUE;

        //Logger.write(minArea);

        for (CompareEntry entry : dataset) {
            Rectangle rect = entry.getRec();
            if (rect.width == (width / 2)) {
                // rectangle of half width with smalles height
                minHeightHalfWidth = Math.min(minHeightHalfWidth, rect.height);
            }

            if (rect.width > (width / 2)) {
                // all rectangles greater than half the width need to be stacked
                possibleHeight += rect.height;
            }

            for (CompareEntry entry1 : dataset) {
                if (entry1 == entry) {
                    continue;
                }
                Rectangle rect1 = entry1.getRec();
                // every pair greater than full width need to be stacked
                if (rect.width + rect1.width > width) {
                    minHeight = Math.max(minHeight, rect.height + rect1.height);
                }
            }

            if (minHeight < rect.height) {
                minHeight = rect.height;
            }
        }

        if (minHeightHalfWidth < Integer.MAX_VALUE) {
            possibleHeight += minHeightHalfWidth;
            minHeight = Math.max(minHeight, possibleHeight);
        }
        // if current box is smaller than minimum box, increase height
        // to be sufficient
        if ((minHeight * width) < minArea) {
            //round up
            minHeight = (int) Math.ceil((double) minArea / width);
        }
        //Logger.write(width + " width and height " + minHeight);

        return minHeight;
    }

    /**
     * @param dataset  input set
     * @param minWidth minimum width for boundingBox
     * @param maxWidth maximum width for boundingBox
     * @param minArea  minimum area for boundingBox
     * @return a heap with the initial set of boxes, containing boxes
     * of every width between minWidth and maxWidth, with an appropriate minHeight
     */
    public PriorityQueue<Rectangle> createInitialHeap(Dataset dataset, NavigableSet<Integer> widths,
                                                      NavigableSet<Integer> heights, int minArea) {
        PriorityQueue<Rectangle> initialHeap = new PriorityQueue<>(Comparator.comparingLong(
                rec -> ((long) rec.width) * ((long) rec.height)
        ));
        // Loop over all possible widths
        for (int i : widths) {
            if (i <= 0) {
                //System.out.println("width: " + i);
                continue;
            }
            int height;

            if (dataset.isFixedHeight()) {
                if (determineHeight(dataset, i, minArea) > dataset.getHeight()) continue;
                height = dataset.getHeight();
            } else {
                Integer h = heights.ceiling(determineHeight(dataset, i, minArea));
                if (h == null) continue;
                height = h;
            }
            // System.out.println(i + "Width and Height" + height);
            Rectangle rect = new Rectangle(i, height);
            initialHeap.add(rect);
            //System.out.println("added");
        }

        return initialHeap;
    }

    // tmp
    public static void main(String[] args) {
        // Logger setup (to disable logging, comment next line).
//        MultiTool.sleepThread(10000);

        Dataset data = new Dataset(-1, false, 15);
        Logger.setDefaultLogger(new StreamLogger(System.out));
        long startTime = System.currentTimeMillis();
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        /**/
        data.add(new Rectangle(10, 10));
        data.add(new Rectangle(10, 10));
        data.add(new Rectangle(1, 1));
        data.add(new Rectangle(2, 2));
        data.add(new Rectangle(3, 3));
        
        data.add(new Rectangle(4, 4));
        data.add(new Rectangle(5, 5));
        data.add(new Rectangle(6, 6));
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 2));
        /**
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(2, 6));
        /**
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(10, 10));
        /**/
        //Dataset data = new Dataset(10, false, 1);
        //data.add(new Rectangle(10, 10));

        Generator generator = new OptimalBoundingBoxGenerator(new OptimalPackerFactory());
        Dataset result = generator.generate(data);
        //time calculation
        long runtime = System.currentTimeMillis() - startTime;
        int mins = (int) runtime / 60000;
        int remainder = (int) runtime - mins * 60000;
        int secs = remainder / 1000;
        int millsecs = remainder - secs * 1000;
        System.out.println("Runtime: " + mins + "min " + secs + "s " + millsecs + "ms.");
        System.out.println("Runtime in milliseconds: " + (System.currentTimeMillis() - startTime) + " ms");
        MultiTool.sleepThread(200);
        System.err.println();
        System.err.println(result);
        new ShowDataset(result);
    }
}

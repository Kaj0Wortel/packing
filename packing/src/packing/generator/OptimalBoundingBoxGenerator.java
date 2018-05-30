
package packing.generator;


// Packing imports
import java.awt.Rectangle;
import packing.data.*;
import packing.packer.*;
import packing.generator.RectangleMinHeap;
import packing.gui.ShowDataset;
import packing.tools.MultiTool;


/**
 * Generates bounding boxes by increasing area, starting at a lower bound
 * for the Dataset instance. When used with an optimal packer, this ensures
 * the final solution is optimal.
 */
public class OptimalBoundingBoxGenerator extends Generator {
    
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
        RectangleMinHeap boundingBoxHeap = new RectangleMinHeap(); // heap to keep track of all the possible bounding boxes in order of non decreasing area
        
        // Determine minArea, greedyWidth, greedyHeight, minWidth, minHeight.
        for (CompareEntry entry : dataset){
            Rectangle rect = entry.getRec();
            minArea += rect.width * rect.height;
            greedyWidth += rect.width;
            greedyHeight = Math.max(greedyHeight, rect.height);
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);            
        }
        
        // Determine maxArea and maxWidth.
        Packer greedyPacker = packerFactory.create(greedyWidth, greedyHeight);
        Dataset greedyPacked = greedyPacker.pack(dataset);
        maxArea = greedyPacked.getArea();
        maxWidth = greedyPacked.getWidth();
        
        //UNCOMMENT
        //boundingBoxHeap = createInitialHeap(dataset, minWidth, maxWidth, minArea);
        
        //REMOVE AFTER
        Rectangle rectangle = new Rectangle(4,13);
        boundingBoxHeap.insert(rectangle);
        while(best == null){
            Rectangle rect = boundingBoxHeap.extractMin();// get minimum boundingbox
            //System.out.println(rect + "rect");
            //System.out.println(rect.width + "width");
            width = rect.width;
            height = rect.height;
            dataset.setWidth(width);
            dataset.setHeight(height);
            //System.out.println("" + dataset.getArea());
            System.out.println(dataset.getHeight() + " height and width " + dataset.getWidth());
            Packer packer = new XCoordinatePacker(new PerfectPackingTransformer(new YCoordinatePacker())); //create packing instance for said box
            System.out.println("tst");
            Dataset packed = packer.pack(dataset); // try to pack the box
            
            //if possible, than this is the optimal solution
            if(packed != null){
                best = packed;
            } else {
                // else increase height and put the new boundingBox in the heap
                System.out.println("Nope");                
                //UNCOMMENT
                /*height++;
                rect.setSize(width, height);
                
                if((rect.width * rect.height) >= greedyPacked.getArea()){*/
                    best = greedyPacked;
               /* } else {
                    boundingBoxHeap.insert(rect);
                }*/

            }
        }
    }
    
    /**
     * Determine minHeight for current box.
     * 
     * @param dataset input set
     * @param width width of current boundingBox
     * @param minArea minimum needed area
     * @return minimum height required for this box
     */
    public int determinHeight(Dataset dataset, int width, int minArea) {
        int minHeight = 0;
        int possibleHeight = 0;
        int minHeightHalfWidth = Integer.MAX_VALUE;
        
        for(CompareEntry entry : dataset){
            Rectangle rect = entry.getRec();
            if(rect.width == (width / 2)){
                // rectangle of half width with smalles height
                minHeightHalfWidth = Math.min(minHeightHalfWidth, rect.height);
            }
            
            if (rect.width > (width / 2)){
                // all rectangles greater than half the width need to be stacked
                possibleHeight += rect.height;
            }
            
            for(CompareEntry entry1 : dataset){
                Rectangle rect1 = entry1.getRec();
                // every pair greater than full width need to be stacked
                if(rect.width + rect1.width > width){
                    minHeight = Math.max(minHeight, rect.height + rect1.height);
                }                    
            }
            
            if(minHeight < rect.height){
                minHeight = rect.height;
            }
        }
        
        if(minHeightHalfWidth < Integer.MAX_VALUE){
            possibleHeight += minHeightHalfWidth;
            minHeight = Math.max(minHeight, possibleHeight);
            }
            // if current box is smaller than minimum box, increase height
            // to be sufficient
        if((minHeight * width) < minArea){
            minHeight = minArea/width;
        }       
            
        
        return minHeight;        
    }
    /**
     * 
     * @param dataset input set
     * @param minWidth minimum width for boundingBox
     * @param maxWidth maximum width for boundingBox
     * @param minArea minimum area for boundingBox
     * @return a heap with the initial set of boxes, containing boxes 
     * of every width between minWidth and maxWidth, with an appropriate minHeight
     */
    public RectangleMinHeap createInitialHeap(Dataset dataset, int minWidth, int maxWidth, int minArea){
        RectangleMinHeap initialHeap = new RectangleMinHeap();
        // Loop over all possible widths
        for(int i = minWidth; i < maxWidth; i++){
            //System.out.println(i);
            int height = determinHeight(dataset, i, minArea); 
           // System.out.println(i + "Width and Height" + height);
            Rectangle rect = new Rectangle(i, height);
            initialHeap.insert(rect);
            //System.out.println("added");
        }
        
        return initialHeap;
    }
    
     // tmp
    public static void main(String[] args) {
        // Logger setup (to disable logging, comment next line).
        
        Dataset data = new Dataset(-1, false, 4);
        //Logger.setDefaultLogger(new StreamLogger(System.out));
        long startTime = System.currentTimeMillis();
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        //data.add(new Rectangle(10, 10));
        /**//*
        data.add(new Rectangle(1, 1));
        data.add(new Rectangle(2, 2));
        data.add(new Rectangle(3, 3));
        data.add(new Rectangle(4, 4));
        data.add(new Rectangle(5, 5));
        data.add(new Rectangle(6, 6));
        /**//*
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(4, 3));
        /**//*
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(10, 10));
        /**/
        //Dataset data = new Dataset(10, false, 1);
        //data.add(new Rectangle(10, 10));
        
        Generator generator = new OptimalBoundingBoxGenerator(new GreedyPackerFactory());
        Dataset result = generator.generate(data);
        System.out.println("Runtime: " + (System.currentTimeMillis() - startTime) + " ms");
        MultiTool.sleepThread(200);
        System.err.println();
        System.err.println(result);
        new ShowDataset(result);
    }
}

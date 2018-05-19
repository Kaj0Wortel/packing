
package packing.generator;


// Packing imports
import java.awt.Rectangle;
import packing.data.*;
import packing.packer.*;
import packing.generator.RectangleMinHeap;


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
        for (Dataset.Entry entry : dataset){
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
        
        boundingBoxHeap = createInitialHeap(dataset, minWidth, maxWidth, minArea);
        
        
        while(best == null){
            Rectangle rect = boundingBoxHeap.extractMin();// get minimum boundingbox
            width = (int)rect.getWidth();
            height = (int)rect.getHeight();
            Packer packer = packerFactory.create(width, height); //create packing instance for said box
            Dataset packed = packer.pack(dataset); // try to pack the box
            
            //if possible, than this is the optimal solution
            if(packed != null){
                best = packed;
            } else {
                // else increase height and put the new boundingBox in the heap
                height++;
                rect.setSize(width, height);
                boundingBoxHeap.insert(rect);
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
        
        for(Dataset.Entry entry : dataset){
            Rectangle rect = entry.getRec();
            if(rect.width == (width / 2)){
                // rectangle of half width with smalles height
                minHeightHalfWidth = Math.min(minHeightHalfWidth, rect.height);
            }
            
            if (rect.width > (width / 2)){
                // all rectangles greater than half the width need to be stacked
                possibleHeight += rect.height;
            }
            
            for(Dataset.Entry entry1 : dataset){
                Rectangle rect1 = entry1.getRec();
                // every pair greater than full width need to be stacked
                if(rect.width + rect1.width > width){
                    minHeight = Math.max(minHeight, rect.height + rect1.height);
                }                    
            }
        }
        
        if(minHeightHalfWidth < Integer.MAX_VALUE){
            possibleHeight += minHeightHalfWidth;
            minHeight = Math.max(minHeight, possibleHeight);
            // if current box is smaller than minimum box, increase height
            // to be sufficient
            if((minHeight * width) < minArea){
                minHeight = minArea/width;
            }
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
            int height = determinHeight(dataset, i, minArea);     
            Rectangle rect = new Rectangle(i, height);
            initialHeap.insert(rect);
        }
        
        return initialHeap;
    }
    
}


package packing.generator;


// Packing imports
import java.awt.Rectangle;
import java.util.List;
import packing.data.*;
import packing.packer.*;


/**
 * Generates bounding boxes by increasing area, starting at a lower bound
 * for the Dataset instance. When used with an optimal packer, this ensures
 * the final solution is optimal.
 */
public class OptimalBoundingBoxGenerator extends Generator {
    private boolean stopped = false;
    
    public OptimalBoundingBoxGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public Dataset generate(Dataset dataset) {
        /*
        Calculate the total area of the rectangles as a lower bound.
        Try packing the rectangles into every possible bounding box of
        that size, increasing the area if it doesn't fit. Return when a
        solution is found.

        See Algorithms Overview for ways to generate the bounding boxes.
         */
        int minArea = 0;
        int minWidth = 0;       // width of the widest rectanlge
        int minHeight = 0;
        int maxArea;            // area of the greedy solution
        int maxWidth;           //width of the greedy solution
        int maxHeight;
        int greedyWidth = 0;    // height to create greedy solution
        int greedyHeight = 0;   // width to create greedy solution
        int width = 0;
        int height = 0;
        int area = 0;
        
        /**
         * determine minArea, greedyWidth, greedyHeight, minWidth, minHeight
         */
        for (Dataset.Entry entry : dataset){
            Rectangle rect = entry.getRec();
            minArea += rect.width * rect.height;
            greedyWidth += rect.width;
            greedyHeight = Math.max(greedyHeight, rect.height);
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);            
        }
        /**
         * determine maxArea and maxWidth
         */
        Packer greedyPacker = packerFactory.create(greedyWidth, greedyHeight);
        Dataset greedyPacked = greedyPacker.pack(dataset);
        maxArea = greedyPacked.getArea();
        maxWidth = greedyPacked.getWidth();
        
        width = minWidth;
        height = determinHeight(dataset, width, minArea);
        
        while(!stopped && area < maxArea){
            Packer packer = packerFactory.create(width, height);
            Dataset packed = packer.pack(dataset);
        }
        
               
        
        return null;
    }
    
    /**
     * Determine minHeight for current box
     * @param dataset input set
     * @param width width of current boundingBox
     * @param minArea minimum needed area
     * @return minimum height required for this box
     */
    public int determinHeight(Dataset dataset, int width, int minArea){
        
        int minHeight = 0;
        int possibleHeight = 0;
        int minHeightHalfWidth = Integer.MAX_VALUE;
        for(Dataset.Entry entry : dataset){
            Rectangle rect = entry.getRec();
            if(rect.width == (width/2)){
                // rectangle of half width with smalles height
                minHeightHalfWidth = Math.min(minHeightHalfWidth, rect.height);
            }
            if (rect.width > (width/2)){
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
            // if current box is smaller than minimum box, increase height to be sufficient
            if((minHeight * width) < minArea){
                minHeight = minArea/width;
            }
        }
        
        return minHeight;        
    }

    @Override
    public void interrupt() {
        stopped = true;
    }
}


package packing.packer;


// Packing imports
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import packing.data.*;
import java.awt.Rectangle;


/**
 * Given a perfect packing instance with assigned X-coordinates, calculate a solution
 * to assign the Y-coordinates.
 *
 * Used in the absolute placement approach.
 */
public class YCoordinatePacker extends Packer {
    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Keep track of corners (starting with just (0,0) as the initial corner,
        and use a backtracking algorithm to fill rectangles with the correct
        X-coordinate.
         */
        
        List<Point> corners = new ArrayList<Point>();
        corners.add(new Point(0,0));
        return null;
    }
    
    public Dataset backtracker(Dataset input, Dataset solution, List<Point> corners){
        
        for(Point p: corners){
            for(Dataset.Entry entry: input){
                Rectangle rec = entry.getRec();
                if(rec.x == p.x){
                    rec.y = p.y;
                    solution.add(rec);
                    input.remove(entry.getRec());
                    corners.remove(p);
                    List<Point> updatedCorners = updateCorners(solution, rec, corners);
                    backtracker(input, solution,updatedCorners);
                    corners.add(p);
                    input.add(entry.getRec());
                    solution.remove(rec);
                }
                solution.add(rec);
                
            }
        }
        
        return null;
    }
    
    /**
     * 
     * @param solution current solution
     * @param rec last placed rectangle
     * @param corners list of corners before rec was placed excluding corner where x was placed
     * @return updated list of corners
     */
    public List<Point> updateCorners(Dataset solution, Rectangle rec, List<Point> corners){
        List<Point> updatedCorners = corners;
        Point topLeft = null;
        Point bottomRight = null;
        
        for(Dataset.Entry entry: solution){
            if((entry.getRec() != rec &&     //entry is not the same rectangle as rec
                    // left side of rec touches the right side of the other rectangle
                    rec.x == (entry.getRec().x + entry.getRec().width) &&  
                    // right rectangle starts below or at the same point as top of rec
                    entry.getRec().y <= (rec.y + rec.height) &&
                    // right rectangle ends above the top of rec
                    (rec.y +rec.height) < (entry.getRec().y + entry.getRec().height)) ||
                    // rec is agains the right handside of the box
                    rec.x == 0
                    ) {
                topLeft = new Point(rec.x, rec.y + rec.height);
                updatedCorners.add(topLeft);
            }
            
            if((entry.getRec() != rec &&  //entry is not the same rectangle as rec
                    // right rectangle touches the right side of rec
                    (rec.x + rec.width) == entry.getRec().x &&
                    // right rectangle does not end above the bottom of rec
                    rec.y >= (entry.getRec().y + entry.getRec().height) &&
                    // right rectangle starts below rec
                    rec.y > (entry.getRec().y)) ||
                    //rec is at bottom level and does not touch the right edge of the box
                    (rec.y == 0 && (rec.x + rec.width) != solution.getWidth())
                    ) {
                bottomRight = new Point((rec.x + rec.width), rec.y );
                updatedCorners.add(bottomRight);
            }
            
            if(topLeft != null && bottomRight != null){
                return updatedCorners;
            }              
        }
        return updatedCorners;
    }
}

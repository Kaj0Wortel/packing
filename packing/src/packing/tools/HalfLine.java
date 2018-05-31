
package packing.tools;


//##########
// Java imports
import java.awt.Rectangle;


/**
 * Class representing a half-line.
 */
public class HalfLine
        implements Cloneable {
    public static enum Direction {
        UP, RIGHT, DOWN, LEFT;
    }
    
    public int x;
    public int y;
    public Direction dir;
    
    /**
     * Create a half-line starting at [@code (x, y)} in the direction of
     * {@code dir}.
     * 
     * @param x1 the start x-coord of the line.
     * @param y1 the start y-coord of the line.
     * @param dir the direction of the line.
     * @throws NullPointerException iff {@code dir == null}.
     */
    public HalfLine(int x, int y, Direction dir) {
        if (dir == null) throw new NullPointerException("Invallid direction!");
        this.x = x;
        this.y = y;
        this.dir = dir;
    }
    
    /**
     * Checks if the line intersects with the bounderies of the rectangle.
     * 
     * @param rec the rectangle to check for.
     * @return whether the line intersects {@code rec}.
     * 
     * Note that the line always intersects if it starts within or at an edge
     * (including the corners) of {@code rec}.
     */
    public boolean intersects(Rectangle rec) {
        switch (dir) {
            case UP:
                return (rec.x <= x && x <= rec.x + rec.width) &&
                        (y <= rec.y + rec.height);
            case RIGHT:
                return (rec.y <= y && y <= rec.y + rec.height) &&
                        (x <= rec.x + rec.width);
            case DOWN:
                return (rec.x <= x && x <= rec.x + rec.width) &&
                        (y >= rec.y);
            case LEFT:
                return (rec.y <= y && y <= rec.y + rec.height) &&
                        (x >= rec.x);
            default:
                return false;
        }
    }
    
    
    @Override
    public String toString() {
        return "[P:(" + x + ", " + y + "), D:" + dir.toString() + "]";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HalfLine)) return false;
        HalfLine line = (HalfLine) obj;
        return dir == line.dir &&
                 x ==   line.x &&
                 y ==   line.y;
    }
    
    @Override
    public int hashCode() {
        return MultiTool.calcHashCode(x, y, dir);
    }
    
    @Override
    public HalfLine clone() {
        return new HalfLine(x, y, dir);
    }
    
}

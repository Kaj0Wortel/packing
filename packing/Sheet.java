
// Java imports
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;


/* 
 * 
 */
public class Sheet {
    Rectangle rec;
    
    List<Sheet> children = new ArrayList<Sheet>();
    List<Rectangle> filled = new ArrayList<Rectangle>();
    
    final protected Sheet parent;
    final protected Sheet upperNeighbor;
    final protected Sheet rightNeighbor;
    
    
    
    public Sheet(Rectangle rec, Sheet parent, Sheet up, Sheet right) {
        this.rec = rec;
        this.parent = parent;
        this.upperNeighbor = up;
        this.rightNeighbor = right;
    }
    
    
    
    public Sheet getParent() {
        return parent;
    }
    
    public Sheet getUpperNeighbor() {
        return upperNeighbor;
    }
    
    public Sheet getRightNeighbor() {
        return rightNeighbor;
    }
    
    public void check(Rectangle rec) {
        // todo
    }
    
    public void fill() {
        // todo
    }
    
    public Rectangle getBounds() {
        return rec;
    }
    
    public List<Sheet> getChildren() {
        return children;
    }
    
    public List<Rectangle> getFilled() {
        return filled;
    }
    
}
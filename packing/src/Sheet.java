
// Java imports
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;


/* 
 * This class keeps track of what part of it is filled and what part isn't.
 */
public class Sheet {
    Rectangle bounds;
    
    boolean full;
    
    final protected boolean up;
    final protected boolean right;
    final protected boolean down;
    final protected boolean left;
    
    List<Sheet> children = new ArrayList<Sheet>();
    List<Rectangle> filled = new ArrayList<Rectangle>();
    
    final protected Sheet parent;
    final protected Sheet upperNeighbor;
    final protected Sheet rightNeighbor;
    
    
    /**
     * Constructs a new main sheet.
     * Uses {@link Sheet(Rectangle, Sheet, Sheet, Sheet,
     * boolean, boolean, boolean, boolean)}.
     * 
     * @param rec the bounds of this sheet.
     */
    public Sheet(Rectangle bounds) {
        this(bounds, null, null, null, true, true, true, true);
    }
    
    /**
     * Constructs a new Sheet.
     * 
     * @param rec the bounds of this sheet.
     * @param parent the parent sheet of this sheet. {@code null} if none.
     * @param up the upper neighbor of this sheet. {@code null} if none.
     * @param right the right neighbor of this sheet. {@code null} if none.
     */
    protected Sheet(Rectangle bounds, Sheet parent, Sheet upNeighbor,
            Sheet rightNeighbor,
            boolean up, boolean right, boolean down, boolean left) {
        this.bounds = bounds;
        this.parent = parent;
        this.upperNeighbor = upNeighbor;
        this.rightNeighbor = rightNeighbor;
        
        this.up = up;
        this.right = right;
        this.down = down;
        this.left = left;
    }
    
    /**
     * @return the parent sheet of {@code this}.
     */
    protected Sheet getParent() {
        return parent;
    }
    
    /**
     * @return the upper neighbor of {@code this}.
     */
    protected Sheet getUpperNeighbor() {
        return upperNeighbor;
    }
    
    /**
     * @return the right neighbor of {@code this}.
     */
    protected Sheet getRightNeighbor() {
        return rightNeighbor;
    }
    
    /**
     * @return the bounds of this sheet.
     */
    protected Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * @return the children of the sheet.
     */
    protected List<Sheet> getChildren() {
        return children;
    }
    
    /**
     * @return whether the entire sheet has been filled.
     */
    protected List<Rectangle> getFilled() {
        return filled;
    }
    
    /**
     * @return whether the entire sheet is full.
     */
    protected boolean isFull() {
        return full;
    }
    
    /**
     * @return whether this sheet is on the top side of its parent.
     */
    protected boolean isUp() {
        return up;
    }
    
    /**
     * @return whether this sheet is on the right side of its parent.
     */
    protected boolean isRight() {
        return right;
    }
    
    /**
     * @return whether this sheet is on the lower side of its parent.
     */
    protected boolean isDown() {
        return down;
    }
    
    /**
     * @return whether this sheet is on the left side of its parent.
     */
    protected boolean isLeft() {
        return left;
    }
    
    /**
     * Checks whether the given rectangle can be placed at this sheet.
     * If the rectangle is bigger then this sheet (up or right), distribute
     * the checking to the neighbours.
     * 
     * @param rec 
     * @return whether the rectangle is allowed to be placed.
     */
    protected boolean check(Rectangle rec) {
        if (children.isEmpty()) {
            if (rec.x == -1 && rec.y == -1) {
                rec.setLocation(bounds.x, bounds.y);
            }
            
            // todo
            
        } else {
            for (Sheet child : children) {
                if (child.isLeft()) {
                    if (check(rec)) {
                        return true;
                    }
                }
            }
            
            for (Sheet child : children) {
                if (child.isDown()) {
                    if (check(rec)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Fills this sheet, using the results obtained via
     * {@link check(Rectangle)}.
     * 
     * @return whether the child is completely filled.
     */
    protected boolean fill() {
        // todo
        return full;
    }
    
    /**
     * Tries to put the entry in the current sheet.
     * If this sheet has children, distribute the placing of the entry
     * to them using {@link check(Rectangle)} and {@code fill()}.
     * 
     * @param entry the entry that should be added.
     * @return whether the entry could be added.
     * 
     * Note that the previous location of the entry is NOT taken into account.
     */
    public boolean put(Dataset.Entry entry) {
        entry.getRec().setLocation(-1, -1);
        
        if (check(entry.getRec())) {
            fill();
            return true;
        }
        
        return false;
    }
    
}
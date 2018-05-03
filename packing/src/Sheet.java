
// Java imports
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;


/* 
 * This class keeps track of what part of it is filled and what part isn't.
 */
public class Sheet extends Packer {
    Rectangle bounds;
    
    boolean full;
    
    final protected List<Sheet> children = new ArrayList<Sheet>();
    final protected List<Rectangle> filled = new ArrayList<Rectangle>();
    
    final protected Sheet root;
    final protected Sheet parent;
    
    protected Rectangle update = null;
    
    /**
     * Constructs a new main sheet.
     * Uses {@link Sheet(Rectangle, Sheet, Sheet, Sheet,
     * boolean, boolean, boolean, boolean)}.
     * 
     * @param rec the bounds of this sheet.
     */
    public Sheet(Rectangle bounds) {
        this(bounds, null, null);
    }

    /**
     * Constructs a new main sheet.
     * Uses {@link Sheet (Rectangle)}.
     *
     * @param width the width of this sheet.
     * @param height the height of this sheet.
     */
    public Sheet(int width, int height) {
        this(new Rectangle(width, height));
    }
    
    /**
     * Constructs a new Sheet.
     * 
     * @param rec the bounds of this sheet.
     * @param root the root of all sheets.
     * @param parent the parent sheet of this sheet. {@code null} if none.
     */
    protected Sheet(Rectangle bounds, Sheet root, Sheet parent) {
        this.bounds = bounds;
        
        this.root = (root == null ? this : root);
        this.parent = parent;
    }
    
    /**
     * @return the parent sheet of {@code this}.
     */
    protected Sheet getParent() {
        return parent;
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
     * @return whether this sheet and all its children have at least one
     *     filled part.
     */
    protected boolean hasFilled() {
        if (!filled.isEmpty()) return true;
        
        for (Sheet child : children) {
            if (child.hasFilled()) return true;
        }
        
        return true;
    }
    
    /**
     * Checks recusively whether the given rectangle
     * can be placed at this sheet.
     * 
     * @param rec the rectangle to be added. Must be withing {@code bounds}.
     * @return if {@code rec} is allowed to be placed a list containing
     *     all sheets that should be modified if {@code rec} is placed.
     *     {@code null} otherwise.
     */
    protected List<Sheet> check(Rectangle rec) {
        // Check if the rectangle is allowed to be placed.
        for (Rectangle filledRec : filled) {
            if (!rec.intersection(filledRec).isEmpty()) return null;
        }
        
        if (children.isEmpty()) {
            /** If this is an end sheet, set the update rectangle
                for later when the rectangle is updated via {@link fill()}.
                Note that it is known here that the intersection of {@code rec}
                and {@code bounds} is not empty. */
            update = rec;
            List<Sheet> mod = new ArrayList<Sheet>();
            mod.add(this);
            
            return mod;
            
        } else if (rec.equals(bounds)) {
            /** If {@code rec} fully contains this sheet, check if there is
                doesn't exist a filled area in this sheet. If so, return
                a list containing {@code this} and update the update rectangle.
                If not, return {@code null}. */
            if (hasFilled()) {
                return null;
                
            } else {
                List<Sheet> mod = new ArrayList<Sheet>();
                mod.add(this);
                update = rec;
                return mod;
            }
            
        } else {
            /** Here it is known that the sheet has no children and is not
                fully contained. Propagate the problem to the children.
                If none finds a violation, return all sheets that should be
                changed. Otherwise return null. */
            List<Sheet> mod = new ArrayList<Sheet>();
            
            // Test for every child whether the current rectangle is allowed.
            // Store the modified children afterwards.
            for (Sheet child : children) {
                Rectangle checkRec = rec.intersection(child.getBounds());
                if (!checkRec.isEmpty()) {
                    List<Sheet> checkResults = child.check(checkRec);
                    if (checkResults == null) return null;
                    mod.addAll(checkResults);
                }
            }
            
            return mod;
        }
    }
    
    /**
     * Tries to find an empty sheet to place {@code rec}.
     * If a sheet was found, check if the location is legal.
     * 
     * @param rec the size of the rectangle to check for.
     * @return if {@code rec} can be placed at a legal location, returns
     *     a list containing all sheets that should be updated when
     *     {@code rec} is placed at its current location. Returns {@code null}
     *     otherwise.
     */
    protected List<Sheet> put(Rectangle rec) {
        if (children.isEmpty()) {
            // If the sheet has no children.
            // Update the location of the rectangle and check if it is valid.
            rec.setLocation(bounds.x, bounds.y);
            // Check if the rectangle is still withing the root sheet.
            if (!root.getBounds().contains(rec)) return null;
            return root.check(rec);
            
        } else {
            // Try to find a location for the rectangle.
            // If a child has found a valid location, return that location.
            List<Sheet> mod;
            for (Sheet child : children) {
                if ((mod = child.put(rec)) != null) {
                    return mod;
                }
            }
            
            return null;
        }
    }
    
    /**
     * This function is called by a child sheet of this sheet when it is full.
     * The full sheet is removed from the child list and the are is marked as
     * filled instead.
     * 
     * @param fullSheet a child sheet that is full.
     */
    protected void notifyFull(Sheet fullSheet) {
        // If no child was removed, simply return and ignore the call.
        if (!children.remove(fullSheet)) return;
        
        // Set the sheet to full.
        fullSheet.full = true;
        
        /** If there are now no more children, we know that the sheet is full
            since if there once were children, then all the available parts of
            this sheet have been distributed among them. So if all of them are
            full, so is this sheet. */
        if (children.isEmpty()) {
            full = true;
            parent.notifyFull(this);
            
        } else {
            // Otherwise simply add a full area to the sheet.
            filled.add(fullSheet.getBounds());
        }
    }
    
    /**
     * Checks and updates whether this sheet is full.
     * If the sheet is full, notify the parent sheet of this.
     */
    protected void updateFull() {
        // If the sheet contains children, it certainly is not full.
        if (!children.isEmpty()) return;
        
        // Calculate the area covered by the filled rectangles.
        int area = 0;
        for (Rectangle rec : filled) {
            area += rec.width * rec.height;
        }
        
        /** Check if the area covered by the filled rectangles equals
            the total area. If so, notify the parent sheet.
            Note that this only works since overlapping rectangles
            will never occur. */
        if (area == bounds.width * bounds.height) {
            parent.notifyFull(this);
        }
    }
    
    /**
     * Fills this sheet, using {@code update} obtained via
     * {@link check(Rectangle)}.
     * 
     * @throws IllegalStateException iff
     *     the lower left point of {@code update} does not match both
     *     the X and Y coordinates of the current rectangle.
     * 
     * Note that it is known here that {@code update} is a valid placement.
     * (so {@code update} is within {@code bounds} and is not empty).
     */
    protected void fill() {
        if (children.isEmpty()) {
            boolean pX1 = update.x == bounds.x;
            boolean pY1 = update.y == bounds.y;
            boolean pX2 = update.x + update.width == bounds.x + bounds.width;
            boolean pY2 = update.y + update.height == bounds.y + bounds.height;
            
            if (!pX1 && !pY1) {
                throw new IllegalStateException("The update rectangle does "
                        + "not start at the lower left edge of the sheet!");
            }
            
            ArrayList<Rectangle> addSheets = new ArrayList<Rectangle>();
            
            // Determine the bounds of the child sheets.
            if (!pX1) { // ==> pY1 == true
                addSheets.add(new Rectangle(
                        bounds.x,
                        bounds.y,
                        update.x - bounds.x,
                        bounds.height));
            }
            
            if (!pY1) { // ==> pX1 == true
                addSheets.add(new Rectangle(
                        bounds.x,
                        bounds.y,
                        update.width,
                        update.y - bounds.y));
            }
            
            if (!pY2) {
                addSheets.add(new Rectangle(
                        bounds.x,
                        bounds.y + update.height,
                        update.width,
                        bounds.height - update.height));
            }
            
            if (!pX2) {
                addSheets.add(new Rectangle(
                        bounds.x + update.width,
                        bounds.y,
                        bounds.width,
                        bounds.height));
            }
            
            // Create and add all child sheets.
            for (Rectangle add : addSheets) {
                children.add(new Sheet(add, root, this));
            }
            
            // Mark the filled update part as filled.
            filled.add(update);
            // Check whether the current sheet is filled.
            updateFull();
            
        } else {
            parent.notifyFull(this);
        }
    }
    
    /**
     * Tries to put the entry in the current sheet.
     * If this sheet has children, distribute the placing of the entry
     * to them using {@link check(Rectangle)} and {@code fill()}.
     * 
     * @param entry the entry that should be added.
     * @return whether the entry could be added.
     * 
     * Note that the previous location of the entry is NOT taken into account
     * and will be modified to the location of where the rectangle has been
     * placed.
     */
    public boolean add(Dataset.Entry entry) {
        Rectangle entryRec = entry.getRec();
        
        List<Sheet> mod = put(entry.getRec());
        if (mod == null) return false;
        
        for (Sheet modSheet : mod) {
            modSheet.fill();
        }
        
        return true;
    }

    public Dataset pack(Dataset dataset) {
        Dataset clone = dataset.clone();
        clone.setSize(this.bounds.width, this.bounds.height);
        for (Dataset.Entry entry : clone) {
            if (!add(entry)) {
                return null;
            }
        }
        return clone;
    }
}
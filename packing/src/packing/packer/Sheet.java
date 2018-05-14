
package packing.packer;


// Packing imports
import packing.data.*;
import packing.tools.*;


// Java imports
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;


/* 
 * This class keeps track of what part of it is filled and what part isn't.
 */
public class Sheet extends Packer {
    // The size and offset of the sheet.
    protected Rectangle bounds;
    
    // Whether the sheet is completely full.
    protected boolean full;
    
    // The children of this sheet. All entries MUST be ordered in the same
    // way they are visited for checking where to add a rectangle.
    final protected List<Sheet> children = new ArrayList<Sheet>();
    final protected List<Rectangle> filled = new ArrayList<Rectangle>();
    
    // The root sheet of the sheet tree.
    protected Sheet root;
    // The parent sheet of this sheet.
    protected Sheet parent;
    // The sheet on the left of this sheet. {@code null} if it doesn't exist.
    
    // These 4 sheets represent the neighbours of this sheet.
    // Note that these may be an indirect neighbor.
    protected Sheet left;
    protected Sheet down;
    protected Sheet up;
    protected Sheet right;
    
    // The rectangle that should be filled when {@link fill()} is called.
    protected Rectangle update = null;
    
    
    /**-------------------------------------------------------------------------
     * Constructors.
     * -------------------------------------------------------------------------
     */
    /**
     * Constructs a new main sheet with the given width and height.
     * Uses {@link Sheet#Sheet(Rectangle)}.
     * 
     * @param width
     * @param height 
     */
    public Sheet(int width, int height) {
        this(new Rectangle(width, height));
    }
    
    /**
     * Constructs a new main sheet.
     * Uses {@link Sheet#Sheet(Rectangle, Sheet, Sheet, Sheet, Sheet)}.
     * 
     * @param rec the bounds of this sheet.
     */
    public Sheet(Rectangle bounds) {
        this(bounds, null, null, null, null);
    }
    
    /**
     * Constructs a new Sheet.
     * 
     * @param rec the bounds of this sheet.
     * @param root the root of all sheets.
     * @param parent the parent sheet of this sheet. {@code null} if none.
     */
    protected Sheet(Rectangle bounds, Sheet root, Sheet parent,
            Sheet left, Sheet down) {
        this.bounds = bounds;
        
        this.root = (root == null ? this : root);
        this.parent = parent;
        this.left = left;
        this.down = down;
    }
    
    
    /**-------------------------------------------------------------------------
     * Get/set functions.
     * -------------------------------------------------------------------------
     */
    /**
     * @return the bounds of this sheet.
     */
    public Rectangle getBounds() {
        return bounds;
    }
    
    /**
     * Sets the bounds of the sheet.
     * 
     * @param newBounds the new bounds of this sheet.
     */
    public void setBounds(Rectangle newBounds) {
        if (newBounds == null)
            throw new NullPointerException("Bounds cannot be null!");
        bounds = newBounds;
    }
    
    
    /**
     * @return the parent sheet of {@code this}.
     */
    public Sheet getParent() {
        return parent;
    }
    
    /**
     * Sets the parent of this sheet.
     * Also updates the root accordingly.
     * 
     * @param newParent the new parent of ths sheet.
     */
    public void setParent(Sheet newParent) {
        parent = newParent;
        
        root = (parent == null
                    ? null
                    : parent.root);
    }
    
    
    /**
     * @return the sheet that is on the left of this sheet.
     * 
     * Note that the returned sheet might be a neighbor higher in
     * the hierarchy, instead of a direct neighbor.
     */
    public Sheet getLeft() {
        return left;
    }
    
    /**
     * Sets the left sheet.
     * 
     * @param newLeft the new left sheet.
     */
    public void setLeft(Sheet newLeft) {
        left = newLeft;
    }
    
    
    /**
     * @return the sheet that is below this sheet.
     * 
     * Note that the returned sheet might be a neighbor higher in
     * the hierarchy, instead of a direct neighbor.
     */
    public Sheet getDown() {
        return down;
    }
    
    /**
     * Sets the down sheet.
     * 
     * @param newDown the new down sheet.
     */
    public void setDown(Sheet newDown) {
        down = newDown;
    }
    
    
    /**
     * @return the sheet that is above this sheet.
     * 
     * Note that the returned sheet might be a neighbor higher in
     * the hierarchy, instead of a direct neighbor.
     */
    public Sheet getUp() {
        return up;
    }
    
    /**
     * Sets the upper sheet.
     * 
     * @param newUp the new up sheet.
     */
    public void setUp(Sheet newUp) {
        up = newUp;
    }
    
    
    /**
     * @return the sheet that is on the right of this sheet.
     * 
     * Note that the returned sheet might be a neighbor higher in
     * the hierarchy, instead of a direct neighbor.
     */
    public Sheet getRight() {
        return right;
    }
    
    /**
     * Sets the right sheet.
     * 
     * @param newRight the new right sheet.
     */
    public void setRight(Sheet newRight) {
        right = newRight;
    }
    
    
    /**
     * @return the children of the sheet.
     */
    public List<Sheet> getChildren() {
        return children;
    }
    
    /**
     * @return a list containing all filled rectangles of this sheet.
     */
    public List<Rectangle> getFilled() {
        return filled;
    }
    
    /**
     * @return whether the entire sheet is full.
     */
    public boolean isFull() {
        return full;
    }
    
    
    /**
     * @return whether this sheet and all its children have at least one
     *     filled part.
     */
    public boolean isEmpty() {
        if (!filled.isEmpty()) return false;
        
        for (Sheet child : children) {
            if (!child.isEmpty()) return false;
        }
        
        return true;
    }
    
    
    /**-------------------------------------------------------------------------
     * Functions.
     * -------------------------------------------------------------------------
     */
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
                for later when the rectangle is updated via
                {@link Sheet#fill()}. Note that it is known here that the
                intersection of {@code rec} and {@code bounds} is not empty. */
            update = rec;
            List<Sheet> mod = new ArrayList<Sheet>();
            mod.add(this);
            
            return mod;
            
        } else if (rec.equals(bounds)) {
            /** If {@code rec} fully contains this sheet, check if there
                doesn't exist a filled area in this sheet. If so, return
                a list containing {@code this} and update the update rectangle.
                If not, return {@code null}. */
            if (!isEmpty()) {
                return null;
                
            } else {
                List<Sheet> mod = new ArrayList<Sheet>();
                mod.add(this);
                update = bounds;
                return mod;
            }
            
        } else {
            /** Here it is known that the sheet has no children and is not
                fully contained by {@code rec}. Propagate the problem to the
                children. If none finds a violation, return all sheets
                that should be changed. Otherwise return null. */
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
        fullSheet.children.clear();
        fullSheet.filled.clear();
        
        /** If there are now no more children, we know that the sheet is full
            since if there once were children, then all the available parts of
            this sheet have been distributed among them. So if all of them are
            full, so is this sheet. */
        if (children.isEmpty()) {
            if (parent != null) {
                parent.notifyFull(this);
                
            } else {
                full = true;
                filled.clear();
            }
            
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
                        + "not start at either the lower or the left edge "
                        + "of the sheet!");
            }
            
            // Determine and add the child sheets.
            Sheet leftSheet = (pX1
                    ? null
                    : addLeftSheet(this, getLeft(), getDown()));
            
            Sheet middleSheet = addMiddleSheet(this, leftSheet, getDown(),
                    !pY1, !pY2);
            
            if (!pX2) addRightSheet(this, middleSheet, getDown());
            
            // Check whether the current sheet is filled.
            updateFull();
            
            /** Add cuts at the sheets below and on the left, using
                the lower right corner as start point for the line downwards
                and the upper left corner for the line to the left. Continues
                until the line hits a filled area. */
            
            if (left != null) left.addCut(new HalfLine(
                    update.x,
                    update.y + update.height,
                    HalfLine.Direction.LEFT));
            
            if (down != null) down.addCut(new HalfLine(
                    update.x + update.width,
                    update.y,
                    HalfLine.Direction.DOWN));
            
        } else {
            parent.notifyFull(this);
        }
    }
    
    /**
     * Creates and adds a sheet that is on the left of the update rectangle.
     * +---+
     * |#  |
     * |#* |
     * |#  |
     * +---+
     * 
     * @param parent the parent of the sheet to be created.
     * @param leftSheet the left sheet of the sheet to be created.
     * @param lowerSheet the lower sheet of the sheet to be created.
     * @return the created sheet.
     */
    private Sheet addLeftSheet(Sheet parent, Sheet leftSheet,
            Sheet lowerSheet) {
        Sheet sheet = new Sheet(new Rectangle(
                parent.bounds.x,
                parent.bounds.y,
                parent.update.x - bounds.x,
                parent.bounds.height),
            root, parent, leftSheet, lowerSheet);
        
        parent.children.add(sheet);
        return sheet;
    }
    
    /**
     * Creates and adds a sheet that is in the middle of the update rectangle.
     * +---+
     * | # |
     * | * |
     * | # |
     * +---+
     * 
     * @param parent the parent of the sheet to be created.
     * @param leftSheet the left sheet of the sheet to be created.
     * @param lowerSheet the lower sheet of the sheet to be created.
     * @param useDown whether to the middle sheet should include the area
     *     below the update rectangle.
     * @param useUp whether to the middle sheet should include the area
     *     above the update rectangle.
     * @return the created sheet. Returns {@code null} iff neither
     *     {@code useUp} and {@code useDown} and adds the update to
     *     {@code filled} of {@code parent}.
     * 
     * Uses {@link Sheet#addLowerSheet(Sheet, Sheet, Sheet)} and 
     * {@link Sheet#addUpperSheet(Sheet, Sheet, Sheet)}.
     */
    private Sheet addMiddleSheet(Sheet parent, Sheet leftSheet,
            Sheet lowerSheet, boolean useDown, boolean useUp) {
        if (leftSheet == null) leftSheet = getLeft();
        
        if (useUp && useDown) {
            // If both up and down should be added, an extra sheet must be added
            // to contain both parts.
            Sheet middleSheet = new Sheet(new Rectangle(
                    parent.update.x,
                    parent.bounds.y,
                    parent.update.width,
                    parent.bounds.height),
                root, parent, leftSheet, getDown());
            middleSheet.update = parent.update;

            addLowerSheet(middleSheet, leftSheet, lowerSheet);
            addUpperSheet(middleSheet, leftSheet, null);
            // Add the update to the middle sheet instead of
            // the parent sheet and return the middle sheet.
            middleSheet.filled.add(update);
            parent.children.add(middleSheet);
            return middleSheet;
        }
        
        // In the remaining cases, the update should always be added
        // to the parent sheet.
        parent.filled.add(update);
        
        if (useUp) {
            return addUpperSheet(parent, leftSheet, lowerSheet);
        }
        
        if (useDown) {
            return addLowerSheet(parent, leftSheet, lowerSheet);
        }
        
        // In all other cases, no middle sheet should be created.
        return null;
    }
    
    /**
     * Creates and adds a sheet that is above of the update rectangle.
     * +---+
     * |   |
     * | * |
     * | # |
     * +---+
     * 
     * @param parent the parent of the sheet to be created.
     * @param leftSheet the left sheet of the sheet to be created.
     * @param lowerSheet the lower sheet of the sheet to be created.
     * @return the created sheet.
     */
    private Sheet addLowerSheet(Sheet parent, Sheet leftSheet,
            Sheet lowerSheet) {
        Sheet sheet = new Sheet(new Rectangle(
                parent.update.x,
                parent.bounds.y,
                parent.update.width,
                parent.update.y - parent.bounds.y),
            root, parent, leftSheet, lowerSheet);
        
        parent.children.add(sheet);
        return sheet;
    }
    
    /**
     * Creates and adds a sheet that is below of the update rectangle.
     * +---+
     * | # |
     * | * |
     * |   |
     * +---+
     * 
     * @param parent the parent of the sheet to be created.
     * @param leftSheet the left sheet of the sheet to be created.
     * @param lowerSheet the lower sheet of the sheet to be created.
     * @return the created sheet.
     */
    private Sheet addUpperSheet(Sheet parent, Sheet leftSheet,
            Sheet lowerSheet) {
        Sheet sheet = new Sheet(new Rectangle(
                parent.update.x,
                parent.update.y + parent.update.height,
                parent.update.width,
                (parent.bounds.y + parent.bounds.height)
                        - (parent.update.y + parent.update.height)),
            root, parent, leftSheet, lowerSheet);
        
        parent.children.add(sheet);
        return sheet;
    }
    
    /**
     * Creates and adds a sheet that is on the right of the update rectangle.
     * +---+
     * |  #|
     * | *#|
     * |  #|
     * +---+
     * 
     * @param parent the parent of the sheet to be created.
     * @param leftSheet the left sheet of the sheet to be created.
     * @param lowerSheet the lower sheet of the sheet to be created.
     * @return the created sheet.
     */
    private Sheet addRightSheet(Sheet parent, Sheet leftSheet,
            Sheet lowerSheet) {
        Sheet sheet = new Sheet(new Rectangle(
                parent.update.x + parent.update.width,
                parent.bounds.y,
                (parent.bounds.x + parent.bounds.width)
                        - (parent.update.x + parent.update.width),
                parent.bounds.height),
            root, parent, leftSheet, lowerSheet);
        
        parent.children.add(sheet);
        return sheet;
    }
    
    /**
     * Adds an extra cut to the sheet.
     * Distributes the cuts to its children.
     * If no children available, cut the sheet according to the line.
     * If the line has not yet hit a filled area, continue to the
     * left/lower neighbor.
     * 
     * @param line the cut line.
     * @param move whether the this sheet should ask its neighbor in the
     *     direction of the line to continue the cut.
     * @return whether a filled area was reached. Also returns {@code false}
     *     if the line does not intersect the child.
     */
    public boolean addCut(HalfLine line) {
        return addCut(line, true);
    }
    
    protected boolean addCut(HalfLine line, boolean move) {
        if (!line.intersects(bounds)) return false;
        if (full) return true;
        
        if (!children.isEmpty()) {
            // Determine the first intersection coord value.
            // This value represents the first intersection
            int firstInterCoord = Integer.MIN_VALUE;
            for (Rectangle fill : filled) {
                if (line.intersects(fill)) {
                    // Calculate the new coordinate value.
                    int newCoord = Integer.MIN_VALUE;
                    if (line.dir == HalfLine.Direction.LEFT)
                        newCoord = fill.x + fill.width;
                    else if (line.dir == HalfLine.Direction.DOWN)
                        newCoord = fill.y + fill.height;
                    
                    if (firstInterCoord > newCoord) {
                        firstInterCoord = newCoord;
                    }
                }
            }
            
            // Iterate in reverse order to get the children in the order
            // right to left, then up to down.
            for (int i = children.size() - 1; i >= 0; i--) {
                Sheet child = children.get(i);
                
                // Calculate the child coordinate value.
                int childCoord = Integer.MIN_VALUE;
                if (line.dir == HalfLine.Direction.LEFT)
                    childCoord = child.bounds.x + child.bounds.width;
                else if (line.dir == HalfLine.Direction.DOWN)
                    childCoord = child.bounds.y + child.bounds.height;
                
                // Check if there is a filled area before the child.
                if (childCoord > firstInterCoord) {
                    if (child.addCut(line, move && i == 0)) {
                        return true;
                    }
                }
            }
            
            return firstInterCoord != Integer.MIN_VALUE;
            
        } else {
            // Note that a sheet that doesn't have children is either
            // completely full or empty.
            
            if (line.dir == HalfLine.Direction.LEFT) {
                if (line.y == bounds.y || line.y == bounds.y + bounds.height) {
                    return false;
                }
                
                update = new Rectangle(bounds.x, line.y, bounds.width, 0);
                
                Sheet lowerSheet = (update.y == bounds.y
                        ? getDown()
                        : addLowerSheet(this, getLeft(), getDown()));
                
                if (update.y != bounds.y + bounds.height) {
                    addUpperSheet(this, getLeft(), lowerSheet);
                }
                
            } else if (line.dir == HalfLine.Direction.DOWN) {
                if (line.x == bounds.x || line.x == bounds.x + bounds.width) {
                    return false;
                }
                
                update = new Rectangle(line.x, bounds.y, 0, bounds.height);
                
                Sheet leftSheet = (update.x == bounds.x
                        ? getLeft()
                        : addLeftSheet(this, getLeft(), getDown()));
                
                if (update.x != bounds.x + bounds.width) {
                    addRightSheet(this, leftSheet, getDown());
                }
            }
        }
        
        return false;
    }
    
    /**
     * Tries to add the entry in the current sheet.
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
        List<Sheet> mod = put(entry.getRec());
        if (mod == null) return false;
        
        for (Sheet modSheet : mod) {
            modSheet.fill();
        }
        
        return true;
    }
    
    /**
     * Packs the given rectangles in the given box.
     * 
     * @param dataset the rectangles and the box data.
     * @return the new locations of the rectangles within the box.
     *     Returns {@code null} if no solution could be found.
     */
    @Override
    public Dataset pack(Dataset dataset) {
        Dataset clone = dataset.clone();
        
        for (Dataset.Entry entry : clone.sorted()) {
            if (!add(entry)) {
                if (dataset.allowRotation()) {
                    entry.setRotation(!entry.useRotation());
                }
                return null;
            }
        }
        
        return clone;
    }
    
    
    /**
     * @param rec the rectangle to give the representation of.
     * @return a simple String representation of {@code rec}.
     */
    public String recToString(Rectangle rec) {
        return "[x=" + rec.x + ", y=" + rec.y + ", width=" + rec.width
                + ", height=" + rec.height + "]";
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[box:" + recToString(bounds) + ", ");
        
        sb.append("filled:[");
        String[] fillBoxes = new String[filled.size()];
        for (int i = 0; i < filled.size(); i++) {
            fillBoxes[i] = recToString(filled.get(i));
        }
        sb.append(String.join(", ", fillBoxes) + "], ");
        
        sb.append("child boxes:[");
        String[] childBoxes = new String[children.size()];
        for (int i = 0; i < children.size(); i++) {
            childBoxes[i] = recToString(children.get(i).bounds);
        }
        sb.append(String.join(", ", childBoxes) + "]]");
        
        return sb.toString();
    }
    
    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        for (StringBuilder line : toTreeStringList()) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
        }
        
        return sb.toString();
    }
    
    public List<StringBuilder> toTreeStringList() {
        List<StringBuilder> list = toTreeStringList(' ', ' ');
        list.remove(0);
        return list;
    }
    
    private List<StringBuilder> toTreeStringList(char treeChar,
            char spacingChar) {
        List<StringBuilder> list = new ArrayList<StringBuilder>();
        
        list.add(new StringBuilder().append(treeChar));
        list.add(new StringBuilder("[" + bounds.width + "x" + bounds.height
                + "@" + bounds.x + "," + bounds.y + "]"));
        
        int curSpacing = 0;
        for (int i = 0; i < children.size(); i++) {
            char childTree = '┬';
            char childSpacing = '─';
            
            if (children.size() == 1) {
                childTree = '│';
                childSpacing = ' ';
                
            } else {
                if (i == 0) {
                    childTree = '├';
                    
                } else if (i == children.size() - 1) {
                    childTree = '┐';
                    childSpacing = ' ';
                }
            }
            
            char spacing = (i % 2 == 0
                    ? childSpacing
                    : ' ');
            
            List<StringBuilder> childList = children.get(i)
                    .toTreeStringList(childTree, childSpacing);
            
            int longestDist = 0;
            for (int j = 0; j < childList.size(); j++) {
                StringBuilder elem = childList.get(j);
                
                // Update the longest distance.
                if (elem.length() > longestDist) {
                    longestDist = elem.length();
                }
                
                if (j + 2 > list.size() - 1) {
                    StringBuilder newSb = new StringBuilder();
                    
                    newSb.append(MultiTool.fill(' ', curSpacing));
                    newSb.append(elem);
                    list.add(newSb);
                    
                } else {
                    StringBuilder listElem = list.get(j + 2);
                    int length = listElem.length();
                    
                    char leftSpacing = (j % 2 == 0
                            ? (j == 0 ? '─' : ' ')
                            : ' ');
                    listElem.append(MultiTool
                            .fill(leftSpacing,
                                    curSpacing - length));
                    listElem.append(elem);
                }
            }
                
            curSpacing += longestDist;
        }
        
        
        list.get(0).append(MultiTool.fill(spacingChar, curSpacing - 2));
        
        return list;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Sheet)) return false;
        if (obj == this) return true;
        
        Sheet sheet = (Sheet) obj;
        return bounds.equals(sheet.bounds) &&
               boundCheck(root,   sheet.root  ) &&
               boundCheck(parent, sheet.parent) &&
               boundCheck(left,   sheet.left  ) &&
               boundCheck(down,   sheet.down  ) &&
               boundCheck(up,     sheet.up    ) &&
               boundCheck(right,  sheet.right ) &&
               filled.equals(sheet.filled) &&
               children.size() == sheet.children.size() &&
               full == sheet.full;
    }
    
    /**
     * Checks whether the bounds of two sheets are equal.
     * 
     * @param s1 sheet 1 to compare.
     * @param s2 sheet 2 to compare.
     * @return if {@code s1} == {@code s2} == {@code null}, return true.
     *     If either {@code s1} or {@code s2} equal {@code null}, return false.
     *     Otherwise return whether the bounds of {@code s1} equal the bounds
     *     of {@code s2.
     */
    private static boolean boundCheck(Sheet s1, Sheet s2) {
        if (s1 == s2) return true;
        if (s1 == null || s2 == null) return false;
        return s1.bounds.equals(s2.bounds);
    }
    
    /**
     * Checks whether {@code obj} is equal to {@code this} using a deep check.
     * 
     * @param obj the object to check.
     * @return wiether {@code obj} is deep equal to {@code this}.
     * 
     * WARNING: use this function with caution, as it might cause
     * infinite loops when {@code left}, {@code down}, {@code up} or
     * {@code right} of either objects are refferences to sheets higher
     * in the hierarchy. The same holds for ALL direct and indirect children
     * of both object ({@code this} and {@code obj}).
     */
    public boolean deepEquals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Sheet)) return false;
        if (obj == this) return true;
        
        Sheet sheet = (Sheet) obj;
        return bounds  .equals(sheet.bounds) &&
               root    .equals(sheet.root) &&
               parent  .equals(sheet.parent) &&
               left    .deepEquals(sheet.left) &&
               down    .deepEquals(sheet.down) &&
               up      .deepEquals(sheet.up) &&
               right   .deepEquals(sheet.right) &&
               filled  .equals(sheet.filled) &&
               children.equals(sheet.children) &&
               full == sheet.full;
    }
    
    @Override
    public int hashCode() {
        // Note that the sheets like {@code root}, {@code parent} and
        // {@code left} are NOT FULLY taken into account to prevent recursion.
        return MultiTool.calcHashCode(bounds, full, filled, children.size(),
                (left == null), (down == null), (up == null), (right == null),
                (root == null), (parent == null));
    }
    
    final public static String FS = System.getProperty("file.separator");
    final public static String LS = System.getProperty("line.separator");
    
    // tmp
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir")
                + FS + "src" + FS + "log.log";
        System.err.println("Logfile: " + fileName);
        Logger.setDefaultLogger(new FileLogger(fileName));
        Logger.setLogHeader("Date: &date&" + LS);
        
        HalfLine downLine = new HalfLine(1, 10, HalfLine.Direction.DOWN);
        HalfLine downLine2 = new HalfLine(2, 10, HalfLine.Direction.DOWN);
        HalfLine leftLine = new HalfLine(10, 2, HalfLine.Direction.LEFT);
        Rectangle rec = new Rectangle(10, 10);
        Rectangle fill = new Rectangle(2, 2);
        Sheet main = new Sheet(rec);
        
        /*
        Sheet main = new Sheet(rec);
        main.addCut(downLine);
        main.addCut(downLine2);
        main.addCut(leftLine);
        
        
        tmp.Logger.write(ls + main.toTreeString());
        */
        
        
        Rectangle[] input = new Rectangle[] {
            /*
            new Rectangle(1, 1),
            new Rectangle(2, 1),
            new Rectangle(2, 8),
            new Rectangle(9, 1),
            new Rectangle(8, 9)
            /*
            new Rectangle(1, 5),
            new Rectangle(1, 6),
            new Rectangle(2, 1)
            /**/
            new Rectangle(1, 5),
            new Rectangle(2, 5),
            new Rectangle(1, 6)
            /**/
        };
        
        
        for (Rectangle in : input) {
            List<Sheet> mod = main.put(in);
            for (Sheet modSheet : mod) {
                modSheet.fill();
            }
            Logger.write(LS + main.toTreeString());
        }
        
        System.out.println(main);
        System.out.println(main.full);
        
        /*
        System.out.println("1: " + main);
        MultiTool.sleepThread(10);
        main.addCut(downLine);
        MultiTool.sleepThread(10);
        System.out.println("2: " + main);
        MultiTool.sleepThread(10);
        main.addCut(leftLine);
        MultiTool.sleepThread(10);
        System.out.println("3: " + main);
        System.out.println("3.left: " + main.children.get(0));
        System.out.println("3.right: " + main.children.get(1));
        main.put(fill);
        System.out.println("3: " + main);
        System.out.println("3.left: " + main.children.get(0));
        System.out.println("3.right: " + main.children.get(1));*/
        
    }
}
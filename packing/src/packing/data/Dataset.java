
package packing.data;


// Packing imports
import packing.tools.*;


// Java imports
import java.awt.Rectangle;
import java.util.*;
import java.util.function.Predicate;


/* 
 * Abstract dataset for the rectangles and additional parameters.
 */
public class Dataset
        implements Iterable<Dataset.Entry>, packing.tools.Cloneable {
    /* -------------------------------------------------------------------------
     * Variables
     * -------------------------------------------------------------------------
     */
    // Whether the height of the sheet is fixed.
    final protected boolean fixedHeight;
    
    // Whether rotation is allowed or not.
    final protected boolean allowRot;
    
    // The number of rectangle.
    final protected int numRect;
    
    // The number for keeping track of the number
    // of elements relative the input.
    protected int idCounter = 0;
    
    // The width and height of the sheet.
    protected int width;
    protected int height;
    
    // The list of entries
    protected List<Dataset.Entry> list = new ArrayList<>();
    
    
    
    /* -------------------------------------------------------------------------
     * Entry class
     * -------------------------------------------------------------------------
     */
    public class Entry
            extends CompareEntry
            implements packing.tools.Cloneable {
        // The rectangle.
        final private Rectangle rec;
        
        // The rotated rectangle.
        private Rectangle rotatedRec;
        
        // Whether to use rotation by default.
        private boolean useRotation = false;
        
        
        /**
         * Default constructor.
         * 
         * @param rec the rectangle denoting the size and offset of the entry.
         * @param id number denoting the order of occurance in the input.
         */
        public Entry(Rectangle rec, int id) {
            super(id);
            this.rec = rec;
        }
        
        /**
         * Clone constructor.
         * 
         * @param clone entry to be cloned from.
         */
        public Entry(Entry clone) {
            super(clone.id);
            this.useRotation = clone.useRotation;
            this.rec = (Rectangle) clone.getNormalRec().clone();
            if (clone.rotatedRec != null) this.rotatedRec
                    = (Rectangle) clone.rotatedRec.clone();
            
        }
        
        
        /**
         * Ensures that the rotated rectangle is calculated.
         * @throws IllegalStateException iff rotations are not allowed.
         */
        private void calcRotatedRec()
                throws IllegalStateException {
            if (rotatedRec != null) return;
            
            if (Dataset.this.allowRot) {
                rotatedRec = new Rectangle(rec.y, rec.x, rec.height, rec.width);
                
            } else {
                throw new IllegalStateException("Rotation is not allowed.");
            }
        }
        
        /**
         * @return the rectangle {@code rec}.
         */
        public Rectangle getNormalRec() {
            return rec;
        }
        
        /**
         * @return the rotated version of rec.
         * @throws IllegalStateException iff rotations are not allowed.
         */
        public Rectangle getRotatedRec()
                throws IllegalStateException {
            calcRotatedRec();
            return rotatedRec;
        }
        
        /**
         * @return the rectangle depending on the default rotation.
         */
        @Override
        public Rectangle getRec() {
            return (useRotation
                        ? getRotatedRec()
                        : getNormalRec());
        }
        
        /**
         * @return the default rotation of the entry.
         */
        public boolean useRotation() {
            return useRotation;
        }
        
        /**
         * Sets the default rotation of the entry.
         * 
         * @param rotation whether the entry is rotated by default.
         * @throws IllegalStateException iff
         *     {@code rotation == true} and rotations are not allowed.
         */
        public void setRotation(boolean rotation) {
            if (rotation && !Dataset.this.allowRot)
                throw new IllegalStateException("Rotation is not allowed.");
            useRotation = rotation;
        }
        
        /**
         * Rotates the entry.
         */
        public void rotate() {
            if (!Dataset.this.allowRot)
                throw new IllegalStateException("Rotation is not allowed.");
            useRotation = !useRotation;
        }
        
        /* 
         * @return the area of the rectangle.
         */
        public int area() {
            return rec.x * rec.y;
        }
        
        /* 
         * Sets the location of the rectangle.
         * @param x the new x coord.
         * @param y the new y coord.
         */
        public void setLocation(int x, int y) {
            rec.setLocation(x, y);
            if (rotatedRec != null) {
                rotatedRec.setLocation(x, y);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Dataset.Entry)) return false;
            Entry entry = (Entry) obj;
            
            return rec.equals(entry.rec) && useRotation;
        }
        
        @Override
        public int hashCode() {
            return MultiTool.calcHashCode(rec.x, rec.y, rec.width, rec.height,
                                          useRotation);
        }
        
        @Override
        public String toString() {
            return "[rec: [x=" + rec.x + ", y=" + rec.y + ", width="
                    + rec.width + ", height=" + rec.height + "], "
                    + "rotation: " + useRotation + "]";
        }
        
        @Override
        public Entry clone() {
            return new Entry(this);
        }
        
    }
    
    
    /* -------------------------------------------------------------------------
     * Constructor
     * -------------------------------------------------------------------------
     */
    /* 
     * @param rotation whether to allow rotation.
     * @param the height restriction. Use -1 for no height restriction.
     * @param numRect the total number of rectangles.
     * @param gen the generator to be used on this dataset.
     */
    public Dataset(int height, boolean rotation, int numRect) {
        this.fixedHeight = height != -1;
        this.allowRot = rotation;
        this.numRect = numRect;

        if (fixedHeight) this.height = height;
    }
    
    public Dataset(Dataset clone) {
        this((clone.fixedHeight ? clone.height : -1),
                clone.allowRot, clone.numRect);
        this.idCounter = clone.idCounter;
        this.width = clone.width;
        this.height = clone.height;
        
        for (Dataset.Entry entry : clone.list) {
            list.add(entry.clone());
        }
    }
    
    
    /* -------------------------------------------------------------------------
     * Functions
     * -------------------------------------------------------------------------
     */
    /**
     * Adds an entry to the data set.
     *
     * @param rec the rectangle to be added.
     */
    public void add(Rectangle rec) {
        list.add(new Dataset.Entry(rec, idCounter++));
    }
    
    /**
     * Removes the provided rectangle from the entry list.
     * 
     * @param rec the rectangle to be removed.
     */
    public void remove(Rectangle rec) {
        list.remove(new Dataset.Entry(rec, idCounter--));
    }
    
    @Override
    public Iterator<Entry> iterator() {
        return list.iterator();
    }
    
    /**
     * @return the i'th object that was added.
     */
    public Dataset.Entry get(int i) {
        return list.get(i);
    }
    
    @Override
    public String toString() {
        return "[allowRot=" + allowRot + ", width=" + width
                + ", height=" + height + ", numRect=" + numRect
                + " set=" + list.toString() + "]";
    }
    
    /**
     * @return whether the height is fixed.
     */
    public boolean isFixedHeight() {
        return fixedHeight;
    }
    
    /**
     * @return whether rotations are allowed.
     */
    public boolean allowRotation() {
        return allowRot;
    }
    
    /**
     * @return the number of rectangles in the dataset.
     */
    public int size() {
        return idCounter;
    }
    
    /**
     * @return the width of the sheet.
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * @return the height of the sheet.
     */
    public int getHeight() {
        return height;
    }
    
    /**
     * @return the area of the sheet.
     */
    public int getArea() {
        return width * height;
    }

    /**
     * @return the actual area used by the entries in this dataset.
     */
    public int getEffectiveWidth() {
        int max = 0;
        for (Entry entry : this) {
            max = Math.max(max, entry.getRec().x + entry.getRec().width);
        }
        return max;
    }
    
    /**
     * Sets the width of the sheet.
     * 
     * @param newWidth the new width of the sheet.
     */
    public void setWidth(int newWidth) {
        width = newWidth;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets the height of the sheet.
     * 
     * @param newHeight the new height of the sheet.
     * @throws IllegalArgumentException iff the height of the sheet is fixed.
     */
    public void setHeight(int newHeight)
            throws IllegalArgumentException {
        if (fixedHeight) {
            throw new IllegalArgumentException("The height cannot be set!");
            
        } else {
            height = newHeight;
        }
        
    }

    /**
     * Set the rotation of each entry according to {@code predicate}
     * @param predicate when to rotate the entry
     */
    public void setRotation(Predicate<CompareEntry> predicate) {
        if (allowRot) {
            if (fixedHeight) {
                for (Entry entry : this) {
                    if (entry.getNormalRec().height > height) {
                        entry.setRotation(true);
                    } else if (entry.getNormalRec().width > height) {
                        entry.setRotation(false);
                    } else {
                        entry.setRotation(predicate.test(entry));
                    }
                }
            } else {
                for (Entry entry : this) {
                    entry.setRotation(predicate.test(entry));
                }
            }
        }
    }

    /**
     * Set the ordering of entries to be sorted according to {@code comparator}
     * @param comparator the comparison to sort on
     */
    public void setOrdering(Comparator<CompareEntry> comparator) {
        list.sort(comparator);
    }

    public void shuffle() {
        Collections.shuffle(list);
    }

    public void swap(int i, int j) {
        Collections.swap(list, i, j);
    }

    public void rotate(int i) {
        if (allowRot) {
            Entry entry = list.get(i);
            entry.setRotation(!entry.useRotation());
        }
    }

    @Override
    public Dataset clone() {
        return new Dataset(this);
    }
    
}

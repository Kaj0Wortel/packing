
// Java imports
import java.awt.Rectangle;


/* 
 * Abstract dataset for the rectangles and additional parameters.
 */
public abstract class Dataset
        implements Iterable<Dataset.Entry>, Cloneable {
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
    
    /* -------------------------------------------------------------------------
     * Entry class
     * -------------------------------------------------------------------------
     */
    public class Entry
            implements Cloneable {
        // The rectangle.
        final private Rectangle rec;
        
        // The number denoting the order of occurance in the input.
        final private int id;
        
        // The rotated rectangle.
        private Rectangle rotatedRec;
        
        // Whether to use rotation by default.
        private boolean useRotation = false;
        
        
        public Entry(Rectangle rec, int id) {
            this.rec = rec;
            this.id = id;
        }
        
        public Entry(Entry clone) {
            this(new Rectangle(clone.getNormalRec()), clone.id);
        }
        
        /* 
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
        
        /* 
         * @return the rectangle {@code rec}.
         */
        public Rectangle getNormalRec() {
            return rec;
        }
        
        /* 
         * @return the rotated version of rec.
         * @throws IllegalStateException iff rotations are not allowed.
         */
        public Rectangle getRotatedRec()
                throws IllegalStateException {
            calcRotatedRec();
            return rotatedRec;
        }
        
        /* 
         * @return the rectangle depending on the default rotation.
         */
        public Rectangle getRec() {
            return (useRotation
                        ? getRotatedRec()
                        : getNormalRec());
        }
        
        /* 
         * @return the default rotation of the entry.
         */
        public boolean useRotation() {
            return useRotation;
        }
        
        /* 
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
            return "[rec: " + rec.toString()
                + ", rotation: " + useRotation + "]";
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
    
    
    /* -------------------------------------------------------------------------
     * Functions
     * -------------------------------------------------------------------------
     */
    /* 
     * Adds an entry to the data set.
     * 
     * @param rec the rectangle to be added.
     */
    public abstract void add(Rectangle rec);
    
    /* 
     * @return the entries.
     * TODO: Maybe other return type?
     */
    public abstract Object getEntries();
    
    /* 
     * @return the i'th object that was added.
     */
    public abstract Entry get(int i);
    
    
    /* 
     * @return whether rotations are allowed.
     */
    public boolean allowRotation() {
        return allowRot;
    }
    
    /* 
     * @return the number of rectangles in the dataset.
     */
    public int size() {
        return idCounter;
    }
    
    /* 
     * @return the width of the sheet.
     */
    public int getWidth() {
        return width;
    }
    
    /* 
     * @return the height of the sheet.
     */
    public int getHeight() {
        return height;
    }

    /*
     * @return the area of the sheet.
     */
    public int getArea() {
        return width * height;
    }

    public int getEffectiveWidth() {
        int max = 0;
        for (Entry entry : this) {
            max = Math.max(max, entry.getRec().x + entry.getRec().width);
        }
        return max;
    }
    
    /* 
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
    
    /* 
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

    public abstract Iterable<Entry> sorted();
    
    public abstract Dataset clone();
    
}

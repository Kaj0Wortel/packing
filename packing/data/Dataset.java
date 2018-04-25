
package packing.data;


// Tools imports
import tools.MultiTool;


// Java imports
import java.awt.Rectangle;
import java.awt.Dimension;


/* 
 * Abstract dataset for the rectangles and additional parameters.
 */
public abstract class Dataset
        implements Iterable<Dataset.Entry> { // TODO: Maybe also let it implement java.util.Set or java.util.List.
    /* -------------------------------------------------------------------------
     * Variables
     * -------------------------------------------------------------------------
     */
    // Whether rotation is allowed or not.
    final protected boolean allowRot;
    
    // The predefined height. Use -1 for no predefined height.
    final protected int height;
    
    final protected int numRect;
    
    // Idea: also keep track of the current size of the sheet.
    
    /* -------------------------------------------------------------------------
     * Entry class
     * -------------------------------------------------------------------------
     */
    public class Entry {
        final private Rectangle rec;
        private Rectangle rotatedRec;
        private boolean useRotation = false;
        
        public Entry(Rectangle rec) {
            this.rec = rec;
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
        
    }
    
    
    /* -------------------------------------------------------------------------
     * Constructor
     * -------------------------------------------------------------------------
     */
    /* 
     * @param rotation whether to allow rotation.
     * @param the height restriction. Use -1 for no height restriction.
     * @param numRect the total number of rectangles.
     */
    public Dataset(boolean rotation, int height, int numRect) {
        this.allowRot = rotation;
        this.height = height;
        this.numRect = numRect;
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
    
    
    
    // TODO: depending on which class it aditionally implements,
    // we can also add a data counter.
}

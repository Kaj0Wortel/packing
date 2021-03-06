
package packing.data;


// Packing imports
import packing.tools.MultiTool;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;


/**
 * Abstract dataset for the rectangles and additional parameters.
 */
public class Dataset
        implements Iterable<CompareEntry>, packing.tools.Cloneable {
    /**-------------------------------------------------------------------------
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
    protected List<CompareEntry> list;

    /**-------------------------------------------------------------------------
     * Entry class
     * -------------------------------------------------------------------------
     */
    public class Entry
            extends CompareEntry {
        // The rectangle.
        final protected Rectangle rec;
        
        // The rotated rectangle.
        protected Rectangle rotatedRec;
        
        // Whether to use rotation by default.
        protected boolean useRotation = false;
        
        
        /**
         * Default constructor.
         * 
         * @param rec the rectangle denoting the size and offset of the entry.
         * @param id number denoting the order of occurance in the input.
         */
        public Entry(Rectangle rec, int id) {
            super(id);
            this.rec = rec;
            if (Dataset.this.allowRotation()) calcRotatedRec();
        }
        
        /**
         * Clone constructor.
         * 
         * @param clone entry to be cloned from.
         */
        public Entry(Entry clone) {
            super(clone.id);
            this.useRotation = clone.useRotation;
            
            if (clone.rec != null)
                this.rec = new Rectangle(clone.rec);
            else this.rec = null;
            
            if (clone.rotatedRec != null) 
                this.rotatedRec = new Rectangle(clone.rotatedRec);
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
        
        @Override
        public Rectangle getRec() {
            return (useRotation
                        ? getRotatedRec()
                        : getNormalRec());
        }
        
        @Override
        public Rectangle getNormalRec() {
            return rec;
        }
        
        /**
         * @return the rotated version of rec.
         * @throws IllegalStateException iff rotations are not allowed.
         */
        @Override
        public Rectangle getRotatedRec()
                throws IllegalStateException {
            calcRotatedRec();
            return rotatedRec;
        }
        
        @Override
        public boolean useRotation() {
            return useRotation;
        }
        
        @Override
        public void setRotation(boolean rotation) {
            if (rotation && !Dataset.this.allowRot)
                throw new IllegalStateException("Rotation is not allowed.");
            useRotation = rotation;
        }
        
        @Override
        public void rotate() {
            if (!Dataset.this.allowRot)
                throw new IllegalStateException("Rotation is not allowed.");
            useRotation = !useRotation;
        }
        
        @Override
        public int area() {
            return rec.width * rec.height;
        }
        
        @Override
        public void setLocation(int x, int y) {
            rec.setLocation(x, y);
            if (allowRotation()) {
                calcRotatedRec();
                rotatedRec.setLocation(x, y);
            }
        }

        @Override
        public void setSize(int width, int height) {
            rec.setSize(width, height);
            if (allowRotation()) {
                calcRotatedRec();
                rotatedRec.setSize(height, width);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Dataset.Entry)) return false;
            Entry entry = (Entry) obj;
            
            return ((rec == null && entry.rec == null) ||
                       (rec != null && entry.rec != null &&
                        rec.equals(entry.rec))) &&
                    useRotation == entry.useRotation &&
                    id == entry.id;
        }
        
        @Override
        public int hashCode() {
            return MultiTool.calcHashCode(rec, useRotation);
        }
        
        @Override
        public String toString() {
            return "[rec: [x=" + rec.x + ", y=" + rec.y + ", width="
                    + rec.width + ", height=" + rec.height + "], "
                    + "rotation: " + useRotation + ", id=" + id + "]";
        }
        
        @Override
        public Entry clone() {
            return new Entry(this);
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Constructor
     * -------------------------------------------------------------------------
     */
    /**
     * @param rotation whether to allow rotation.
     * @param height the height restriction. Use -1 for no height restriction.
     * @param numRect the total number of rectangles.
     */
    public Dataset(int height, boolean rotation, int numRect) {
        this.fixedHeight = height != -1;
        this.allowRot = rotation;
        this.numRect = numRect;
        
        if (fixedHeight) this.height = height;
        initList();
    }
    
    /**
     * Clone constructor.
     * 
     * @param clone 
     */
    public Dataset(Dataset clone) {
        this((clone.fixedHeight ? clone.height : -1),
                clone.allowRot, clone.numRect);
        this.idCounter = clone.idCounter;
        this.width = clone.width;
        this.height = clone.height;
        
        for (CompareEntry entry : clone.list) {
            list.add(entry.clone());
        }
    }
    
    /**
     * Constructor to be used for sub-classes.
     */
    public Dataset(int height, int width, boolean allowRot,
            int numRect, boolean fixedHeight) {
        this.height = height;
        this.width = width;
        this.allowRot = allowRot;
        this.numRect = numRect;
        this.fixedHeight = fixedHeight;
        initList();
    }
    
    /**
     * Initiates {@link #list}.
     */
    protected void initList() {
        list = new Stack<CompareEntry>();
    }
    
    
    /**-------------------------------------------------------------------------
     * Functions
     * -------------------------------------------------------------------------
     */
    public static Dataset createEmptyDataset(Dataset data) {
        return new Dataset(data.height, data.width, data.allowRot,
                data.numRect, data.fixedHeight);
    }
    
    /**
     * Adds an entry to the dataset.
     *
     * @param rec the rectangle to be added.
     * @return the new added entry.
     */
    public CompareEntry add(Rectangle rec) {
        CompareEntry entry = new Dataset.Entry(rec, idCounter++);
        list.add(entry);
        return entry;
    }

    public CompareEntry add(CompareEntry entry) {
        idCounter = Math.max(idCounter, entry.getId() + 1);
        CompareEntry newEntry = entry.clone();
        list.add(newEntry);
        return newEntry;
    }

    /**
     * Adds an entry to the data set with a specific id.
     *
     * Only use this when reconstructing from an existing
     * dataset!!
     *
     * @param rec the rectangle to be added.
     * @param id the id of the new entry.
     * @return the new added entry.
     */
    public CompareEntry add(Rectangle rec, int id) {
        //idCounter = Math.max(idCounter, id + 1);
        CompareEntry entry = new Dataset.Entry(rec, id);
        list.add(entry);
        return entry;
    }

    public CompareEntry push(CompareEntry entry) {
        //idCounter = Math.max(idCounter, entry.getId() + 1);
        CompareEntry newEntry = entry.clone();
        list.add(newEntry);
        return newEntry;
    }

    public CompareEntry pop(CompareEntry entry) {
        //if (idCounter == entry.getId() + 1) idCounter--;
        return list.remove(list.size() - 1);
    }
    
    /**
     * Adds an entry to the dataset with the provide width and height.
     * 
     * @param width the width of the new entry.
     * @param height the height of the new entry.
     * @return the new added entry.
     */
    public CompareEntry add(int width, int height) {
        return add(new Rectangle(width, height));
    }
    
    /**
     * Removes the provided rectangle from the entry list.
     * 
     * @param rec the rectangle to be removed.
     */
    public void remove(Rectangle rec) {
        throw new UnsupportedOperationException();
        //list.remove(new Dataset.Entry(rec, idCounter--));
    }
    
    /**
     * Removes the entry from the list.
     * 
     * @param entry entry to be remvoed
     */
    public void remove(CompareEntry entry) {
        list.remove(entry);
    }
    
    @Override
    public Iterator<CompareEntry> iterator() {
        return list.iterator();
    }
    
    /**
     * @return the i'th object that was added.
     */
    public CompareEntry get(int i) {
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
        return list.size();
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
     * @return the actual width used by the entries in this dataset.
     */
    public int getEffectiveWidth() {
        int max = 0;
        for (CompareEntry entry : this) {
            max = Math.max(max, entry.getRec().x + entry.getRec().width);
        }
        return max;
    }
    
    /**
     * @return the actual height used by the entries in this dataset.
     */
    public int getEffectiveHeight() {
        int max = 0;
        for (CompareEntry entry : this) {
            max = Math.max(max, entry.getRec().y + entry.getRec().height);
        }
        return max;
    }
    
    /**
     * Calculates and sets the effective width and height as width and height
     * of the dataset.
     * If the dataset has a fixed height, only the width will be updated.
     */
    public void calcEffectiveSize() {
        if (fixedHeight) {
            setWidth(getEffectiveWidth());
            
        } else {
            setSize(getEffectiveWidth(), getEffectiveHeight());
        }
    }
    
    /**
     * Sets the width and height of the dataset.
     * Does so without bound checking.
     * 
     * @param width 
     * @param height 
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    /**
     * Sets the width of the sheet.
     * 
     * @param newWidth the new width of the sheet.
     * 
     * Delegates the functionality to {@link #setSize(int, int)}.
     */
    public void setWidth(int newWidth) {
        setSize(newWidth, height);
    }
    
    /**
     * Sets the height of the sheet.
     * 
     * @param newHeight the new height of the sheet.
     * 
     * Delegates the functionality to {@link #setSize(int, int)}.
     */
    public void setHeight(int newHeight) {
        setSize(width, newHeight);
    }
    
    /**
     * Set the rotation of each entry according to {@code predicate}
     * @param predicate when to rotate the entry
     */
    public void setRotation(Predicate<CompareEntry> predicate) {
        if (allowRot) {
            if (fixedHeight) {
                for (CompareEntry entry : this) {
                    if (entry.getNormalRec().height == height) {
                        entry.setRotation(predicate.test(entry));
                        
                    } else {
                        entry.setRotation(entry.getNormalRec().height > height);
                    }
                }
            } else {
                for (CompareEntry entry : this) {
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
            CompareEntry entry = list.get(i);
            entry.setRotation(!entry.useRotation());
        }
    }
    
    /**
     * If there were any modifications, update {@link #list}.
     */
    public void update() { }
    
    @Override
    public Dataset clone() {
        return new Dataset(this);
    }
    
}

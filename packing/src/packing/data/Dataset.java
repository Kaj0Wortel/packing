
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

    protected static Random random = new Random();

    // Do not rotate rectangles
    public static final Predicate<Entry> NO_ROTATION = entry -> false;

    // Rotate rectangles randomly
    public static final Predicate<Entry> RANDOM_ROTATION = entry -> random.nextBoolean();

    // Rotate rectangles so their longest side is vertical
    public static final Predicate<Entry> LONGEST_SIDE_VERTIAL = entry -> entry.getRec().width > entry.getRec().height;

    // Sort rectangles by decreasing height
    public static final Comparator<Entry> SORT_HEIGHT = Collections.reverseOrder(
            Comparator.comparingInt((Entry entry) -> entry.getRec().height)
                    .thenComparing((Entry entry) -> entry.getRec().width)
    );

    // Sort rectangles by decreasing area
    public static final Comparator<Entry> SORT_AREA = Collections.reverseOrder(
            Comparator.comparingInt((Entry entry) -> entry.getRec().height * entry.getRec().width)
                    .thenComparing((Entry entry) -> entry.getRec().height)
    );

    // Sort rectangles by decreasing width
    public static final Comparator<Entry> SORT_WIDTH = Collections.reverseOrder(
            Comparator.comparingInt((Entry entry) -> entry.getRec().width)
                    .thenComparing((Entry entry) -> entry.getRec().height)
    );

    // Sort rectangles by the length of their longest side, decreasing
    public static final Comparator<Entry> SORT_LONGEST_SIDE = Collections.reverseOrder(
            Comparator.comparingInt((Entry entry) -> Math.max(entry.getRec().height, entry.getRec().width))
                    .thenComparing((Entry entry) -> Math.min(entry.getRec().height, entry.getRec().width))
    );

    // Sort rectangles by id, ascending
    public static final Comparator<Entry> SORT_ID = Comparator.comparingInt(entry -> entry.id);


    /* -------------------------------------------------------------------------
     * Entry class
     * -------------------------------------------------------------------------
     */
    public class Entry
            implements packing.tools.Cloneable {
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
            useRotation = clone.useRotation;
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

    public Dataset(Dataset clone) {
        this((clone.fixedHeight ? clone.height : -1),
                clone.allowRot, clone.numRect);
        this.idCounter = clone.idCounter;
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
        return "[allowRot: " + allowRot + ", height: " + height
                + ", numRect: " + numRect + " set: " + list.toString() + "]";
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
    public void setRotation(Predicate<Entry> predicate) {
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
    public void setOrdering(Comparator<Entry> comparator) {
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

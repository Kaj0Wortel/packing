
package packing.data;

// Java imports.
import java.awt.Rectangle;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;


/**
 * This dataset ignores double occurance of rectangles for iteration.
 * This means that each multiple occuring the rectangle is returned only once
 * by the iterator. Note that when rotation is allowed, the rectangles
 * (1, 2) and (2, 1) also are considered equal.
 * 
 * NOTES OF CAUTION:
 * - An "open" iterator cannot be "abandoned" carelessly. One can only do this
 *   AFTER calling the {@link IgnoreDoubleIterator#hasNext()} function when no
 *   rotations are not allowed or the last entry returned was a square,
 *   or by calling the same method after the same rectangle occured twice when
 *   rotations are allowed
 * - By default, the returned entries are semi-randomly sorted. This means
 *   that when exactly the same dataset in the same order is inputted, the
 *   result will be the same, but no further guarantees about the order.
 */
public class IgnoreDoubleDataset
        extends Dataset
        implements Iterable<CompareEntry>, packing.tools.Cloneable {
    
    // The dataset used as source.
    final protected Dataset dataset;
    
    // Map containing the entries. Used to find width/height matches in O(1).
    protected HashMap<MultiEntryKey, MultiEntry> entryMap;
    
    // Whether there were any unprocessed modifications to {@link #entryMap}.
    protected boolean modified = false;
    
    
    /**-------------------------------------------------------------------------
     * Entry containing multiple {@code CompareEntry}'s.
     * -------------------------------------------------------------------------
     */
    /**
     * All entries contained in this class have the same characteristics,
     * so the width and height are equal or, when rotations are allowed,
     * the width equal to the height and vice verca.
     */
    public class MultiEntry
            extends CompareEntry
            implements Iterable<CompareEntry>, Iterator<CompareEntry> {
        // List containing all entries.
        final private List<CompareEntry> entries = new ArrayList<>();
        
        // The width and height of the entries. Note that these values
        // can occur swapped for some entries iff rotations are allowed.
        final private int width;
        final private int height;
        
        // Pointer for the current entry.
        private int entryPointer = -1;
        
        
        /**
         * @param width the width value for all entries.
         * @param height the height value for all entries.
         * 
         * Note that the width and height can be swapped for some entries
         * iff rotations are allowed.
         */
        public MultiEntry(int width, int height) {
            super(idCounter++);
            this.width = width;
            this.height = height;
        }
        
        /**
         * Clone constructor.
         */
        public MultiEntry(MultiEntry clone) {
            super(clone.id);
            this.width = clone.width;
            this.height = clone.height;
            this.entryPointer = clone.entryPointer;
        }
        
        
        /**
         * @return the total number of entries in this class.
         */
        public int size() {
            return entries.size();
        }
        
        /**
         * @return the remaining available entries to be returned.
         */
        public int getRemaining() {
            return entries.size() - entryPointer + 1;
        }
        
        /**
         * @param i the location of the element to return.
         * @return the entry at the i'th position.
         */
        public CompareEntry get(int i) {
            return entries.get(i);
        }
        
        /**
         * @return whether there are more entries to be returned.
         */
        @Override
        public boolean hasNext() {
            return entryPointer + 1 < entries.size();
        }
        
        /**
         * @return the next entry according to the entry pointer.
         * @throws NoSuchElementException iff
         *     there are no more remaining elements.
         */
        @Override
        public CompareEntry next() {
            if (getRemaining() <= 0) throw new NoSuchElementException();
            return entries.get(++entryPointer);
        }
        
        @Override
        public void remove() {
            entries.remove(entryPointer--);
        }
        
        
        /**
         * Removes the element at the given index from the list
         * 
         * @param i index of the element to be removed.
         * @return the removed element.
         */
        public CompareEntry remove(int i) {
            return entries.remove(i);
        }
        
        /**
         * Reverts the entry pointer to the previous entry.
         */
        public void revert() {
            entryPointer--;
        }
        
        
        @Override
        public Rectangle getRec() {
            return entries.get(Math.max(entryPointer, 0)).getRec();
        }
        
        @Override
        public Rectangle getNormalRec() {
            return entries.get(Math.max(entryPointer, 0)).getNormalRec();
        }
        
        @Override
        public Rectangle getRotatedRec() {
            return entries.get(Math.max(entryPointer, 0)).getRotatedRec();
        }
        
        @Override
        public boolean useRotation() {
            return entries.get(Math.max(entryPointer, 0)).useRotation();
        }
        
        @Override
        public void setRotation(boolean rotate) {
            entries.get(Math.max(entryPointer, 0)).setRotation(rotate);
        }
        
        @Override
        public void rotate() {
            entries.get(Math.max(entryPointer, 0)).rotate();
        }
        
        @Override
        public int area() {
            return entries.get(Math.max(entryPointer, 0)).area();
        }
        
        @Override
        public void setLocation(int x, int y) {
            entries.get(Math.max(entryPointer, 0)).setLocation(x, y);
        }
        
        /**
         * Adds the given entry.
         * Assumes that the entry is already of the correct format.
         * (correct width and height).
         * 
         * @param entry the entry to be added.
         */
        public void add(CompareEntry entry) {
            entries.add(entry);
        }
        
        /**
         * Merges with the given multiEntry.
         * Assumes that both entries have the same width and height.
         * Only modifies it's own entries.
         * The other entry should be disposed of after the merge.
         * 
         * @param entry entry to merge with.
         */
        public void merge(MultiEntry entry) {
            for (int i = 0; i < entry.size(); i++) {
                entries.add(entry.get(i));
            }
        }
        
        @Override
        public Iterator<CompareEntry> iterator() {
            return entries.iterator();
        }
        
        @Override
        public int hashCode() {
            return width * height;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MultiEntry)) return false;
            MultiEntry me = (MultiEntry) obj;
            
            return width == me.width && height == me.height;
        }
        
        @Override
        public MultiEntry clone() {
            return new MultiEntry(this);
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Key class for the multiEntries.
     * -------------------------------------------------------------------------
     */
    public class MultiEntryKey {
        final private int width;
        final private int height;
        
        /**
         * @param width the width value of the key.
         * @param height the height value of the key.
         */
        public MultiEntryKey(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        @Override
        public int hashCode() {
            if (dataset.allowRotation()) {
                return 7 * width * height;
            } else {
                return (7 * width + 17) * (5 * height + 19);
            }
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MultiEntryKey)) return false;
            MultiEntryKey mek = (MultiEntryKey) obj;
            
            return (width == mek.width && height == mek.height) ||
                   (dataset.allowRot &&
                       width == mek.height && height == mek.width);
        }
    }
    
    
    /**-------------------------------------------------------------------------
     * Iterator ignoring double elements.
     * -------------------------------------------------------------------------
     */
    private class IgnoreDoubleIterator
            implements Iterator<CompareEntry> {
        // The iterator over the sorted array
        final private Iterator<CompareEntry> it;
        
        // The next and current multiEntry.
        private MultiEntry nextEntry;
        
        // The current dataset entry. If rotation is allowed, then this will be
        // non-null if the entry from {@code nextEntry} has been returned once,
        // and null if the entry from {@code nextEntry} has not yet been
        // returned or has been returned twice.
        private CompareEntry curDataEntry;
        
        // Whether {@code nextEntry} has been feched yet.
        private boolean nextFeched = false;
        
        /**
         * Iterates over the multi entries of the current entry map.
         */
        private IgnoreDoubleIterator() {
            it = list.iterator();
            //new ArrayIterator<MultiEntry>(sortedArray);
        }
        
        @Override
        public CompareEntry next() {
            if (curDataEntry != null) {
                // If rotations are allowed, and the entry has been returned
                // once, rotate it and return it again.
                curDataEntry.rotate();
                
                // And set the value to null.
                CompareEntry returnValue = curDataEntry;
                curDataEntry = null;
                return returnValue;
            }
            
            if (!nextFeched) {
                if (!hasNext()) throw new NoSuchElementException();
            }
            
            // Check for {@code null} element.
            if (nextEntry == null) throw new NoSuchElementException();
            nextFeched = false;
            
            if (!dataset.allowRotation()) {
                // If no rotations allowed, simply return the value.
                return nextEntry.next();
                
            } else {
                // If rotations are allowed, also set {@code curDataEntry}
                // if the width and height are unequal.
                CompareEntry entry = nextEntry.next();
                Rectangle rec = entry.getNormalRec();
                if (rec.width != rec.height) curDataEntry = entry;
                return entry;
            }
        }
        
        @Override
        public boolean hasNext() {
            if (curDataEntry != null) return true;
            if (nextFeched) return nextEntry != null;
            
            // Revert the actions of the next(/current) entry.
            if (nextEntry != null) {
                nextEntry.revert();
                nextEntry = null;
            }
            
            calcNextEntry();
            nextFeched = true;
            
            return nextEntry != null;
        }
        
        /**
         * Calculates the next entry.
         * Simply returns when the next entry has already been calculated.
         */
        private void calcNextEntry() {
            while (nextEntry == null && it.hasNext()) {
                MultiEntry me = (MultiEntry) it.next(); // Safe cast.
                
                if (me.hasNext()) {
                    nextEntry = me;
                }
            }
        }
        
    }
    
    /**-------------------------------------------------------------------------
     * Iterator for arrays.
     * -------------------------------------------------------------------------
     */
    private class ArrayIterator<V> implements Iterator<V> {
        // The array to iterate over.
        final private V[] array;
        
        // The current element counter.
        private int elemCounter = 0;
        
        private ArrayIterator(V[] array) {
            this.array = array;
        }
        
        @Override
        public V next() {
            if (elemCounter >= array.length)
                throw new NoSuchElementException();
            return array[elemCounter++];
        }
        
        @Override
        public boolean hasNext() {
            return elemCounter < array.length;
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Constructors.
     * -------------------------------------------------------------------------
     */
    /**
     * Maps all entries in the dataset relative to their width and height.
     * 
     * @param data the source dataset.
     */
    public IgnoreDoubleDataset(Dataset data) {
        super(data.height, data.width, data.allowRot, data.numRect,
                data.fixedHeight);
        
        this.dataset = data;
        float loadFactor = 0.75f;
        int numEntries = (int) (data.size() / loadFactor + 1);
        entryMap = new HashMap<>(numEntries, loadFactor);
        
        for (CompareEntry entry : data) {
            Rectangle rec = entry.getNormalRec();
            MultiEntryKey key = new MultiEntryKey(rec.width, rec.height);
            MultiEntry me = entryMap.get(key);
            
            if (me == null) {
                // If the multi entry does not yet exist, create it.
                me = new MultiEntry(rec.width, rec.height);
                entryMap.put(key, me);
            }
            
            me.add(entry);
        }
        //sortedArray = toArray(entryMap.values(), MultiEntry.class);
        for (MultiEntry entry : entryMap.values()) {
            super.list.add(entry);
        }
        
        modified = false;
    }
    
    /**
     * Clone constructor.
     * 
     * @param height
     * @param width
     * @param allowRot
     * @param numRect
     * @param fixedHeight
     * @param dataset 
     */
    private IgnoreDoubleDataset(int height, int width, boolean allowRot,
           int numRect, boolean fixedHeight, Dataset dataset) {
        super(height, width, allowRot, numRect, fixedHeight);
        this.dataset = dataset;
    }
    
    
    /**-------------------------------------------------------------------------
     * Functions.
     * -------------------------------------------------------------------------
     */
    /**
     * Converts a collection to an array.
     * 
     * @param <V1> the type value of the collection.
     * @param <V2> the type value of the returned array.
     * @param col the collection to be converted.
     * @param c denotes the return array class type.
     * @return an array containg the same instances as {@code col}, and in the
     *     same order as returned by the iterator of {@code col}.
     */
    @SuppressWarnings("unchecked")
    private static <V1, V2 extends V1> V1[] toArray(Collection<V2> col,
                                                    Class<V1> c) {
        V1[] arr = (V1[]) Array.newInstance(c, col.size()); // Valid cast.
        
        int counter = 0;
        for (V1 v : col) {
            arr[counter++] = v;
        }
        
        return arr;
    }
    
    /**
     * @return the used dataset.
     */
    public Dataset getDataset() {
        return dataset;
    }
    
    @Override
    public void setSize(int width, int height) {
        dataset.setSize(width, height);
    }
    
    @Override
    public void setRotation(Predicate<CompareEntry> predicate) {
        update();
        super.setRotation(predicate);
    }
    
    @Override
    public void setOrdering(Comparator<CompareEntry> comparator) {
        update();
        super.setOrdering(comparator);
    }
    
    @Override
    public void shuffle() {
        update();
        super.shuffle();
    }
    
    @Override
    public Iterator<CompareEntry> iterator() {
        return new IgnoreDoubleIterator();
    }
    
    @Override
    public IgnoreDoubleDataset clone() {
        IgnoreDoubleDataset clone = new IgnoreDoubleDataset(height, width,
                allowRot, numRect, fixedHeight, dataset);
        
        for (MultiEntry entry : entryMap.values()) {
            Rectangle rec = entry.getNormalRec();
            MultiEntryKey key
                    = new MultiEntryKey(rec.width, rec.height);
            
            MultiEntry clonedEntry = entry.clone();
            clone.entryMap.put(key, clonedEntry);
            list.add(clonedEntry);
        }
        
        return clone;
    }
    
    @Override
    public CompareEntry add(Rectangle rec) {
        Dataset.Entry entry = new Dataset.Entry(rec, idCounter++);
        add(entry);
        return entry;
    }
    
    /**
     * Adds the given entry to the mapping.
     * @param entry 
     */
    public void add(CompareEntry entry) {
        Rectangle rec = entry.getRec();
        MultiEntryKey key = new MultiEntryKey(rec.width, rec.height);
        MultiEntry me = entryMap.get(key);
        
        if (me == null) {
            if (entry instanceof MultiEntry) {
                me = (MultiEntry) entry;
                
            } else {
                me = new MultiEntry(rec.width, rec.height);
                me.add(entry);
            }
            
            modified = true;
            entryMap.put(key, me);
            
        } else {
            if (entry instanceof MultiEntry) {
                me.merge((MultiEntry) entry);
                
            } else {
                me.add(entry);
            }
        }
    }
    
    @Override
    public void update() {
        // Note: any previous order that was set in the same list will be
        // overwritten by the new one.
        if (modified) {
            modified = false;
            list.clear();
            for (MultiEntry entry : entryMap.values()) {
                list.add(entry);
            }
        }
        
        dataset.update();
    }
    
    
    // tmp
    public static void main(String[] args) {
        Dataset data = new Dataset(-1, false, 3);
        //data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(6, 2));
        //data.add(new Rectangle(4, 3));
        //data.add(new Rectangle(3, 4));
        //data.add(new Rectangle(100, 100));
        data.add(new Rectangle(20, 6));
        data.add(new Rectangle(2, 4));
        data.add(new Rectangle(7, 1));
        
        IgnoreDoubleDataset idd = new IgnoreDoubleDataset(data);
        
        /*
        for (Dataset.Entry entry : idd) {
            System.err.println("entry: " + entry);
        }
        
        System.err.println("------------");
        
        for (Dataset.Entry entry : idd) {
            System.err.println("entry: " + entry);
            for (Dataset.Entry entry2 : idd) {
                System.err.println("entry2: " + entry2);
            }
        }*/
        recursion(idd, 0);
        
    }
    
    // tmp
    public static void recursion(IgnoreDoubleDataset idd, int i) {
        for (CompareEntry entry : idd) {
            System.out.println(i + ": " + entry.getRec());
            recursion(idd, i + 1);
        }
    }
}

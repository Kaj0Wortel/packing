
package packing.data;

// Java imports.
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;


/**
 * This dataset ignores double occurance of rectangles for iteration.
 * This means that each multiple occuring the rectangle is returned only once
 * by the iterator. Note that when rotation is allowed, the rectangles
 * (1, 2) and (2, 1) also are considered equal.
 * 
 * NOTES OF CAUTION:
 * - This class is ONLY suitable for recursive use. This means that you
 *   should not use this class when new iterators are created in a linear way
 *   (recursing after each other instead of simultaneously).
 * - This class is not thread safe.
 * - There is no guarantee of the order of the rectangles that are returned.
 * - There is no guarantee of the entry returned by the iterator.
 * - An open iterator cannot be abandoned carelessly. One can only do this
 *   AFTER calling the {@link IgnoreDoubleIterator#hasNext()} function.
 */
public class IgnoreDoubleDataset
        implements Iterable<Dataset.Entry>, packing.tools.Cloneable {
    
    // The dataset used as source.
    final protected Dataset dataset;
    
    // Map containing the multi entries.
    final protected HashMap<MultiEntryKey, MultiEntry> entryMap;
    
    
    /**-------------------------------------------------------------------------
     * Entry containing multiple {@code Dataset.Entry}'s.
     * All entries contained in this class have the same characteristics,
     * so the width and height are equal or, when rotations are allowed,
     * the width equal to the height and vice verca.
     */
    public class MultiEntry 
            implements Iterable {
        // List containing all entries.
        final private List<Dataset.Entry> entries = new ArrayList<>();
        
        // The width and height of the entries. Note that these values
        // can occur swapped for some entries iff rotations are allowed.
        final private int width;
        final private int height;
        
        // Pointer for the entries to be returned via {@link #next()}.
        private int entryPointer = 0;
        
        /**
         * @param width the width value for all entries.
         * @param height the height value for all entries.
         * 
         * Note that the width and height can be swapped for some entries
         * iff rotations are allowed.
         */
        public MultiEntry(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        /**
         * @return the total number of enties in this class.
         */
        public int size() {
            return entries.size();
        }
        
        /**
         * @return the remaining available entries to be returned.
         */
        public int getRemaining() {
            return entries.size() - entryPointer;
        }
        
        /**
         * @param i the location of the element to return.
         * @return the entry at the i'th position.
         */
        public Dataset.Entry get(int i) {
            return entries.get(i);
        }
        
        /**
         * @return whether there are more entries to be returned.
         */
        public boolean hasNext() {
            return entryPointer < entries.size();
        }
        
        /**
         * @return the next entry according to the entry pointer.
         * @throws NoSuchElementException iff
         *     there are no more remaining elements.
         */
        public Dataset.Entry next() {
            if (getRemaining() <= 0) throw new NoSuchElementException();
            return entries.get(entryPointer++);
        }
        
        /**
         * Reverts the entry pointer to the previous entry.
         */
        public void revert() {
            entryPointer--;
        }
        
        /**
         * Adds the given entry.
         * Assumes that the entry is already of the correct format.
         * (correct width and height).
         * 
         * @param entry the entry to be added.
         */
        public void add(Dataset.Entry entry) {
            entries.add(entry);
        }
        
        @Override
        public Iterator<Dataset.Entry> iterator() {
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
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Key class for the multiEntries.
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
            return 7 * width * height;
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
    
    
    /**
     * Iterator for the elements.
     */
    private class IgnoreDoubleIterator
            implements Iterator<Dataset.Entry> {
        // The iterator over the entries of the map.
        final private Iterator<MultiEntry> mapIt;
        
        // The next and current multiEntry.
        private MultiEntry nextEntry;
        
        private boolean processed = false;
        
        /**
         * Iterates over the multi entries of the current entry map.
         */
        private IgnoreDoubleIterator() {
            mapIt = entryMap.values().iterator();
        }
        
        @Override
        public Dataset.Entry next() {
            if (!processed) {
                if (hasNext()) throw new NoSuchElementException();
            }
            
            // Check for {@code null} element.
            if (nextEntry == null) throw new NoSuchElementException();
            processed = false;
            
            // Return the value
            return nextEntry.next();
        }
        
        @Override
        public boolean hasNext() {
            if (processed) return nextEntry != null;
            
            // Revert the actions of the next(/current) entry.
            if (nextEntry != null) {
                nextEntry.revert();
                nextEntry = null;
            }
            
            calcNextEntry();
            processed = true;
            
            return nextEntry != null;
        }
        
        /**
         * Calculates the next entry.
         * Simply returns when the next entry has already been calculated.
         */
        private void calcNextEntry() {
            while (nextEntry == null && mapIt.hasNext()) {
                MultiEntry me = mapIt.next();
                
                if (me.hasNext()) {
                    nextEntry = me;
                }
            }
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Constructor.
     * Maps all entries in the dataset relative to their width and height.
     * 
     * @param data the source dataset.
     */
    public IgnoreDoubleDataset(Dataset data) {
        this.dataset = data;
        float loadFactor = 0.75f;
        int numEntries = (int) (data.size() / loadFactor + 1);
        entryMap = new HashMap<>(numEntries, loadFactor);
        
        for (Dataset.Entry entry : data) {
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
    }
    
    /**
     * @return the used dataset.
     */
    public Dataset getDataset() {
        return dataset;
    }
    
    @Override
    public Iterator<Dataset.Entry> iterator() {
        return new IgnoreDoubleIterator();
    }
    
    @Override
    public IgnoreDoubleDataset clone() {
        return new IgnoreDoubleDataset(dataset.clone());
    }
    
    // tmp
    public static void main(String[] args) {
        Dataset data = new Dataset(-1, false, 3);
        data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(6, 2));
        //data.add(new Rectangle(4, 3));
        //data.add(new Rectangle(3, 4));
        data.add(new Rectangle(100, 100));
        
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
    
    public static void recursion(IgnoreDoubleDataset idd, int i) {
        for (Dataset.Entry entry : idd) {
            System.out.println(i + ": " + entry);
            recursion(idd, i + 1);
        }
    }
}

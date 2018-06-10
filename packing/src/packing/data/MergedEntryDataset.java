
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * This class represents a merged {@link packing.data.CompareEntry}.
 * After the entries have been merged, the original entries will be deleted
 * and the new entry will be added.
 */
public class MergedEntryDataset
        extends Dataset
        implements packing.tools.Cloneable {
    
    public class MergedEntry
            extends Dataset.Entry
            implements Iterable<CompareEntry> {
        final List<CompareEntry> entries;
        
        /**
         * Default constructor.
         * 
         * @param entries the entries to be merged.
         * @param id 
         */
        protected MergedEntry(Iterable<CompareEntry> entries, int id) {
            super(new Rectangle
                (Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0),
                    id);
            this.entries = new ArrayList<CompareEntry>();
            
            for (CompareEntry entry : entries) {
                this.add(entry);
            }
        }
        
        /**
         * Clone constructor.
         * 
         * @param clone 
         */
        protected MergedEntry(MergedEntry clone) {
            super((Rectangle) clone.rec.clone(), clone.id);
            entries = new ArrayList<CompareEntry>();
            
            for (CompareEntry entry : clone.entries) {
                entries.add(entry.clone());
            }
        }
        
        
        @Override
        public void setRotation(boolean rotation) {
            boolean rotated = this.useRotation ^ rotation;
            super.setRotation(rotation);
            
            if (rotated) {
                for (CompareEntry entry : entries) {
                    entry.rotate();
                }
            }
        }
        
        @Override
        public void rotate() {
            super.rotate();
            for (CompareEntry entry : entries) {
                entry.rotate();
            }
        }
        
        @Override
        public void setLocation(int x, int y) {
            int dx = x - rec.x;
            int dy = y - rec.y;
            for (CompareEntry entry : entries) {
                Rectangle normal = entry.getNormalRec();
                normal.x += dx;
                normal.y += dy;
                
                if (allowRot) {
                    Rectangle rotated = entry.getRotatedRec();
                    rotated.x += dx;
                    rotated.y += dy;
                }
            }
            
            super.setLocation(x, y);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MergedEntry)) return false;
            MergedEntry entry = (MergedEntry) obj;
            
            return entries == entry.entries && super.equals(entry);
        }
        
        @Override
        public MergedEntry clone() {
            return new MergedEntry(this);
        }
        
        @Override
        public Iterator<CompareEntry> iterator() {
            return entries.iterator();
        }
        
        /**
         * Adds the provided entry.
         * 
         * @param entry the entry to be added.
         */
        public void add(CompareEntry entry) {
            entries.add(entry);
            
            Rectangle main = entry.getRec();
            if (useRotation) {
                entry.rotate();
                Rectangle rotated = entry.getRec();
                rotated.x = main.y;
                rotated.y = main.x;
                entry.rotate();
            }
            
            // Update the current bounding rectangle.
            if (rec.x == Integer.MIN_VALUE) {
                // First rectangle.
                rec.x = main.x;
                rec.y = main.y;
                rec.width = main.width;
                rec.height = main.height;
                
            } else {
                // Not first rectangle.
                if (main.x < rec.x) {
                    rec.width += rec.x - main.x;
                    rec.x = main.x;
                }
                
                if (main.y < rec.y) {
                    rec.height += rec.y - main.y;
                    rec.y = main.y;
                }
                
                if (main.x + main.width > rec.x + rec.width)
                    rec.width = main.x + main.width - rec.x;
                if (main.y + main.height > rec.y + rec.height)
                    rec.height = main.y + main.height - rec.y;
            }
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Constructor.
     * -------------------------------------------------------------------------
     */
    /**
     * Constructs a new dataset from the provided dataset.
     * 
     * @param dataset the input dataset.
     */
    public MergedEntryDataset(Dataset dataset) {
        this(dataset, new LinkedList<CompareEntry>(dataset.list));
    }
    
    /**
     * Constructs a new dataset from the provided dataset using the additional
     * list as entry set.
     * 
     * @param dataset the input dataset.
     * @param newList the entries to use as ordering. These entries do not 
     *     nessecarily need to contain all elements from {@code dataset} and
     *     can contain additional not occuring in {@code dataset}.
     * 
     * Note thet the provide list is not cloned.
     */
    public MergedEntryDataset(Dataset dataset,
            List<CompareEntry> list) {
        super(dataset.height, dataset.width, dataset.allowRot, dataset.numRect,
                dataset.fixedHeight);
        this.list = list;
        idCounter = dataset.idCounter;
    }
    
    
    /**-------------------------------------------------------------------------
     * Functions.
     * -------------------------------------------------------------------------
     */
    /**
     * Merges the provided entries as a single entry.
     * Note that the provided entries are only removed in this class,
     * but not in the initial dataset. The changes in location and rotations
     * of the new entry however are delegated to the old entries, which are
     * changed in the initial dataset.
     * 
     * @param entries the entries to be merged.
     * 
     * Note:
     * Worstcase running time: O(n). Use {@link #mergeEntries(List)} to
     * get O(k) constant running time (with k the number of merged rectangles).
     */
    public MergedEntry mergeEntries(List<CompareEntry> entries) {
        MergedEntry me = new MergedEntry(entries, idCounter++);
        list.add(me);
        list.removeAll(entries);
        return me;
    }
    
    /**
     * Merges the entries given through their indices.
     * Note that the provided entries are only removed in this class,
     * but not in the initial dataset. The changes in location and rotations
     * of the new entry however are delegated to the old entries, which are
     * changed in the initial dataset.
     * 
     * @param entryIndices Nums the entries to be merged.
     */
    public MergedEntry MergedEntry(int... entryIndices) {
        List<CompareEntry> entries = new ArrayList<CompareEntry>();
        // Iterate entryIndices in reverse sorted order, so the indices
        // of the entries we still have to remove don't shift before
        // we can remove them.
        Arrays.sort(entryIndices);
        for (int i = entryIndices.length - 1; i >= 0; i--) {
            entries.add(list.remove(entryIndices[i]));
        }
        
        MergedEntry me = new MergedEntry(entries, idCounter++);
        list.add(me);
        return me;
    }
    
    /**
     * Used to merge two entries. If possible, prefere using this method
     * when joining a {@code MergedEntry} with another {@code MergedEntry}.
     * 
     * @param entry1 first entry to be merged.
     * @param entry2 second entry to be merged.
     * @return the merged entry of {@code entry1} and {@code entry2}.
     */
    public MergedEntry merge(CompareEntry entry1, CompareEntry entry2) {
        boolean isMe1 = entry1 instanceof MergedEntry;
        boolean isMe2 = entry2 instanceof MergedEntry;
        
        if (isMe1) {
            MergedEntry me1 = (MergedEntry) entry1;
            if (isMe2) {
                // Both are MergedEntries.
                // Add all entries from entry 2 to entry1.
                MergedEntry me2 = (MergedEntry) entry2;
                for (CompareEntry entry : me2) {
                    me1.add(entry);
                }
                list.remove(entry2);
                
            } else {
                // Only entry1 is a MergedEntry.
                me1.add(entry2);
                list.remove(entry2);
            }
            
            return me1;
            
        } else {
            if (isMe2) {
                // Only entry2 is a MergedEntry
                MergedEntry me2 = (MergedEntry) entry2;
                me2.add(entry1);
                list.remove(entry1);
                return me2;
                
            } else {
                // None are mergedEntries
                List<CompareEntry> merge = new ArrayList<CompareEntry>(2);
                merge.add(entry1);
                merge.add(entry2);
                return mergeEntries(merge);
            }
        }
    }
    
    
    public static void main(String[] args) {
        Dataset dataset = new Dataset(-1, false, 5);
        /*
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(1, 1));
        */
        //dataset.add(new Rectangle(1, 1));
        //dataset.add(new Rectangle(1, 1));
        //dataset.add(new Rectangle(1, 1));
        CompareEntry me1 = dataset.add(new Rectangle(4, 5));
        CompareEntry me2 = dataset.add(new Rectangle(2, 5));
        CompareEntry me3 = dataset.add(new Rectangle(3, 5));
        
        MergedEntryDataset med = new MergedEntryDataset(dataset);
        System.out.println("start merge!");
        me2.getRec().setLocation(4, 0);
        System.out.println(med);
        MergedEntry me = med.merge(me1, me2);
        me.getRec().setLocation(3, 1);
        me3.setLocation(9, 1);
        System.out.println(med);
        med.merge(me, me3);
        System.out.println(me);
        System.out.println(med);
    }
    
}

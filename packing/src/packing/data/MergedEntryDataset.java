
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
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
            extends Dataset.Entry {
        final List<CompareEntry> entries;
        
        /**
         * Default constructor.
         * 
         * @param entries the entries to be merged.
         * @param id 
         */
        protected MergedEntry(List<CompareEntry> entries, int id) {
            super(new Rectangle
                (Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0),
                    id);
            this.entries = entries;
            
            for (CompareEntry entry : entries) {
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
        public boolean equals(Object obj) {
            if (!(obj instanceof MergedEntry)) return false;
            MergedEntry entry = (MergedEntry) obj;
            
            return entries == entry.entries && super.equals(entry);
        }
        
        @Override
        public MergedEntry clone() {
            return new MergedEntry(this);
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Constructor.
     * -------------------------------------------------------------------------
     */
    /**
     * @param dataset 
     */
    public MergedEntryDataset(Dataset dataset) {
        super(dataset.height, dataset.width, dataset.allowRot, dataset.numRect,
                dataset.fixedHeight);
        
        for (CompareEntry entry : dataset) {
            list.add(entry);
        }
        
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
    
    public MergedEntry merge(CompareEntry entry1, CompareEntry entry2) {
        List<CompareEntry> merge = new ArrayList<CompareEntry>(2);
        merge.add(entry1);
        merge.add(entry2);
        return mergeEntries(merge);
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

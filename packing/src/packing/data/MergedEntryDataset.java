package packing.data;

import java.awt.Rectangle;
import java.util.ArrayList;
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
        
        protected MergedEntry(List<CompareEntry> entries, int id) {
            super(new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0),
                    id);
            this.entries = entries;
            
            for (CompareEntry entry : entries) {
                Rectangle main = entry.getRec();
                entry.rotate();
                Rectangle rotated = entry.getNormalRec();
                entry.rotate();
                
                rotated.x = main.y;
                rotated.y = main.x;
                
                // Update the current bounding rectangle.
                if (main.x < rec.x)
                    rec.x = rotated.x;
                if (main.y < rec.y)
                    rec.y = rotated.y;
                if (main.x + main.width > rec.x + rec.width)
                    rec.width = main.x + main.width - rec.x;
                if (main.y + main.height > rec.y + rec.height)
                    rec.height = main.y + main.height - rec.y;
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
        
        idCounter += list.size();
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
    public void mergeEntries(List<CompareEntry> entries) {
        list.add(new MergedEntry(entries, idCounter++));
        
        list.removeAll(entries);
    }
    
    /**
     * Merges the entries given through their indices.
     * Note that the provided entries are only removed in this class,
     * but not in the initial dataset. The changes in location and rotations
     * of the new entry however are delegated to the old entries, which are
     * changed in the initial dataset.
     * 
     * @param entryNums the entries to be merged.
     */
    public void mergeEntries(int... entryIndices) {
        List<CompareEntry> entries = new ArrayList<CompareEntry>();
        
        for (int i : entryIndices) {
            entries.add(list.get(i));
            list.remove((int) i);
        }
        
        list.add(new MergedEntry(entries, idCounter++));
    }
    
}

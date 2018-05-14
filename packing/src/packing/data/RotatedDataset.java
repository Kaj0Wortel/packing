
package packing.data;


// Java imports
import java.awt.Rectangle;

import java.util.*;


public class RotatedDataset
        extends Dataset {
    List<Dataset.Entry> set = new ArrayList<>();
    
    public RotatedDataset(int height, boolean rotation, int numRect) {
        super(height, rotation, numRect);
    }
    
    public RotatedDataset(RotatedDataset clone) {
        super((clone.fixedHeight ? clone.height : -1),
              clone.allowRot, clone.numRect);
        this.idCounter = clone.idCounter;
        for (Dataset.Entry entry : clone.set) {
            set.add(entry.clone());
        }
    }
    
    @Override
    public void add(Rectangle rec) {
        set.add(new Dataset.Entry(rec, idCounter++));
    }
    
    @Override
    public Object getEntries() {
        return null;
    }
    
    @Override
    public Iterator<Dataset.Entry> iterator() {
        return set.iterator();
    }

    @Override
    public Iterable<Dataset.Entry> sorted() {
        List<Dataset.Entry> entries = new ArrayList<>(set);
        if (allowRot) {
            for (Entry ent : entries) {
                ent.setRotation(ent.getNormalRec().width > ent.getNormalRec().height);
            }
        }
        entries.sort(
            Collections.reverseOrder(
                Comparator.comparingInt(
                    (Dataset.Entry entry) -> (entry.getRec().height)
                )
            )
        );
        return entries;
    }
    
    @Override
    public Dataset.Entry get(int i) {
        return set.get(i);
    }
    
    @Override
    public String toString() {
        return "[allowRot: " + allowRot + ", height: " + height
            + ", numRect: " + numRect + " set: " + set.toString() + "]";
    }
    
    @Override
    public RotatedDataset clone() {
        return new RotatedDataset(this);
    }
    
}

package packing.data;


// Java imports
import java.awt.Rectangle;

import java.util.*;


public class RotatedDataset
        extends Dataset {
    List<Dataset.Entry> list = new ArrayList<>();
    
    public RotatedDataset(int height, boolean rotation, int numRect) {
        super(height, rotation, numRect);
    }
    
    public RotatedDataset(RotatedDataset clone) {
        super((clone.fixedHeight ? clone.height : -1),
              clone.allowRot, clone.numRect);
        this.idCounter = clone.idCounter;
        for (Dataset.Entry entry : clone.list) {
            list.add(entry.clone());
        }
    }
    
    @Override
    public void add(Rectangle rec) {
        list.add(new Dataset.Entry(rec, idCounter++));
    }
    
    @Override
    public Iterator<Dataset.Entry> iterator() {
        return list.iterator();
    }
    
    @Override
    public Iterable<Dataset.Entry> sorted() {
        List<Dataset.Entry> entries = new ArrayList<>(list);
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
        return list.get(i);
    }
    
    @Override
    public String toString() {
        return "[allowRot: " + allowRot + ", height: " + height
            + ", numRect: " + numRect + " set: " + list.toString() + "]";
    }
    
    @Override
    public RotatedDataset clone() {
        return new RotatedDataset(this);
    }
    
}
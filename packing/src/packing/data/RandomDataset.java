
package packing.data;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Randomly iterates of the the instances of the dataset.
 * Ensures that all elements are used.
 */
public class RandomDataset
        extends Dataset {
    List<Dataset.Entry> list = new ArrayList<>();
    
    public RandomDataset(int height, boolean rotation, int numRect) {
        super(height, rotation, numRect);
    }
    
    public RandomDataset(RandomDataset clone) {
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
    
    /**
     * @return A cloned ArrayList where the entries are randomly shuffled.
     */
    @Override
    public Iterable<Dataset.Entry> sorted() {
        // Clone the order.
        ArrayList<Entry> clone = new ArrayList<Entry>(list);
        
        // Shuffle the entries.
        Collections.shuffle(clone);
        
        // Randomly set the rotation iff allowed.
        if (allowRot) {
            Random r = new Random();
            Iterator<Entry> it = clone.iterator();
            while (it.hasNext()) {
                it.next().setRotation(r.nextBoolean());
            }
        }
        
        return clone;
    }
    
    @Override
    public Dataset.Entry get(int i) {
        return list.get(i);
    }
    
    @Override
    public String toString() {
        return "[allowRot: " + allowRot + ", height: " + height
            + ", numRect: " + numRect + " list: " + list.toString() + "]";
    }
    
    @Override
    public RandomDataset clone() {
        return new RandomDataset(this);
    }
    
}

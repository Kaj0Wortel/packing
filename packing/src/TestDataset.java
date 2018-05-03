
// Java imports
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class TestDataset
        extends Dataset {
    List<Entry> set = new ArrayList<>();
    
    public TestDataset(int height, boolean rotation, int numRect,
                       Generator gen) {
        super(height, rotation, numRect, gen);
    }
    
    public TestDataset(TestDataset clone) {
        super((clone.fixedHeight ? clone.height : -1),
              clone.allowRot, clone.numRect, clone.generator);
        for (Dataset.Entry entry : clone.set) {
            set.add(entry.clone());
        }
    }
    
    @Override
    public void add(Rectangle rec) {
        set.add(new Entry(rec, idCounter++));
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
    public Entry get(int i) {
        return set.get(i);
    }
    
    @Override
    public String toString() {
        return "[allowRot: " + allowRot + ", height: " + height
            + ", numRect: " + numRect + " set: " + set.toString() + "]";
    }
    
    @Override
    public TestDataset clone() {
        return new TestDataset(this);
    }
    
}
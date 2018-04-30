
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
    
}
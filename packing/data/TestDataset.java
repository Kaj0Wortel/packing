
package packing.data;


// Java imports
import java.awt.Rectangle;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class TestDataset
        extends Dataset {
    Set<Dataset.Entry> set = new HashSet<>();
    
    public TestDataset(boolean rotation, int height, int numRect) {
        super(rotation, height, numRect);
    }
    
    @Override
    public void add(Rectangle rec) {
        set.add(new Entry(rec));
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
    public String toString() {
        return set.toString();
    }
}
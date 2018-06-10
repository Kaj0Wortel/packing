
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Comparator;
import java.util.Iterator;
import java.util.function.Predicate;


/**
 * Abstract decorator class for a dataset.
 */
public abstract class DatasetDecorator
        extends Dataset {
    
    protected Dataset dataset;
    
    public DatasetDecorator(Dataset dataset) {
        super(dataset.height, dataset.allowRot, dataset.numRect);
        this.dataset = dataset;
    }
    
    @Override
    public CompareEntry add(Rectangle rec) {
        return dataset.add(rec);
    }
    
    @Override
    public void remove(Rectangle rec) {
        dataset.remove(rec);
    }
    
    @Override
    public void remove(CompareEntry entry) {
        dataset.remove(entry);
    }
    
    @Override
    public Iterator<CompareEntry> iterator() {
        return dataset.iterator();
    }
    
    @Override
    public CompareEntry get(int i) {
        return dataset.get(i);
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[dataset=" + dataset.toString() + "]";
    }
    
    @Override
    public boolean isFixedHeight() {
        return dataset.isFixedHeight();
    }
    
    @Override
    public boolean allowRotation() {
        return dataset.isFixedHeight();
    }
    
    @Override
    public int size() {
        return dataset.size();
    }
    
    @Override
    public int getWidth() {
        return dataset.getWidth();
    }
    
    @Override
    public int getHeight() {
        return dataset.getWidth();
    }
    
    @Override
    public int getArea() {
        return dataset.getArea();
    }
    
    @Override
    public int getEffectiveWidth() {
        return dataset.getEffectiveWidth();
    }
    
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        dataset.setSize(width, height);
    }
    
    @Override
    public void setRotation(Predicate<CompareEntry> predicate) {
        dataset.setRotation(predicate);
    }
    
    @Override
    public void setOrdering(Comparator<CompareEntry> comparator) {
        dataset.setOrdering(comparator);
    }
    
    @Override
    public void shuffle() {
        dataset.shuffle();
    }
    
    @Override
    public void swap(int i, int j) {
        dataset.swap(i, j);
    }
    
    @Override
    public void rotate(int i) {
        dataset.rotate(i);
    }
    
}

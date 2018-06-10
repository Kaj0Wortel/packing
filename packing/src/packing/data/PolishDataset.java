
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;


/**
 * Dataset that stores the placement of the entries in a polish notation.
 */
public class PolishDataset
        extends DatasetDecorator {
    
    // The used random variable.
    final private static Random random = new Random();
    
    /**
     * Direction class for denoting the operator direction.
     * Let A and B be rectangles, then:
     * A-B-UP means that B is above A, and 
     * A-B-RIGHT means that B on the right of A.
     */
    public static enum Direction {
        RIGHT, UP;
    }
    
    
    /**
     * Operator class.
     * This class denotes the type of operator used in the polish notation.
     */
    public class Operator
            extends Dataset.Entry {
        final private Direction dir;
        
        /**
         * Creates a new operator with the provided direction.
         * 
         * @param dir the direcion of the operator.
         */
        public Operator(Direction dir) {
            super(null, -1);
            this.dir = dir;
        }
        
        /**
         * Creates a new operator using the provided boolean option as
         * operator.
         * 
         * @param option converted to {@link Direction#RIGHT} if true,
         *     {@link Direction#UP} otherwise.
         */
        public Operator(boolean option) {
            this(option ? Direction.RIGHT : Direction.UP);
        }
        
        /**
         * Randomly creates a new operator.
         */
        public Operator() {
            this(random.nextBoolean());
        }
        
        /**
         * @return the direction of the operator.
         */
        public Direction getDirection() {
            return dir;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Operator) return false;
            Operator op = (Operator) obj;
            return op.dir == this.dir;
        }
        
        @Override
        public String toString() {
            return "[" + dir.toString() + "]";
        }
        
    }
    
    
    /**
     * Creates a new PolishDataset from the provided dataset.
     * 
     * @param dataset the input dataset.
     */
    public PolishDataset(Dataset dataset) {
        super(dataset);
        list = new LinkedList<CompareEntry>();
    }
    
    /**
     * Creates a new PolishDataset from the provided dataset using the
     * provided list.
     * 
     * @param dataset the input dataset.
     * @param list the list to be used.
     * 
     * Note that the list is not cloned.
     */
    public PolishDataset(Dataset dataset, LinkedList<CompareEntry> list) {
        super(dataset);
        this.list = list;
    }
    
    @Override
    public PolishDataset clone() {
        return new PolishDataset(dataset.clone(), new LinkedList<>(list));
    }
    
    /**
     * Initializes the dataset with a random reverse polish notation.
     * Note that the elements still occur in the same order as in
     * the provided dataset.
     */
    public void init() {
        list.clear();
        int numOp = 0;
        int numElem = 0;
        int size = size();
        
        Iterator<CompareEntry> it = dataset.iterator();
        
        // Add all elements and (some) operators randomly
        while (numElem < size) {
            if (numElem - 2 >= numOp &&
                    (!it.hasNext() || random.nextBoolean())) {
                // Add an operator.
                numOp++;
                list.add(new Operator());
                
            } else {
                // Add an element.
                list.add(it.next());
                numElem++;
            }
        }
        
        // Add the remaining operators.
        while(numOp < size - 1) {
            // Select an operator.
            numOp++;
            list.add(new Operator());
        }
    }
    
    /**
     * Set the default iterator to the iterator of the input dataset since
     * in this way the additional operators are ignored, and when not yet
     * initialized there is still is a solution available.
     */
    @Override
    public Iterator<CompareEntry> iterator() {
        return dataset.iterator();
    }
    
    /**
     * @return an iterator to iterate over the elements in the list.
     */
    public Iterator<CompareEntry> listIterator() {
        return list.iterator();
    }
    
    @Override
    public String toString() {
        String[] strs = new String[list.size()];
        
        int i = 0;
        for (CompareEntry entry : list) {
            strs[i++] = entry.toString();
        }
        
        return getClass().getSimpleName() + "[" + String.join("", strs) + "]";
    }
    
    /**
     * @return a short representation of the data using id's as identifiers.
     */
    public String toShortString() {
        String[] strs = new String[list.size()];
        
        int i = 0;
        for (CompareEntry entry : list) {
            if (entry instanceof Operator) {
                strs[i++] = entry.toString();
            } else {
                strs[i++] = "[" + entry.id + "]";
            }
        }
        
        return getClass().getSimpleName() + "[" + "width=" + width
                + ", height=" + height + ", elems:[" + String.join("", strs)
                + "]]";
    }
    
    public static void main(String[] args) {
        Dataset dataset = new Dataset(-1, true, 8);
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(2, 2));
        dataset.add(new Rectangle(3, 3));
        dataset.add(new Rectangle(4, 4));
        dataset.add(new Rectangle(5, 5));
        dataset.add(new Rectangle(6, 6));
        dataset.add(new Rectangle(7, 7));
        dataset.add(new Rectangle(8, 8));
        
        PolishDataset pd = new PolishDataset(dataset);
        pd.init();
        System.out.println(pd.toString());
    }
    
}

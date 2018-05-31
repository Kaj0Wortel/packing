
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.*;

import packing.tools.MultiTool;


/**
 * Dataset that stores the placement of the entries in a polish notation.
 */
public class PolishDataset
        extends DatasetDecorator {
    
    // The used random variable.
    final private static Random random = new Random();
    
    private static int opCounter = 0;
    
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
        // Direction of the operator.
        final private Direction dir;
        
        // The total wasted area of the operator.
        private int wastedArea = 0;
        
        // The total area of the operator (covered + wasted).
        private int area = 0;
        
        // The involved entries.
        private CompareEntry[] entries = null;
        private List<CompareEntry> allEntries = null;
        
        // The total number of involved entries, excluding operators.
        private int size = -1;
        
        /**
         * Creates a new operator with the provided direction.
         * 
         * @param dir the direcion of the operator.
         */
        public Operator(Direction dir) {
            super(null, opCounter++);
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
         * TMP
         * @param entry1 the first entry of the operation.
         * @param entry2 the second entry of the operation.
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
            return op.dir == this.dir && op.id == this.id;
        }
        
        @Override
        public String toString() {
            return "[" + dir.toString() + " " + id + "]";
        }
        
        @Override
        public int hashCode() {
            return MultiTool.calcHashCode(dir, id);
        }
        
        /**
         * Sets the amount of wasted area.
         * 
         * @param wa the new amount of wasted area.
         */
        public void setWastedArea(int wa) {
            wastedArea = wa;
        }
        
        /**
         * @return the amount of wasted area.
         */
        public int wastedArea() {
            return wastedArea;
        }
        
        /**
         * Sets the total area.
         * 
         * @param area the new total area.
         */
        public void setArea(int area) {
            this.area = area;
        }
        
        /**
         * @return the total area (covered + wasted).
         */
        @Override
        public int area() {
            return area;
        }
        
        /**
         * @return the ratio of wasted area, e.g. area / wastedArea.
         */
        public double wastedRatio() {
            return ((double) wastedArea) / area;
        }
        
        /**
         * @return array containing the two entries involved with
         *     this operation. Note that either two can be an operator.
         * 
         * Calculates the entries if not yet calculated.
         */
        public CompareEntry[] getEntries() {
            if (entries == null) calcEntries();
            return entries;
        }
        
        /**
         * @return array containing the two entries involved with
         *     this operation. Note that either two can be an operator.
         * 
         * Always calculates the entries.
         */
        public CompareEntry[] calcEntries() {
            entries = new CompareEntry[2];
            ListIterator<CompareEntry> it = list.listIterator(list.size());
            
            // Search the current entry in the list and set the endpoint.
            // Safe call due to inverse polish notation.
            while (it.previous() != this) { }
            
            // Safe call due to inverse polish notation.
            entries[1] = it.previous();
            
            if (entries[1] instanceof Operator) {
                // If the entry is an operator, skip all entries between
                // the two main entries.
                int numEntry = 0;
                int numOp = 1;
                while (!(numEntry > numOp) ) {
                    // Safe call due to inverse polish notation.
                    CompareEntry entry = it.previous();
                    
                    if (entry instanceof Operator) numOp++;
                    else numEntry++;
                }
            }
            
            // Safe call due to inverse polish notation.
            entries[0] = it.previous();
            
            return entries;
        }
        
        /**
         * @return a list containing all involved entries.
         */
        public List<CompareEntry> listAllEntries() {
            if (allEntries != null) return allEntries;
            getEntries();
            allEntries = new ArrayList<>();
            
            for (int i = 0; i < 2; i++) {
                if (entries[i] instanceof Operator) {
                    allEntries.addAll(((Operator) entries[i]).listAllEntries());
                    
                } else {
                    allEntries.add(entries[i]);
                }
            }
            
            return allEntries;
        }
        
        /**
         * @return the total number of involved entries. Excludes operators.
         */
        public int size() {
            if (size != -1) return size;
            
            getEntries();
            size = 0;
            
            for (CompareEntry entry : entries) {
                if (entry instanceof Operator) {
                    size += ((Operator) entry).size();
                } else {
                    size += 1;
                }
            }
            
            return size;
        }
        
    }
    
    
    /**
     * Iterator that iterates in order over the operators in {@code list}.
     */
    private class OperatorIterator
            implements Iterator<Operator> {
        // Iterator over the list.
        final private Iterator<CompareEntry> it;
        // The next operator.
        private Operator next = null;
        
        
        /**
         * Default constructor.
         */
        private OperatorIterator() {
            it = list.iterator();
        }
        
        
        @Override
        public boolean hasNext() {
            calcNext();
            return next != null;
        }
        
        @Override
        public Operator next() {
            if (!hasNext()) throw new NoSuchElementException();
            Operator op = next;
            next = null;
            return op;
        }
        
        /**
         * Calculates the next operator.
         * Simply returns if the next operator has already been calculated.
         * If a next element exists, then {@code next} represents
         * the next element. If no next element exists, then
         * {@code next == null}.
         */
        private void calcNext() {
            if (next != null) return;
            
            while (it.hasNext()) {
                CompareEntry entry = it.next();
                if (entry instanceof Operator) {
                    next = (Operator) entry;
                    break;
                }
            }
        }
        
    }
    
    
    /**
     * Creates a new PolishDataset from the provided dataset.
     * 
     * @param dataset the input dataset.
     */
    public PolishDataset(Dataset dataset) {
        super(dataset);
        list = new Stack<>();
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
    public PolishDataset(Dataset dataset, Stack<CompareEntry> list) {
        super(dataset);
        this.list = list;
    }
    
    
    @Override
    public PolishDataset clone() {
        return new PolishDataset(dataset.clone(), (Stack<CompareEntry>) list.clone());
    }
    
    /**
     * Initializes the dataset with a random reverse polish notation.
     * Note that the elements still occur in the same order as in
     * the provided dataset.
     */
    public void init(List<CompareEntry>... hints) {
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
            Operator op = new Operator();
            list.add(op);
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
    public Iterator<CompareEntry> fullListIterator() {
        return list.iterator();
    }
    
    public Iterator<Operator> operatorIterator() {
        return new OperatorIterator();
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

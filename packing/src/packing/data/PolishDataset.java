
package packing.data;


// Packing imports
import packing.genetic.CrossoverPopulation;
import packing.tools.MultiTool;
import packing.tools.Logger;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;


/**
 * Dataset that stores the placement of the entries in a polish notation.
 */
public class PolishDataset
        extends DatasetDecorator {
    
    // The used random variable.
    final private static Random random = new Random();
    
    // Counter for the operators. Must be static.
    private static int opCounter = 0;
    
    // Map containing a map from the id's to the corresponding entries.
    final private Map<Integer, CompareEntry> entryMap = new HashMap<>();
    
    
    /**
     * Direction class for denoting the operator direction.
     * Let A and B be (configurations of) rectangles, then:
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
    public static class Operator
            extends CompareEntry {
        // Direction of the operator.
        final private Direction dir;
        
        private PolishDataset pd;
        
        // The total wasted area of the operator.
        private int wastedArea = 0;
        
        // The total area of the operator (covered + wasted).
        private int area = 0;
        
        // The involved entries.
        private CompareEntry[] entries = null; // Two direct involved entries.
        
        // The total number of involved entries, excluding operators.
        private int size = -1;
        
        /**
         * Creates a new operator with the provided direction.
         * 
         * @param dir the direcion of the operator.
         */
        public Operator(Direction dir, PolishDataset pd) {
            super(opCounter++);
            this.dir = dir;
            this.pd = pd;
        }
        
        /**
         * Creates a new operator using the provided boolean option as
         * operator.
         * 
         * @param option converted to {@link Direction#RIGHT} if true,
         *     {@link Direction#UP} otherwise.
         */
        public Operator(boolean option, PolishDataset pd) {
            this(option ? Direction.RIGHT : Direction.UP, pd);
        }
        
        /**
         * Randomly creates a new operator.
         * TMP
         * @param entry1 the first entry of the operation.
         * @param entry2 the second entry of the operation.
         */
        public Operator(PolishDataset pd) {
            this(random.nextBoolean(), pd);
        }
        
        /**
         * Clone constructor.
         * @param clone operator to be cloned.
         */
        public Operator(Operator clone) {
            this(clone.dir, clone.pd);
        }
        
        
        /**
         * @return the direction of the operator.
         */
        public Direction getDirection() {
            return dir;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Operator)) return false;
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
         * @return the list of the outer class of this operator.
         */
        protected List<CompareEntry> getList() {
            return pd.list;
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
            ListIterator<CompareEntry> it
                    = pd.list.listIterator(pd.list.size());
            
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
            calcEntries();
            List<CompareEntry> allEntries = new ArrayList<>();
            
            for (int i = 0; i < 2; i++) {
                if (entries[i] instanceof Operator) {
                    Operator op = (Operator) entries[i];
                    allEntries.addAll(op.listAllEntries());
                    
                } else {
                    allEntries.add(entries[i]);
                }
            }
            
            return allEntries;
        }
        
        /**
         * @return a list containing all involved entries and operators.
         */
        public List<CompareEntry> listAllInvolved() {
            calcEntries();
            List<CompareEntry> allInvolved = new ArrayList<>();
            
            for (int i = 0; i < 2; i++) {
                if (entries[i] instanceof Operator) {
                    Operator op = (Operator) entries[i];
                    allInvolved.addAll(op.listAllInvolved());
                    
                } else {
                    allInvolved.add(entries[i]);
                }
            }
            
            allInvolved.add(this);
            
            return allInvolved;
        }
        
        /**
         * @return the total number of involved entries. Excludes operators.
         */
        public int size() {
            if (size != -1) return size;
            
            calcEntries();
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
        
        /**
         * Sets the {@code PolishDataset} from which the
         * {@link PolishDataset#list} should be obtained from.
         * @param pd 
         */
        protected void setPD(PolishDataset pd) {
            this.pd = pd;
        }
        
        @Override
        public Operator clone() {
            return new Operator(this);
        }
        
        /**
         * Unsupported operations.
         */
        @Override
        public Rectangle getRec() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setSize(int width, int height) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setLocation(int x, int y) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void rotate() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public void setRotation(boolean rotation) {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean useRotation() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Rectangle getRotatedRec() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Rectangle getNormalRec() {
            throw new UnsupportedOperationException();
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
        protected void calcNext() {
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
        list = new LinkedList<CompareEntry>();
        for (CompareEntry entry : dataset) {
            entryMap.put(entry.getId(), entry);
        }
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
    public PolishDataset(Dataset dataset, List<CompareEntry> list) {
        super(dataset);
        this.list = list;
        for (CompareEntry entry : dataset) {
            entryMap.put(entry.getId(), entry);
        }
    }
    
    /**
     * Clones the current entry. Note that {@link #dataset} must containg
     * at least all entries in {@link #list}.
     * 
     * @return a clone of this entry.
     */
    @Override
    public PolishDataset clone() {
        // Clone the main dataset.
        Dataset cloneData = dataset.clone();
        // Create a new instance using the cloned dataset.
        PolishDataset pd = new PolishDataset(cloneData, new LinkedList<>());
        
        // Copy all values based on their ID.
        // Create new entries for {@code Operator}s.
        Iterator<CompareEntry> it = list.iterator();
        while (it.hasNext()) {
            CompareEntry entry = it.next();
            if (entry instanceof Operator) {
                Operator op = (Operator) entry;
                pd.list.add(new Operator(op.dir, pd));
                
            } else {
                pd.list.add(pd.entryMap.get(entry.id));
            }
        }
        
        pd.setSize(width, height);
        
        return pd;
    }
    
    /**
     * Regenerates the solution using the current configuration
     *  and the provided hints.
     * 
     * @param hints used to generate a solution. If no hints are given,
     *     then no changes will occur.
     * 
     * Notes:
     * - It is not allowed to have multiple equal elements in the hints.
     *   There is no checking performed on this property.
     * - The items in the list will be processed from low to high index.
     * - It is assumed that all hints are correctly formatted
     *   (e.g. all use reverse polish notation).
     */
    public void regenerate(List<CompareEntry>... hints) {
        if (hints == null || hints.length == 0) return;
        
        // Merge all id's of the entries entries into one set.
        Set<Integer> entries = new HashSet<>();
        // Store the id of the last enties of each hint in one set.
        Set<Integer> endEntries = new HashSet<>();
        for (List<CompareEntry> hint : hints) {
            for (CompareEntry entry : hint) {
                entries.add(entry.getId());
            }
            
            if (!hint.isEmpty()) {
                endEntries.add(hint.get(hint.size() - 1).getId());
            }
        }
        
        List<CompareEntry> newList = new LinkedList<CompareEntry>();
        
        // Safely remove the entries to be replaced and
        // savely add entries at their new locations.
        ListIterator<CompareEntry> listIt = list.listIterator(0);
        Set<CompareEntry> ignoreSet = new HashSet<>();
        int hintCounter = 0;
        int loc = 0;
        while (listIt.hasNext()) {
            CompareEntry entry = listIt.next();
            // The entry is an operator.
            if (entry instanceof Operator) {
                Operator op = (Operator) entry;
                // Only add the operator if it is not in the ignore set.
                if (!ignoreSet.contains(entry)) {
                    newList.add(op);
                }
                
            } else if (entries.contains(entry.getId())) {
                // Skip all entries that should be removed.
                // Note that this is allowed since each separate hint should
                // be a valid polish notation on it's own. Moreover, the
                // iteration also stops when the end of a hint has been reached
                // (e.g. the end of a "big multi-entry" in polish notation,
                // which can be represented as a single element).
                /*
                if (!endEntries.contains(entry.getId())) {
                    while (listIt.hasNext()) {
                        CompareEntry entry2 = listIt.next();
                        if (entry2 instanceof Operator) {
                            listIt.previous();
                            break;
                        }
                        
                        if (!entries.contains(entry2.getId()) ||
                                endEntries.contains(entry2.getId())) break;
                    }
                }*/
                
                // Check if there are any hints left to add.
                if (hintCounter < hints.length) {
                    // If there are, use the next hint to replace the
                    // removed element.
                    for (CompareEntry entry2 : hints[hintCounter++]) {
                        if (entry2 instanceof Operator) {
                            newList.add(entry2.clone());
                            
                        } else {
                            newList.add(entryMap.get(entry2.getId()));
                        }
                    }
                    
                    // tmp
                    List<CompareEntry> tmp = list;
                    list = newList;
                    Logger.write("regen[0]: " + hints[hintCounter - 1]);
                    Logger.write("regen[1]: " + toShortString());
                    list = tmp;
                    
                } else {
                    // If not, remove the corresponding operator.
                    ListIterator<CompareEntry> it = list.listIterator(loc);
                    int entryCounter = 0;
                    while (it.hasNext()) {
                        CompareEntry entry2 = it.next();
                        if (entry2 instanceof Operator) {
                            if (--entryCounter <= 0) {
                                ignoreSet.add(entry2);
                                break;
                            }
                        } else {
                            entryCounter++;
                        }
                    }
                    
                    // tmp
                    List<CompareEntry> tmp = list;
                    list = newList;
                    Logger.write("regen[2]: " + toShortString());
                    list = tmp;
                }
                
            } else {
                newList.add(entryMap.get(entry.getId()));
            }
            
            loc++;
        }
        
        list = newList;
        for (CompareEntry entry : list) {
            if (entry instanceof Operator) {
                Operator op = (Operator) entry;
                op.setPD(this);
            }
        }
    }
    
    /**
     * Swaps two random entries. An entry might be an operator.
     * In that case, the entire part will be swapped.
     */
    @SuppressWarnings("null")
    public void swapRandomEntries() {
        while (true) {
            // Generate two positions to be swapped.
            int pos1 = random.nextInt(list.size());
            int pos2 = random.nextInt(list.size());
            CompareEntry ce1 = list.get(pos1);
            CompareEntry ce2 = list.get(pos2);
            
            // Swapping the same element is trivial.
            if (pos1 == pos2) return;
            
            boolean ce1IsOp = ce1 instanceof Operator;
            boolean ce2IsOp = ce2 instanceof Operator;
            
            //Logger.write("ce1: " + (ce1IsOp ? ce1 : "[" + ce1.getId() + "]"));
            //Logger.write("ce2: " + (ce2IsOp ? ce2 : "[" + ce2.getId() + "]"));
            
            if (!ce1IsOp && !ce2IsOp) {
                // Neither are operators, so simply swap them.
                Collections.swap(list, pos1, pos2);
                return;
            }
            
            List<CompareEntry> entries1 = null;
            List<CompareEntry> entries2 = null;
            
            if (ce1IsOp && ce2IsOp) {
                // Both are operators.
                Operator op1 = (Operator) ce1;
                Operator op2 = (Operator) ce2;
                entries1 = op1.listAllInvolved();
                entries2 = op2.listAllInvolved();
                //Logger.write("Involved [1]: " + entries1);
                //Logger.write("Involved [2]: " + entries2);
                
                // First add all entries from 1 to a set for easy lookup.
                Set<CompareEntry> set = new HashSet<>();
                set.addAll(entries1);
                // Then check if the set contains any entries of
                // {@code entries2}. If so, redo the proccess.
                boolean skip = false;
                for (CompareEntry entry : entries2) {
                    if (set.contains(entry)) {
                        skip = true;
                        break;
                    }
                }
                
                if (skip) continue;
                
            } else if (ce1IsOp && !ce2IsOp) {
                Operator op1 = (Operator) ce1;
                entries1 = op1.listAllInvolved();
                //Logger.write("Involved [1]: " + entries1);
                // If the {@code ce2} is involved in the operator {@code ce1},
                // redo the process.
                if (entries1.contains(ce2)) continue;
                entries2 = new ArrayList<CompareEntry>();
                entries2.add(ce2);
                
            } else if (!ce1IsOp && ce2IsOp) {
                Operator op2 = (Operator) ce2;
                entries2 = op2.listAllInvolved();
                //Logger.write("Involved [2]: " + entries2);
                // If the {@code ce1} is involved in the operator {@code ce2},
                // redo the process.
                if (entries2.contains(ce1)) continue;
                entries1 = new ArrayList<CompareEntry>();
                entries1.add(ce1);
            }
            
            // Determine which list is the first and the last one.
            List<CompareEntry> first;
            List<CompareEntry> last;
            int firstPos;
            int lastPos;
            
            // Determine which list is the first and the last one.
            if (pos1 < pos2) {
                first = entries1;
                firstPos = pos1;
                last = entries2;
                lastPos = pos2;
                
            } else {
                last = entries1;
                lastPos = pos1;
                first = entries2;
                firstPos = pos2;
            }
            
            list.removeAll(first);
            list.removeAll(last);
            Logger.write(list);
            list.addAll(lastPos - first.size() - last.size() + 1, first);
            list.addAll(firstPos - first.size() + 1, last);
            Logger.write("pd: " + toShortString());
            return;
        }
    }
    
    /**
     * Randomly re-chooses an operator from the list.
     */
    public void changeOperator() {
        if (list.size() < 3) return;
        
        boolean found = false;
        while (!found) {
            int loc = random.nextInt(list.size());
            CompareEntry entry = list.get(loc);
            if (entry instanceof Operator) {
                found = true;
                list.set(loc, new Operator(this));
            }
        }
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
                list.add(new Operator(this));
                
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
            Operator op = new Operator(this);
            list.add(op);
        }
        
        Logger.write("init: " + toShortString());
        calcEffectiveSize();
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
            if (entry == null) {
                strs[i++] = "null";
            } else {
                strs[i++] = entry.toString();
            }
        }
        
        return getClass().getSimpleName() + "[width=" + width + ", height="
                + height + ", elems:["  + String.join("", strs) + "]";
    }
    
    /**
     * @return a short representation of the data using id's as identifiers.
     */
    public String toShortString() {
        String[] strs = new String[list.size()];
        
        int i = 0;
        for (CompareEntry entry : list) {
            if (entry == null) {
                strs[i++] = "null";
            } else if (entry instanceof Operator) {
                strs[i++] = entry.toString();
            } else {
                strs[i++] = "[" + entry.id + "]";
            }
        }
        
        return getClass().getSimpleName() + "[" + "width=" + width
                + ", height=" + height + ", elems:[" + String.join("", strs)
                + "]]";
    }
    
    @Override
    public void initList() {
        list = new LinkedList<CompareEntry>();
    }
    
    @Override
    public int getEffectiveWidth() {
        return dataset.getEffectiveWidth();
    }
    
    @Override
    public int getEffectiveHeight() {
        return dataset.getEffectiveHeight();
    }
    
    /*
    public static void main(String[] args) {
        Dataset dataset = new Dataset(-1, true, 3);
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(2, 2));
        dataset.add(new Rectangle(3, 3));
        dataset.add(new Rectangle(4, 4));
        dataset.add(new Rectangle(5, 5));
        dataset.add(new Rectangle(6, 6));
        dataset.add(new Rectangle(7, 7));
        dataset.add(new Rectangle(8, 8));
        
        /*
        PolishDataset pd = new PolishDataset(dataset);
        System.out.println(pd.toShortString());
        pd.init();
        System.out.println(pd.toShortString());
        MultiTool.sleepThread(100);
        pd.swapRandomEntries();
        MultiTool.sleepThread(100);
        System.out.println(pd.toShortString());
        MultiTool.sleepThread(100);
        pd.swapRandomEntries();
        MultiTool.sleepThread(100);
        System.out.println(pd.toShortString());
        /**/
        /*
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
        /*
    }
    */
    public static void main(String[] args) {
        CrossoverPopulation.main(null);
    }/**/
}
